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
                SubSeed(
                    "Hortifruti", "eco", "#2E7D32",
                    listOf(
                        MicroSeed("frutas", "Frutas", listOf("BANANA", "MACA", "MAÇÃ", "LARANJA", "MAMAO", "UVA", "FRUTA")),
                        MicroSeed("verduras_legumes", "Verduras e legumes", listOf("ALFACE", "TOMATE", "CEBOLA", "BATATA", "CENOURA", "VERDURA", "LEGUME")),
                    ),
                ),
                SubSeed(
                    "Padaria", "bakery_dining", "#8D6E63",
                    listOf(
                        MicroSeed("pao", "Pão", listOf("PAO FRANCES", "PÃO FRANCÊS", "PAO DE ", "PÃO DE ")),
                        MicroSeed("bolos_salgados", "Bolos e salgados", listOf("BOLO", "SALGADO", "ESFIHA", "COXINHA")),
                    ),
                ),
                SubSeed(
                    "Congelados e prontos", "kitchen", "#0097A7",
                    listOf(
                        MicroSeed("congelados", "Congelados", listOf("CONGELADO", "PRE PRONTO", "LASANHA CONG")),
                        MicroSeed("pratos_prontos", "Pratos prontos", listOf("PRATO PRONTO", "MARMITA")),
                    ),
                ),
                SubSeed(
                    "Doces e snacks", "cake", "#D81B60",
                    listOf(
                        MicroSeed("chocolates_doces", "Chocolates e doces", listOf("CHOCOLATE", "BOMBOM", "DOCE ", "BALA")),
                        MicroSeed("salgadinhos", "Salgadinhos e snacks", listOf("SALGADINHO", "BATATA CHIPS", "SNACK")),
                        MicroSeed("biscoitos", "Biscoitos", listOf("BISCOITO", "BOLACHA")),
                    ),
                ),
                SubSeed(
                    "Bebidas alcoólicas", "sports_bar", "#6A1B9A",
                    listOf(
                        MicroSeed("cerveja", "Cerveja", listOf("CERVEJA", "CHOPP")),
                        MicroSeed("vinho", "Vinho", listOf("VINHO", "ESPUMANTE")),
                        MicroSeed("destilados", "Destilados", listOf("VODKA", "WHISKY", "CACHACA", "CACHAÇA", "GIN ")),
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
                SubSeed(
                    "Manutenção e reparos", "handyman", "#5D4037",
                    listOf(
                        MicroSeed("ferramentas", "Ferramentas", listOf("FERRAMENTA", "FURADEIRA", "CHAVE DE FENDA", "ALICATE")),
                        MicroSeed("material_construcao", "Material de construção", listOf("TINTA ", "CIMENTO", "PARAFUSO", "ELETRICO MATERIAL")),
                    ),
                ),
                SubSeed(
                    "Decoração", "chair", "#795548",
                    listOf(MicroSeed("decoracao", "Decoração", listOf("DECORACAO", "QUADRO DECOR", "ENFEITE", "VASO DECOR"))),
                ),
                SubSeed(
                    "Utensílios domésticos", "kitchen", "#546E7A",
                    listOf(MicroSeed("utensilios", "Utensílios domésticos", listOf("PANELA", "UTENSILIO", "TALHER", "LOUCA", "LOUÇA"))),
                ),
                SubSeed(
                    "Jardinagem", "yard", "#33691E",
                    listOf(
                        MicroSeed("plantas_mudas", "Plantas e mudas", listOf("MUDA ", "PLANTA ", "VASO PLANTA")),
                        MicroSeed("ferramentas_jardim", "Ferramentas de jardim", listOf("MANGUEIRA", "ADUBO", "FERTILIZANTE", "TESOURA JARDIM")),
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
                SubSeed(
                    "Cuidados com o cabelo", "content_cut", "#AB47BC",
                    listOf(MicroSeed("cuidados_cabelo", "Cuidados com o cabelo", listOf("CONDICIONADOR", "CREME CABELO", "TINTA CABELO", "GEL CABELO"))),
                ),
                SubSeed(
                    "Papel higiênico e descartáveis", "shopping_bag", "#7B1FA2",
                    listOf(
                        MicroSeed("papel_higienico", "Papel higiênico", listOf("PAPEL HIGIENICO", "PAPEL HIGIÊNICO")),
                        MicroSeed("absorvente_fraldas", "Absorventes e fraldas", listOf("ABSORVENTE", "FRALDA")),
                        MicroSeed("descartaveis", "Descartáveis", listOf("GUARDANAPO", "PAPEL TOALHA", "COPO DESCART")),
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
                SubSeed(
                    "Primeiros socorros", "medical_services", "#B71C1C",
                    listOf(MicroSeed("primeiros_socorros", "Primeiros socorros", listOf("CURATIVO", "BAND AID", "ALCOOL 70", "GAZE", "ATADURA"))),
                ),
                SubSeed(
                    "Suplementos e vitaminas", "medication", "#D32F2F",
                    listOf(MicroSeed("suplementos_vitaminas", "Suplementos e vitaminas", listOf("VITAMINA", "SUPLEMENTO", "WHEY", "OMEGA 3", "COLAGENO"))),
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
                SubSeed(
                    "Manutenção veicular", "build", "#0D47A1",
                    listOf(
                        MicroSeed("pecas_servicos", "Peças e serviços", listOf("OFICINA", "TROCA OLEO", "PNEU", "ALINHAMENTO", "REVISAO VEIC")),
                        MicroSeed("lavagem", "Lavagem", listOf("LAVA RAPIDO", "LAVAGEM CARRO")),
                    ),
                ),
                SubSeed(
                    "Pedágio e multas", "toll", "#283593",
                    listOf(
                        MicroSeed("pedagio", "Pedágio", listOf("PEDAGIO", "PEDÁGIO", "SEM PARAR")),
                        MicroSeed("multas", "Multas", listOf("MULTA TRANSITO", "INFRACAO")),
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
                SubSeed(
                    "Livros e revistas", "menu_book", "#6A1B9A",
                    listOf(MicroSeed("livros_revistas", "Livros e revistas", listOf("LIVRO ", "REVISTA "))),
                ),
                SubSeed(
                    "Hobbies", "sports_esports", "#7B1FA2",
                    listOf(MicroSeed("hobbies", "Hobbies", listOf("ARTESANATO", "COLECIONAVEL", "MODELISMO"))),
                ),
                SubSeed(
                    "Viagens", "flight", "#4527A0",
                    listOf(
                        MicroSeed("hospedagem", "Hospedagem", listOf("HOTEL", "POUSADA", "HOSPEDAGEM")),
                        MicroSeed("passagens", "Passagens", listOf("PASSAGEM AEREA", "PASSAGEM ONIBUS")),
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
                SubSeed(
                    "Higiene e cuidados pet", "pets", "#33691E",
                    listOf(MicroSeed("higiene_pet", "Higiene e cuidados pet", listOf("AREIA GATO", "SHAMPOO PET", "TAPETE HIGIENICO"))),
                ),
                SubSeed(
                    "Veterinário", "pets", "#1B5E20",
                    listOf(MicroSeed("veterinario", "Veterinário", listOf("VETERINARIO", "VETERINÁRIO", "PET SHOP CONSULTA"))),
                ),
            ),
        ),
        CategorySeed(
            name = "Vestuário", icon = "checkroom", colorHex = "#AD1457",
            subs = listOf(
                SubSeed("Roupas", "checkroom", "#AD1457", listOf(MicroSeed("roupas", "Roupas", listOf("CAMISA", "CALCA", "CALÇA", "VESTIDO", "BLUSA", "SHORT")))),
                SubSeed("Calçados", "checkroom", "#C2185B", listOf(MicroSeed("calcados", "Calçados", listOf("TENIS", "TÊNIS", "SAPATO", "SANDALIA", "CHINELO")))),
                SubSeed("Acessórios", "checkroom", "#D81B60", listOf(MicroSeed("acessorios_vestuario", "Acessórios", listOf("BONE", "BOLSA", "CINTO", "OCULOS")))),
            ),
        ),
        CategorySeed(
            name = "Eletrônicos e Informática", icon = "devices", colorHex = "#37474F",
            subs = listOf(
                SubSeed("Celulares e acessórios", "smartphone", "#37474F", listOf(MicroSeed("celulares", "Celulares e acessórios", listOf("CAPA CELULAR", "CARREGADOR", "FONE DE OUVIDO", "PELICULA")))),
                SubSeed("Informática", "computer", "#455A64", listOf(MicroSeed("informatica", "Informática", listOf("MOUSE", "TECLADO", "PENDRIVE", "HD EXTERNO", "NOTEBOOK")))),
                SubSeed("Eletrodomésticos", "kitchen", "#546E7A", listOf(MicroSeed("eletrodomesticos", "Eletrodomésticos", listOf("LIQUIDIFICADOR", "MICROONDAS", "GELADEIRA", "FERRO DE PASSAR")))),
            ),
        ),
        CategorySeed(
            name = "Papelaria e Escritório", icon = "edit", colorHex = "#F57F17",
            subs = listOf(
                SubSeed("Material escolar", "edit", "#F57F17", listOf(MicroSeed("material_escolar", "Material escolar", listOf("CADERNO", "CANETA", "LAPIS", "LÁPIS", "MOCHILA")))),
                SubSeed("Material de escritório", "edit", "#F9A825", listOf(MicroSeed("material_escritorio", "Material de escritório", listOf("PAPEL SULFITE", "TONER", "CARTUCHO IMPRESSORA", "GRAMPEADOR")))),
            ),
        ),
        CategorySeed(
            name = "Presentes e Festas", icon = "card_giftcard", colorHex = "#EC407A",
            subs = listOf(
                SubSeed("Presentes", "card_giftcard", "#EC407A", listOf(MicroSeed("presentes", "Presentes", listOf("PRESENTE", "EMBALAGEM PRESENTE")))),
                SubSeed("Artigos de festa", "celebration", "#F06292", listOf(MicroSeed("artigos_festa", "Artigos de festa", listOf("BALAO", "BALÃO", "ARTIGO FESTA", "VELA ANIVERSARIO")))),
            ),
        ),
    )

    /** Idempotente — chame sempre que o app abrir; só grava o que ainda não existe. */
    suspend fun seed(categoryDao: CategoryDao, microcategoryDao: MicrocategoryDao) {
        for (categorySeed in taxonomy) {
            val existingCategory = categoryDao.findByNameAndType(categorySeed.name, CategoryType.EXPENSE)
            val category = if (existingCategory != null) {
                if (!existingCategory.isAiTaxonomy) categoryDao.update(existingCategory.copy(isAiTaxonomy = true))
                existingCategory
            } else {
                categoryDao.getById(
                    categoryDao.insert(
                        CategoryEntity(
                            name = categorySeed.name,
                            type = CategoryType.EXPENSE,
                            icon = categorySeed.icon,
                            colorHex = categorySeed.colorHex,
                            isDefault = true,
                            isAiTaxonomy = true,
                        ),
                    ),
                )
            }
            val categoryId = category?.id ?: continue

            for (subSeed in categorySeed.subs) {
                val existingSubcategory = categoryDao.findByNameAndParent(subSeed.name, categoryId)
                val subcategoryId = if (existingSubcategory != null) {
                    if (!existingSubcategory.isAiTaxonomy) categoryDao.update(existingSubcategory.copy(isAiTaxonomy = true))
                    existingSubcategory.id
                } else {
                    categoryDao.insert(
                        CategoryEntity(
                            name = subSeed.name,
                            type = CategoryType.EXPENSE,
                            parentCategoryId = categoryId,
                            icon = subSeed.icon,
                            colorHex = subSeed.colorHex,
                            isDefault = true,
                            isAiTaxonomy = true,
                        ),
                    )
                }

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
