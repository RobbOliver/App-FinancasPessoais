package com.robson.financas.data.local.dao.fiscal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.robson.financas.data.local.entity.fiscal.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(product: ProductEntity): Long

    @Update
    suspend fun update(product: ProductEntity)

    @Query("SELECT * FROM products WHERE gtin = :gtin LIMIT 1")
    suspend fun findByGtin(gtin: String): ProductEntity?

    @Query("SELECT * FROM products WHERE genericName = :genericName AND (brand IS :brand OR brand = :brand) LIMIT 1")
    suspend fun findByNameAndBrand(genericName: String, brand: String?): ProductEntity?

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getById(id: Long): ProductEntity?

    @Query("SELECT * FROM products ORDER BY normalizedName ASC")
    fun observeAll(): Flow<List<ProductEntity>>

    @Query("UPDATE products SET brand = :brand WHERE id = :productId")
    suspend fun updateBrand(productId: Long, brand: String?)
}
