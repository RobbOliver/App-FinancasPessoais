package com.robson.financas.data.local.dao.fiscal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.robson.financas.data.local.entity.fiscal.RecurringPatternEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringPatternDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(pattern: RecurringPatternEntity)

    @Query("SELECT * FROM recurring_patterns ORDER BY nextExpectedAt ASC")
    fun observeAll(): Flow<List<RecurringPatternEntity>>

    @Query("SELECT * FROM recurring_patterns WHERE productId = :productId LIMIT 1")
    suspend fun findByProduct(productId: Long): RecurringPatternEntity?

    @Query("SELECT * FROM recurring_patterns WHERE microcategoryId = :microcategoryId AND productId IS NULL LIMIT 1")
    suspend fun findByMicrocategory(microcategoryId: Long): RecurringPatternEntity?
}
