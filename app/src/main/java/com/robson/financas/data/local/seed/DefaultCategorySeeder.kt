package com.robson.financas.data.local.seed

import com.robson.financas.data.local.entity.CategoryEntity
import com.robson.financas.data.local.entity.CategoryType

/**
 * Categorias de despesa que NÃO fazem parte da taxonomia fiscal (ver [com.robson.financas.data.local.seed.fiscal.FiscalTaxonomySeeder],
 * que cria/gerencia Alimentação, Transporte, Moradia, Lazer, Saúde etc. como categorias `isAiTaxonomy = true`)
 * — evita nome duplicado entre os dois seeders.
 */
object DefaultCategorySeeder {

    fun buildDefaultCategories(): List<CategoryEntity> = listOf(
        CategoryEntity(name = "Educação", type = CategoryType.EXPENSE, icon = "school", colorHex = "#00838F", isDefault = true),
        CategoryEntity(name = "Compras", type = CategoryType.EXPENSE, icon = "shopping_bag", colorHex = "#D81B60", isDefault = true),
        CategoryEntity(name = "Contas e Serviços", type = CategoryType.EXPENSE, icon = "receipt_long", colorHex = "#455A64", isDefault = true),
        CategoryEntity(name = "Outras despesas", type = CategoryType.EXPENSE, icon = "more_horiz", colorHex = "#757575", isDefault = true),
        CategoryEntity(name = "Salário", type = CategoryType.INCOME, icon = "payments", colorHex = "#2E7D32", isDefault = true),
        CategoryEntity(name = "Freelance", type = CategoryType.INCOME, icon = "work", colorHex = "#558B2F", isDefault = true),
        CategoryEntity(name = "Investimentos", type = CategoryType.INCOME, icon = "trending_up", colorHex = "#00695C", isDefault = true),
        CategoryEntity(name = "Outras receitas", type = CategoryType.INCOME, icon = "more_horiz", colorHex = "#757575", isDefault = true),
    )
}
