package com.robson.financas.data.local.dao.fiscal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.robson.financas.data.local.entity.fiscal.ProductAliasEntity

@Dao
interface ProductAliasDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alias: ProductAliasEntity): Long

    @Query("SELECT * FROM product_aliases WHERE rawDescription = :rawDescription")
    suspend fun findByRawDescription(rawDescription: String): ProductAliasEntity?

    @Query("SELECT * FROM product_aliases WHERE rawDescription IN (:rawDescriptions)")
    suspend fun findByRawDescriptions(rawDescriptions: List<String>): List<ProductAliasEntity>
}
