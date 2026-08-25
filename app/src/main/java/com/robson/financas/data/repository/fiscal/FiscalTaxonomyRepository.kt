package com.robson.financas.data.repository.fiscal

import com.robson.financas.data.local.dao.CategoryDao
import com.robson.financas.data.local.dao.fiscal.MicrocategoryDao
import com.robson.financas.domain.fiscal.model.MicrocategoryOption
import javax.inject.Inject
import javax.inject.Singleton

/** Leitura da taxonomia para telas de seleção (revisão, orçamento) — sem regras de classificação. */
@Singleton
class FiscalTaxonomyRepository @Inject constructor(
    private val microcategoryDao: MicrocategoryDao,
    private val categoryDao: CategoryDao,
) {
    suspend fun getMicrocategoryOptions(): List<MicrocategoryOption> {
        val microcategories = microcategoryDao.getAllActive()
        val subcategoryIds = microcategories.map { it.subcategoryId }.distinct()
        val subcategories = subcategoryIds.associateWith { categoryDao.getById(it) }
        val categoryIds = subcategories.values.mapNotNull { it?.parentCategoryId }.distinct()
        val categories = categoryIds.associateWith { categoryDao.getById(it) }

        return microcategories.mapNotNull { micro ->
            val subcategory = subcategories[micro.subcategoryId] ?: return@mapNotNull null
            val categoryId = subcategory.parentCategoryId ?: return@mapNotNull null
            val category = categories[categoryId] ?: return@mapNotNull null
            MicrocategoryOption(
                microcategoryId = micro.id,
                name = micro.name,
                subcategoryId = subcategory.id,
                subcategoryName = subcategory.name,
                categoryId = category.id,
                categoryName = category.name,
            )
        }.sortedWith(compareBy({ it.categoryName }, { it.subcategoryName }, { it.name }))
    }
}
