package com.robson.financas.data.local.dao.fiscal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.robson.financas.data.local.entity.fiscal.MatchType
import com.robson.financas.data.local.entity.fiscal.UserClassificationRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserClassificationRuleDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(rule: UserClassificationRuleEntity): Long

    @Update
    suspend fun update(rule: UserClassificationRuleEntity)

    @Delete
    suspend fun delete(rule: UserClassificationRuleEntity)

    @Query("SELECT * FROM user_classification_rules WHERE active = 1 ORDER BY priority DESC")
    suspend fun getAllActive(): List<UserClassificationRuleEntity>

    @Query("SELECT * FROM user_classification_rules ORDER BY priority DESC")
    fun observeAll(): Flow<List<UserClassificationRuleEntity>>

    @Query("SELECT * FROM user_classification_rules WHERE matchType = :matchType AND active = 1 ORDER BY priority DESC")
    suspend fun getActiveByType(matchType: MatchType): List<UserClassificationRuleEntity>

    @Query("UPDATE user_classification_rules SET timesApplied = timesApplied + 1 WHERE id = :id")
    suspend fun incrementTimesApplied(id: Long)
}
