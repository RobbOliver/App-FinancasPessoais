package com.robson.financas.data.local.dao.fiscal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.robson.financas.data.local.entity.fiscal.ClassificationHistoryEntity

@Dao
interface ClassificationHistoryDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: ClassificationHistoryEntity): Long

    @Query("SELECT * FROM classification_history WHERE purchaseItemId = :purchaseItemId ORDER BY createdAt DESC")
    suspend fun getForItem(purchaseItemId: Long): List<ClassificationHistoryEntity>
}
