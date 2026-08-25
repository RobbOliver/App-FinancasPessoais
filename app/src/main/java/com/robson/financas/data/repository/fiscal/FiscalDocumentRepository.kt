package com.robson.financas.data.repository.fiscal

import com.robson.financas.data.local.dao.fiscal.EstablishmentDao
import com.robson.financas.data.local.dao.fiscal.FiscalDocumentDao
import com.robson.financas.data.local.dao.fiscal.PurchaseItemDao
import com.robson.financas.data.local.entity.fiscal.ClassificationStatus
import com.robson.financas.data.local.entity.fiscal.EstablishmentEntity
import com.robson.financas.data.local.entity.fiscal.FiscalDocumentEntity
import com.robson.financas.data.local.entity.fiscal.PurchaseItemEntity
import com.robson.financas.data.local.relation.fiscal.PurchaseItemWithDetails
import com.robson.financas.domain.fiscal.AccessKeyValidator
import com.robson.financas.domain.fiscal.model.FiscalImportResult
import com.robson.financas.domain.fiscal.model.ParsedFiscalDocument
import com.robson.financas.domain.fiscal.parser.FiscalXmlParseException
import com.robson.financas.domain.fiscal.parser.NfeXmlParser
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest
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
) {
    fun observeAll() = fiscalDocumentDao.observeAll()

    fun observeById(id: Long): Flow<FiscalDocumentEntity?> = fiscalDocumentDao.observeById(id)

    fun observeItems(documentId: Long): Flow<List<PurchaseItemWithDetails>> =
        purchaseItemDao.observeByDocument(documentId)

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

        val itemEntities = parsed.items.map { item ->
            PurchaseItemEntity(
                fiscalDocumentId = documentId,
                establishmentId = establishmentId,
                originalDescription = item.originalDescription,
                normalizedDescription = item.originalDescription, // normalizador entra na etapa 3
                quantity = item.quantity,
                unit = item.unit,
                unitPriceCents = item.unitPriceCents,
                totalPriceCents = item.totalPriceCents,
                discountCents = item.discountCents,
                classificationStatus = ClassificationStatus.NEEDS_REVIEW,
            )
        }
        purchaseItemDao.insertAll(itemEntities)

        return FiscalImportResult.Success(documentId = documentId, itemCount = itemEntities.size, needsAttention = needsAttention)
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
