package com.robson.financas.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.robson.financas.data.local.dao.CategoryDao
import com.robson.financas.data.local.entity.CategoryEntity
import com.robson.financas.data.local.entity.CategoryType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao,
) {
    fun observeAll(): Flow<List<CategoryEntity>> = categoryDao.observeAll()

    fun observeByType(type: CategoryType): Flow<List<CategoryEntity>> = categoryDao.observeByType(type)

    suspend fun getById(id: Long): CategoryEntity? = categoryDao.getById(id)

    suspend fun create(category: CategoryEntity): Long = categoryDao.insert(category)

    suspend fun update(category: CategoryEntity) = categoryDao.update(category)

    suspend fun delete(category: CategoryEntity) {
        try {
            categoryDao.delete(category)
        } catch (e: SQLiteConstraintException) {
            val hasChildren = categoryDao.countChildren(category.id) > 0
            val message = if (hasChildren) {
                "Não é possível excluir: existem subcategorias vinculadas a esta categoria."
            } else {
                "Não é possível excluir: existem transações nesta categoria."
            }
            throw DeletionBlockedException(message)
        }
    }
}
