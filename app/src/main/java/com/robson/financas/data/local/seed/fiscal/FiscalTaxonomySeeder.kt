package com.robson.financas.data.local.seed.fiscal

import com.robson.financas.data.local.dao.CategoryDao
import com.robson.financas.data.local.dao.fiscal.MicrocategoryDao
import com.robson.financas.data.local.entity.CategoryEntity
import com.robson.financas.data.local.entity.CategoryType
import com.robson.financas.data.local.entity.fiscal.MicrocategoryEntity

private data class MicroSeed(val key: String, val name: String, val keywords: List<String>, val aliases: List<String> = emptyList())
private data class SubSeed(val name: String, val icon: String, val colorHex: String, val micros: List<MicroSeed>)
private data class CategorySeed(val name: String, val icon: String, val colorHex: String, val subs: List<SubSeed>)

/**
 * Semente da taxonomia fiscal (categoria → subcategoria → microcategoria — seção 7 do plano de
 * arquitetura). Reaproveita [CategoryEntity] existente para categoria/subcategoria (o app já usa
 * `parentCategoryId` para isso); só [MicrocategoryEntity] é nova e controlada pelo sistema.
 *
 * Idempotente: seguro rodar toda vez que o app abre — só cria o que ainda não existe, nunca
 * duplica, nunca sobrescreve algo que o usuário já renomeou.
 */
object FiscalTaxonomySeeder {

    private val taxonomy = listOf(
        CategorySeed(
            name = "Alimentação", icon = "restaurant", colorHex = "#EF6C00",
            subs = listOf(
                SubSeed(
                    "Carnes", "restaurant", "#C62828",
                    listOf(
                        MicroSeed("carne_moida", "Carne moída", listOf("CARNE MOIDA", "CARNE MOÍDA", "PATINHO MOIDO")),
                        MicroSeed("carne_bovina", "Carne bovina", listOf("CARNE BOV", "ALCATRA", "PICANHA", "CONTRA FILE", "COXAO")),
                        MicroSeed("frango", "Frango", listOf("FRANGO", "PEITO FGO", "COXA FGO", "SASSAMI")),
                        MicroSeed("peixe", "Peixe", listOf("PEIXE", "TILAPIA", "SALMAO", "FILE PEIXE")),
                        MicroSeed("linguica", "Linguiça", listOf("LINGUICA", "LINGUIÇA", "SALSICHA")),
                    ),
                ),
                SubSeed(
                    "Laticínios", "local_cafe", "#F9A825",
                    listOf(
                        MicroSeed("leite_integral", "Leite integral", listOf("LEITE INTEG", "LEITE UHT INT")),
                        MicroSeed("leite_desnatado", "Leite desnatado", listOf("LEITE DESN", "LEITE ZERO")),
                        MicroSeed("queijos", "Queijos", listOf("QUEIJO", "MUSSARELA", "MUSS", "PRATO", "MINAS")),
                        MicroSeed("iogurtes", "Iogurtes", listOf("IOGURTE", "IOG ")),
                        MicroSeed("manteiga", "Manteiga", listOf("MANTEIGA", "MARGARINA")),
                    ),
                ),
                SubSeed(
                    "Mercearia", "local_grocery_store", "#6D4C41",
                    listOf(
                        MicroSeed("cafe_po", "Café em pó", listOf("CAFE PO", "CAFÉ PÓ", "CAFE TORR")),
                        MicroSeed("cafe_capsula", "Café em cápsula", listOf("CAFE CAPS", "CAPSULA CAFE")),
                        MicroSeed("arroz", "Arroz", listOf("ARROZ")),
                        MicroSeed("feijao", "Feijão", listOf("FEIJAO", "FEIJÃO")),
                        MicroSeed("oleo", "Óleo", listOf("OLEO SOJA", "OLEO DE", "AZEITE")),
                        MicroSeed("acucar", "Açúcar", listOf("ACUCAR", "AÇÚCAR")),
                    ),
                ),
                SubSeed(
                    "Bebidas", "local_cafe", "#00897B",
                    listOf(
                        MicroSeed("refrigerante", "Refrigerante", listOf("REFRIGERANTE", "REFRI ", "COCA COLA", "GUARANA")),
                        MicroSeed("agua", "Água", listOf("AGUA MINERAL", "AGUA S/GAS", "AGUA C/GAS")),
                        MicroSeed("suco", "Suco", listOf("SUCO ", "NECTAR")),
                    ),
                ),
                SubSeed(
                    "Fora de casa", "restaurant", "#D84315",
                    listOf(
                        MicroSeed("restaurantes", "Restaurantes", listOf("RESTAURANTE", "LANCHONETE", "BUFFET")),
                        MicroSeed("delivery", "Delivery", listOf("DELIVERY", "IFOOD", "RAPPI")),
                    ),
                ),
            ),
        ),
        CategorySeed(
            name = "Moradia", icon = "home", colorHex = "#6D4C41",
            subs = listOf(
                SubSeed(
                    "Limpeza", "home", "#455A64",
                    listOf(
                        MicroSeed("sabao_po", "Sabão em pó", listOf("SABAO PO", "SABÃO PÓ", "LAV ROUPAS PO", "LAV.ROUPAS PO")),
                        MicroSeed("sabao_liquido", "Sabão líquido", listOf("SABAO LIQ", "LAV ROUPAS LIQ")),
                        MicroSeed("amaciante", "Amaciante", listOf("AMACIANTE")),
                        MicroSeed("detergente", "Detergente", listOf("DETERGENTE", "DETERG")),
                        MicroSeed("desinfetante", "Desinfetante", listOf("DESINFETANTE", "AGUA SANITARIA")),
                    ),
                ),
            ),
        ),
        CategorySeed(
            name = "Higiene", icon = "shopping_bag", colorHex = "#8E24AA",
            subs = listOf(
                SubSeed(
                    "Higiene pessoal", "shopping_bag", "#8E24AA",
                    listOf(
                        MicroSeed("shampoo", "Shampoo", listOf("SHAMPOO", "XAMPU")),
                        MicroSeed("sabonete", "Sabonete", listOf("SABONETE")),
                        MicroSeed("creme_dental", "Creme dental", listOf("CREME DENTAL", "PASTA DENTE")),
                        MicroSeed("desodorante", "Desodorante", listOf("DESODORANTE", "ANTITRANSPIRANTE")),
                    ),
                ),
            ),
        ),
        CategorySeed(
            name = "Saúde", icon = "local_hospital", colorHex = "#C62828",
            subs = listOf(
                SubSeed(
                    "Medicamentos", "local_hospital", "#C62828",
                    listOf(MicroSeed("medicamentos", "Medicamentos", listOf("DIPIRONA", "PARACETAMOL", "IBUPROFENO", "AMOXICILINA", "MG COMP", "CAPS "))),
                ),
            ),
        ),
        CategorySeed(
            name = "Transporte", icon = "directions_car", colorHex = "#1565C0",
            subs = listOf(
                SubSeed(
                    "Combustível", "local_gas_station", "#1565C0",
                    listOf(
                        MicroSeed("gasolina_comum", "Gasolina comum", listOf("GASOLINA COMUM", "GASOLINA C ")),
                        MicroSeed("gasolina_aditivada", "Gasolina aditivada", listOf("GASOLINA ADIT", "GASOLINA A ")),
                        MicroSeed("etanol", "Etanol", listOf("ETANOL", "ALCOOL COMB")),
                    ),
                ),
                SubSeed(
                    "Mobilidade", "directions_car", "#1976D2",
                    listOf(
                        MicroSeed("estacionamento", "Estacionamento", listOf("ESTACIONAMENTO", "PARKING")),
                        MicroSeed("app_transporte", "Aplicativo de transporte", listOf("UBER", "99 ", "TAXI")),
                    ),
                ),
            ),
        ),
        CategorySeed(
            name = "Lazer", icon = "movie", colorHex = "#8E24AA",
            subs = listOf(
                SubSeed(
                    "Entretenimento", "movie", "#8E24AA",
                    listOf(
                        MicroSeed("cinema", "Cinema", listOf("CINEMA", "INGRESSO FILME")),
                        MicroSeed("streaming", "Streaming", listOf("NETFLIX", "SPOTIFY", "DISNEY", "STREAMING")),
                        MicroSeed("jogos", "Jogos", listOf("STEAM", "PLAYSTATION", "XBOX", "JOGO ")),
                    ),
                ),
            ),
        ),
        CategorySeed(
            name = "Animais de estimação", icon = "pets", colorHex = "#558B2F",
            subs = listOf(
                SubSeed(
                    "Alimentação animal", "pets", "#558B2F",
                    listOf(
                        MicroSeed("racao_caes", "Ração para cães", listOf("RACAO CAO", "RAÇÃO CÃO", "RACAO CACHORRO")),
                        MicroSeed("racao_gatos", "Ração para gatos", listOf("RACAO GATO", "RAÇÃO GATO")),
                    ),
                ),
            ),
        ),
    )

    /** Idempotente — chame sempre que o app abrir; só grava o que ainda não existe. */
    suspend fun seed(categoryDao: CategoryDao, microcategoryDao: MicrocategoryDao) {
        for (categorySeed in taxonomy) {
            val category = categoryDao.findByNameAndType(categorySeed.name, CategoryType.EXPENSE)
                ?: categoryDao.getById(
                    categoryDao.insert(
                        CategoryEntity(
                            name = categorySeed.name,
                            type = CategoryType.EXPENSE,
                            icon = categorySeed.icon,
                            colorHex = categorySeed.colorHex,
                            isDefault = true,
                        ),
                    ),
                )
            val categoryId = category?.id ?: continue

            for (subSeed in categorySeed.subs) {
                val subcategoryId = categoryDao.findByNameAndParent(subSeed.name, categoryId)?.id
                    ?: categoryDao.insert(
                        CategoryEntity(
                            name = subSeed.name,
                            type = CategoryType.EXPENSE,
                            parentCategoryId = categoryId,
                            icon = subSeed.icon,
                            colorHex = subSeed.colorHex,
                            isDefault = true,
                        ),
                    )

                for (microSeed in subSeed.micros) {
                    if (microcategoryDao.findByKey(microSeed.key) != null) continue
                    microcategoryDao.insert(
                        MicrocategoryEntity(
                            systemKey = microSeed.key,
                            name = microSeed.name,
                            subcategoryId = subcategoryId,
                            aliases = microSeed.aliases.toJsonArray(),
                            keywords = microSeed.keywords.toJsonArray(),
                        ),
                    )
                }
            }
        }
    }
}

/** Serialização mínima de lista de strings — sem aspas/vírgulas nos termos, não precisa de parser JSON de verdade. */
fun List<String>.toJsonArray(): String = joinToString(prefix = "[", postfix = "]") { "\"${it.replace("\"", "")}\"" }

fun String.parseJsonStringArray(): List<String> =
    trim('[', ']').split(',').map { it.trim().trim('"') }.filter { it.isNotEmpty() }
