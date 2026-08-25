package com.robson.financas.data.local.dao.fiscal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.robson.financas.data.local.entity.fiscal.PurchaseItemEntity
import com.robson.financas.data.local.relation.fiscal.PurchaseItemWithDetails
import kotlinx.coroutines.flow.Flow

private const val WITH_DETAILS_SELECT = """
    SELECT
        pi.*,
        e.name AS establishmentName,
        cat.name AS categoryName,
        sub.name AS subcategoryName,
        m.name AS microcategoryName
    FROM purchase_items pi
    LEFT JOIN establishments e ON e.id = pi.establishmentId
    LEFT JOIN categories cat ON cat.id = pi.categoryId
    LEFT JOIN categories sub ON sub.id = pi.subcategoryId
    LEFT JOIN microcategories m ON m.id = pi.microcategoryId
"""

@Dao
interface PurchaseItemDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: PurchaseItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(items: List<PurchaseItemEntity>): List<Long>

    @Update
    suspend fun update(item: PurchaseItemEntity)

    @Query("SELECT * FROM purchase_items WHERE fiscalDocumentId = :documentId")
    suspend fun getByDocument(documentId: Long): List<PurchaseItemEntity>

    @Query("SELECT * FROM purchase_items WHERE id = :id")
    suspend fun getById(id: Long): PurchaseItemEntity?

    @Query("$WITH_DETAILS_SELECT WHERE pi.fiscalDocumentId = :documentId ORDER BY pi.id ASC")
    fun observeByDocument(documentId: Long): Flow<List<PurchaseItemWithDetails>>

    @Query(
        "$WITH_DETAILS_SELECT WHERE pi.classificationStatus != 'AUTOMATIC' AND pi.classificationStatus != 'CONFIRMED' " +
            "ORDER BY pi.createdAt DESC",
    )
    fun observeNeedingReview(): Flow<List<PurchaseItemWithDetails>>

    @Query("SELECT COUNT(*) FROM purchase_items WHERE classificationStatus = 'NEEDS_REVIEW' OR classificationStatus = 'NEEDS_CONFIRMATION'")
    fun observePendingReviewCount(): Flow<Int>

    @Query(
        "SELECT * FROM purchase_items WHERE normalizedDescription = :normalizedDescription " +
            "AND classificationStatus IN ('AUTOMATIC', 'CONFIRMED') ORDER BY createdAt DESC LIMIT 1",
    )
    suspend fun findLastConfirmedByNormalizedDescription(normalizedDescription: String): PurchaseItemEntity?

    @Query(
        "SELECT * FROM purchase_items WHERE productId = :productId " +
            "AND classificationStatus IN ('AUTOMATIC', 'CONFIRMED') ORDER BY createdAt DESC LIMIT 1",
    )
    suspend fun findLastConfirmedByProduct(productId: Long): PurchaseItemEntity?
}
