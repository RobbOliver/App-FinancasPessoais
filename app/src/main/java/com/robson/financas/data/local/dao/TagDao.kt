package com.robson.financas.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.robson.financas.data.local.entity.TagEntity
import com.robson.financas.data.local.entity.TransactionTagCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Insert
    suspend fun insert(tag: TagEntity): Long

    @Update
    suspend fun update(tag: TagEntity)

    @Delete
    suspend fun delete(tag: TagEntity)

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun observeAll(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getById(id: Long): TagEntity?

    @Query("SELECT * FROM tags t JOIN transaction_tag_cross_refs x ON x.tagId = t.id WHERE x.transactionId = :transactionId")
    fun observeTagsForTransaction(transactionId: Long): Flow<List<TagEntity>>

    @Query("DELETE FROM transaction_tag_cross_refs WHERE transactionId = :transactionId")
    suspend fun clearTagsForTransaction(transactionId: Long)

    @Insert
    suspend fun insertCrossRefs(crossRefs: List<TransactionTagCrossRef>)

    @Query("SELECT COUNT(*) FROM transaction_tag_cross_refs WHERE tagId = :tagId")
    suspend fun countUsagesForTag(tagId: Long): Int
}
