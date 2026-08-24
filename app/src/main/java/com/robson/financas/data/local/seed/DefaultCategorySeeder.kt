package com.robson.financas.data.local.seed

import com.robson.financas.data.local.entity.CategoryEntity
import com.robson.financas.data.local.entity.CategoryType

object DefaultCategorySeeder {

    fun buildDefaultCategories(): List<CategoryEntity> = listOf(
        CategoryEntity(name = "Alimentação", type = CategoryType.EXPENSE, icon = "restaurant", colorHex = "#EF6C00", isDefault = true),
        CategoryEntity(name = "Transporte", type = CategoryType.EXPENSE, icon = "directions_car", colorHex = "#1565C0", isDefault = true),
        CategoryEntity(name = "Moradia", type = CategoryType.EXPENSE, icon = "home", colorHex = "#6D4C41", isDefault = true),
        CategoryEntity(name = "Lazer", type = CategoryType.EXPENSE, icon = "movie", colorHex = "#8E24AA", isDefault = true),
        CategoryEntity(name = "Saúde", type = CategoryType.EXPENSE, icon = "local_hospital", colorHex = "#C62828", isDefault = true),
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
