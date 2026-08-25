package com.robson.financas.data.local.dao.fiscal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.robson.financas.data.local.entity.fiscal.PriceHistoryEntity
import com.robson.financas.data.local.relation.fiscal.EstablishmentPricePoint
import com.robson.financas.data.local.relation.fiscal.ProductPriceSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceHistoryDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: PriceHistoryEntity): Long

    @Query("SELECT * FROM price_history WHERE productId = :productId ORDER BY purchasedAt DESC")
    fun observeForProduct(productId: Long): Flow<List<PriceHistoryEntity>>

    @Query("SELECT * FROM price_history WHERE productId = :productId ORDER BY purchasedAt DESC LIMIT 1")
    suspend fun getLastForProduct(productId: Long): PriceHistoryEntity?

    @Query(
        """
        SELECT
            productId,
            MIN(normalizedPriceCents) AS minNormalizedCents,
            MAX(normalizedPriceCents) AS maxNormalizedCents,
            CAST(AVG(normalizedPriceCents) AS INTEGER) AS avgNormalizedCents,
            COUNT(*) AS purchaseCount
        FROM price_history
        WHERE productId = :productId
        GROUP BY productId
        """,
    )
    fun observeSummary(productId: Long): Flow<ProductPriceSummary?>

    @Query(
        """
        SELECT
            ph.establishmentId AS establishmentId,
            e.name AS establishmentName,
            ph.normalizedPriceCents AS normalizedPriceCents,
            ph.purchasedAt AS purchasedAt
        FROM price_history ph
        LEFT JOIN establishments e ON e.id = ph.establishmentId
        WHERE ph.productId = :productId
        ORDER BY ph.normalizedPriceCents ASC
        """,
    )
    fun observeEstablishmentComparison(productId: Long): Flow<List<EstablishmentPricePoint>>
}
