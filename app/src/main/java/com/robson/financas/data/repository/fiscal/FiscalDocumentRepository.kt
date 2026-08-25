package com.robson.financas.data.repository.fiscal

import com.robson.financas.data.local.dao.CategoryDao
import com.robson.financas.data.local.dao.fiscal.ClassificationHistoryDao
import com.robson.financas.data.local.dao.fiscal.EstablishmentDao
import com.robson.financas.data.local.dao.fiscal.FiscalAuditLogDao
import com.robson.financas.data.local.dao.fiscal.FiscalDocumentDao
import com.robson.financas.data.local.dao.fiscal.MicrocategoryDao
import com.robson.financas.data.local.dao.fiscal.PriceHistoryDao
import com.robson.financas.data.local.dao.fiscal.ProductDao
import com.robson.financas.data.local.dao.fiscal.PurchaseItemDao
import com.robson.financas.data.local.dao.fiscal.UserClassificationRuleDao
import com.robson.financas.data.local.entity.fiscal.ClassificationHistoryEntity
import com.robson.financas.data.local.entity.fiscal.ClassificationSource
import com.robson.financas.data.local.entity.fiscal.ClassificationStatus
import com.robson.financas.data.local.entity.fiscal.EstablishmentEntity
import com.robson.financas.data.local.entity.fiscal.FiscalAuditLogEntity
import com.robson.financas.data.local.entity.fiscal.FiscalDocumentEntity
import com.robson.financas.data.local.entity.fiscal.MatchType
import com.robson.financas.data.local.entity.fiscal.PriceHistoryEntity
import com.robson.financas.data.local.entity.fiscal.ProductEntity
import com.robson.financas.data.local.entity.fiscal.PurchaseItemEntity
import com.robson.financas.data.local.entity.fiscal.UserClassificationRuleEntity
import com.robson.financas.data.local.seed.fiscal.toJsonArray
import com.robson.financas.data.local.relation.fiscal.PurchaseItemWithDetails
import com.robson.financas.data.local.seed.fiscal.parseJsonStringArray
import com.robson.financas.domain.fiscal.AccessKeyValidator
import com.robson.financas.domain.fiscal.classification.ClassificationContext
import com.robson.financas.domain.fiscal.classification.ClassificationEngine
import com.robson.financas.domain.fiscal.classification.ConfidenceRouter
import com.robson.financas.domain.fiscal.classification.MicrocategoryTaxonomy
import com.robson.financas.domain.fiscal.classification.PriorMatch
import com.robson.financas.domain.fiscal.classification.UserRule
import com.robson.financas.domain.fiscal.model.FiscalImportResult
import com.robson.financas.domain.fiscal.model.NormalizedProduct
import com.robson.financas.domain.fiscal.model.ParsedFiscalDocument
import com.robson.financas.domain.fiscal.model.ParsedItem
import com.robson.financas.domain.fiscal.normalization.ItemNormalizer
import com.robson.financas.domain.fiscal.parser.FiscalXmlParseException
import com.robson.financas.domain.fiscal.parser.NfeXmlParser
import com.robson.financas.domain.fiscal.price.PriceNormalizer
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/** Tolerância de arredondamento entre a soma dos itens e o total da nota (seção 14 do plano). */
private const val RECONCILIATION_TOLERANCE_CENTS = 1L

@Singleton
class FiscalDocumentRepository @Inject constructor(
    private val fiscalDocumentDao: FiscalDocumentDao,
    private val establishmentDao: EstablishmentDao,
    private val purchaseItemDao: PurchaseItemDao,
    private val productDao: ProductDao,
    private val microcategoryDao: MicrocategoryDao,
    private val userClassificationRuleDao: UserClassificationRuleDao,
    private val categoryDao: CategoryDao,
    private val priceHistoryDao: PriceHistoryDao,
    private val classificationHistoryDao: ClassificationHistoryDao,
    private val fiscalAuditLogDao: FiscalAuditLogDao,
) {
    private val classificationEngine = ClassificationEngine.default()

    fun observeAll() = fiscalDocumentDao.observeAll()

    fun observeById(id: Long): Flow<FiscalDocumentEntity?> = fiscalDocumentDao.observeById(id)

    fun observeItems(documentId: Long): Flow<List<PurchaseItemWithDetails>> =
        purchaseItemDao.observeByDocument(documentId)

    fun observeItemsNeedingReview(): Flow<List<PurchaseItemWithDetails>> = purchaseItemDao.observeNeedingReview()

    suspend fun findByAccessKey(accessKey: String): FiscalDocumentEntity? = fiscalDocumentDao.findByAccessKey(accessKey)

    /** O sistema já sugeriu certo — o usuário só confirma, sem trocar nada. */
    suspend fun confirmClassification(itemId: Long) {
        val item = purchaseItemDao.getById(itemId) ?: return
        purchaseItemDao.update(item.copy(classificationStatus = ClassificationStatus.CONFIRMED))
    }

    /**
     * O usuário escolheu a microcategoria certa manualmente. [createRuleForDescription] grava
     * uma regra pessoal só se o usuário pedir explicitamente (seção 10 — nunca automático).
     */
    suspend fun correctClassification(itemId: Long, microcategoryId: Long, createRuleForDescription: Boolean) {
        val item = purchaseItemDao.getById(itemId) ?: return
        val microcategory = microcategoryDao.getById(microcategoryId) ?: return
        val subcategory = categoryDao.getById(microcategory.subcategoryId) ?: return
        val categoryId = subcategory.parentCategoryId ?: return

        classificationHistoryDao.insert(
            ClassificationHistoryEntity(
                purchaseItemId = itemId,
                previousCategoryId = item.categoryId,
                previousSubcategoryId = item.subcategoryId,
                previousMicrocategoryId = item.microcategoryId,
                newCategoryId = categoryId,
                newSubcategoryId = subcategory.id,
                newMicrocategoryId = microcategoryId,
                source = ClassificationSource.USER_CORRECTION,
                confidence = 1.0f,
                changedByUser = true,
            ),
        )

        purchaseItemDao.update(
            item.copy(
                categoryId = categoryId,
                subcategoryId = subcategory.id,
                microcategoryId = microcategoryId,
                classificationConfidence = 1.0f,
                classificationSource = ClassificationSource.USER_CORRECTION,
                classificationStatus = ClassificationStatus.CONFIRMED,
                classificationReason = "Corrigido manualmente por você.",
            ),
        )

        if (createRuleForDescription) {
            userClassificationRuleDao.insert(
                UserClassificationRuleEntity(
                    matchType = MatchType.DESCRIPTION_CONTAINS,
                    matchValue = listOf(item.normalizedDescription).toJsonArray(),
                    categoryId = categoryId,
                    subcategoryId = subcategory.id,
                    microcategoryId = microcategoryId,
                ),
            )
        }
    }

    /** Dispensa da fila sem atribuir categoria — para itens irrelevantes (troco, taxa, etc.). */
    suspend fun ignoreItem(itemId: Long) {
        val item = purchaseItemDao.getById(itemId) ?: return
        purchaseItemDao.update(item.copy(classificationStatus = ClassificationStatus.CONFIRMED))
        fiscalAuditLogDao.insert(
            FiscalAuditLogEntity(
                entityType = "purchase_item",
                entityId = itemId,
                action = "ignored_in_review",
            ),
        )
    }

    suspend fun importFromXml(xml: String): FiscalImportResult {
        val parsed = try {
            NfeXmlParser.parse(xml)
        } catch (e: FiscalXmlParseException) {
            return FiscalImportResult.Invalid(e.message ?: "XML fora do padrão de NF-e/NFC-e.")
        }
        return importParsedDocument(parsed)
    }

    suspend fun importParsedDocument(parsed: ParsedFiscalDocument): FiscalImportResult {
        val accessKey = parsed.accessKey
        if (accessKey != null && !AccessKeyValidator.isValid(accessKey)) {
            return FiscalImportResult.Invalid("Chave de acesso inválida (dígito verificador não confere).")
        }

        val idempotencyHash = accessKey ?: computeFallbackHash(parsed)

        accessKey?.let { key -> fiscalDocumentDao.findByAccessKey(key)?.let { return FiscalImportResult.Duplicate(it.id) } }
        fiscalDocumentDao.findByIdempotencyHash(idempotencyHash)?.let { return FiscalImportResult.Duplicate(it.id) }

        val establishmentId = findOrCreateEstablishment(parsed)

        val itemsTotal = parsed.items.sumOf { it.totalPriceCents - it.discountCents }
        val needsAttention = abs(itemsTotal - parsed.totalCents) > RECONCILIATION_TOLERANCE_CENTS

        val documentId = fiscalDocumentDao.insert(
            FiscalDocumentEntity(
                accessKey = accessKey,
                source = parsed.source,
                issuerCnpj = parsed.issuerCnpj,
                establishmentId = establishmentId,
                issuedAt = parsed.issuedAt,
                totalCents = parsed.totalCents,
                status = parsed.status,
                rawData = parsed.rawData,
                needsAttention = needsAttention,
                idempotencyHash = idempotencyHash,
            ),
        )

        // Contexto de classificação buscado uma única vez por importação, reaproveitado por item
        // (a taxonomia e as regras não mudam no meio de uma mesma nota).
        val taxonomy = buildTaxonomyContext()
        val rules = buildUserRules()

        for (item in parsed.items) {
            classifyAndInsertItem(item, documentId, establishmentId, taxonomy, rules)
        }

        return FiscalImportResult.Success(documentId = documentId, itemCount = parsed.items.size, needsAttention = needsAttention)
    }

    private suspend fun classifyAndInsertItem(
        item: ParsedItem,
        documentId: Long,
        establishmentId: Long?,
        taxonomy: List<MicrocategoryTaxonomy>,
        rules: List<UserRule>,
    ) {
        val normalized = ItemNormalizer.normalize(item.originalDescription)
        val productId = findOrCreateProduct(normalized, item.gtin)
        val rawUpper = item.originalDescription.uppercase(Locale.ROOT).trim()

        val prior = purchaseItemDao.findLastConfirmedByNormalizedDescription(rawUpper)?.let {
            if (it.categoryId != null && it.subcategoryId != null && it.microcategoryId != null) {
                PriorMatch(it.categoryId, it.subcategoryId, it.microcategoryId)
            } else {
                null
            }
        }

        val context = ClassificationContext(
            normalizedDescription = rawUpper,
            establishmentId = establishmentId,
            productId = productId,
            gtin = item.gtin,
            userRules = rules,
            microcategories = taxonomy,
            priorConfirmedMatch = prior,
        )
        val result = classificationEngine.classify(context)
        val status = ConfidenceRouter.statusFor(result.confidence)

        val itemId = purchaseItemDao.insert(
            PurchaseItemEntity(
                fiscalDocumentId = documentId,
                establishmentId = establishmentId,
                productId = productId,
                originalDescription = item.originalDescription,
                normalizedDescription = rawUpper,
                quantity = item.quantity,
                unit = item.unit,
                unitPriceCents = item.unitPriceCents,
                totalPriceCents = item.totalPriceCents,
                discountCents = item.discountCents,
                weightGrams = normalized.weightGrams,
                volumeMl = normalized.volumeMl,
                categoryId = result.categoryId,
                subcategoryId = result.subcategoryId,
                microcategoryId = result.microcategoryId,
                classificationConfidence = result.confidence,
                classificationSource = result.source,
                classificationStatus = status,
                classificationReason = result.reason,
            ),
        )

        val normalizedPrice = PriceNormalizer.normalize(item.unit, item.quantity, item.totalPriceCents - item.discountCents)
        priceHistoryDao.insert(
            PriceHistoryEntity(
                productId = productId,
                establishmentId = establishmentId,
                purchaseItemId = itemId,
                priceTotalCents = item.totalPriceCents - item.discountCents,
                normalizedPriceCents = normalizedPrice.normalizedPriceCents,
                normalizedUnit = normalizedPrice.normalizedUnit,
                quantity = item.quantity,
                purchasedAt = java.time.LocalDate.now(),
            ),
        )
    }

    private suspend fun findOrCreateProduct(normalized: NormalizedProduct, gtin: String?): Long {
        gtin?.let { key -> productDao.findByGtin(key)?.let { return it.id } }
        productDao.findByNameAndBrand(normalized.genericName, normalized.brand)?.let { return it.id }
        return productDao.insert(
            ProductEntity(
                normalizedName = normalized.normalizedName,
                genericName = normalized.genericName,
                brand = normalized.brand,
                gtin = gtin,
            ),
        )
    }

    private suspend fun buildTaxonomyContext(): List<MicrocategoryTaxonomy> {
        val microcategories = microcategoryDao.getAllActive()
        val subcategoryIds = microcategories.map { it.subcategoryId }.distinct()
        val parentByCategoryId = subcategoryIds.associateWith { categoryDao.getById(it)?.parentCategoryId }
        return microcategories.mapNotNull { micro ->
            val categoryId = parentByCategoryId[micro.subcategoryId] ?: return@mapNotNull null
            MicrocategoryTaxonomy(
                microcategoryId = micro.id,
                subcategoryId = micro.subcategoryId,
                categoryId = categoryId,
                keywords = micro.keywords.parseJsonStringArray(),
            )
        }
    }

    private suspend fun buildUserRules(): List<UserRule> =
        userClassificationRuleDao.getAllActive().map { rule: UserClassificationRuleEntity ->
            UserRule(
                id = rule.id,
                matchType = rule.matchType,
                matchValue = rule.matchValue,
                productId = rule.productId,
                categoryId = rule.categoryId,
                subcategoryId = rule.subcategoryId,
                microcategoryId = rule.microcategoryId,
                priority = rule.priority,
            )
        }

    private suspend fun findOrCreateEstablishment(parsed: ParsedFiscalDocument): Long? {
        val cnpj = parsed.issuerCnpj?.filter { it.isDigit() }?.takeIf { it.isNotBlank() } ?: return null
        establishmentDao.findByCnpj(cnpj)?.let { return it.id }
        val name = parsed.issuerName ?: "Estabelecimento $cnpj"
        return establishmentDao.insert(
            EstablishmentEntity(
                cnpj = cnpj,
                name = name,
                normalizedName = name.uppercase().trim(),
                city = parsed.issuerCity,
                state = parsed.issuerState,
            ),
        )
    }

    /** Só usado quando não há chave de acesso (ex.: uma futura importação manual/OCR sem QR). */
    private fun computeFallbackHash(parsed: ParsedFiscalDocument): String {
        val raw = "${parsed.issuerCnpj}|${parsed.issuedAt}|${parsed.totalCents}"
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
