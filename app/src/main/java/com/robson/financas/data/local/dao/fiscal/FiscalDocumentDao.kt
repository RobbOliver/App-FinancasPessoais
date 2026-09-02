package com.robson.financas.data.local.dao.fiscal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.robson.financas.data.local.entity.fiscal.FiscalDocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FiscalDocumentDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(document: FiscalDocumentEntity): Long

    @Update
    suspend fun update(document: FiscalDocumentEntity)

    @Delete
    suspend fun delete(document: FiscalDocumentEntity)

    @Query("SELECT * FROM fiscal_documents WHERE id = :id")
    suspend fun getById(id: Long): FiscalDocumentEntity?

    @Query("SELECT * FROM fiscal_documents WHERE id = :id")
    fun observeById(id: Long): Flow<FiscalDocumentEntity?>

    @Query("SELECT * FROM fiscal_documents WHERE accessKey = :accessKey LIMIT 1")
    suspend fun findByAccessKey(accessKey: String): FiscalDocumentEntity?

    @Query("SELECT * FROM fiscal_documents WHERE idempotencyHash = :hash LIMIT 1")
    suspend fun findByIdempotencyHash(hash: String): FiscalDocumentEntity?

    @Query("SELECT * FROM fiscal_documents ORDER BY issuedAt DESC")
    fun observeAll(): Flow<List<FiscalDocumentEntity>>

    @Query("SELECT * FROM fiscal_documents WHERE linkedTransactionId = :transactionId LIMIT 1")
    suspend fun getByLinkedTransactionId(transactionId: Long): FiscalDocumentEntity?

    @Query("SELECT * FROM fiscal_documents WHERE linkedTransactionId = :transactionId LIMIT 1")
    fun observeByLinkedTransactionId(transactionId: Long): Flow<FiscalDocumentEntity?>
}
