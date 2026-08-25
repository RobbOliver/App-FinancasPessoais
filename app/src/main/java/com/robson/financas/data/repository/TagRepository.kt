package com.robson.financas.data.repository

import com.robson.financas.data.local.dao.TagDao
import com.robson.financas.data.local.entity.TagEntity
import com.robson.financas.data.local.entity.TransactionTagCrossRef
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepository @Inject constructor(
    private val tagDao: TagDao,
) {
    fun observeAll(): Flow<List<TagEntity>> = tagDao.observeAll()

    suspend fun create(tag: TagEntity): Long = tagDao.insert(tag)

    suspend fun update(tag: TagEntity) = tagDao.update(tag)

    suspend fun delete(tag: TagEntity) = tagDao.delete(tag)

    fun observeTagsForTransaction(transactionId: Long): Flow<List<TagEntity>> =
        tagDao.observeTagsForTransaction(transactionId)

    suspend fun setTagsForTransaction(transactionId: Long, tagIds: List<Long>) {
        tagDao.clearTagsForTransaction(transactionId)
        if (tagIds.isNotEmpty()) {
            tagDao.insertCrossRefs(tagIds.map { TransactionTagCrossRef(transactionId, it) })
        }
    }
}
