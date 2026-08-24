package com.robson.financas.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.robson.financas.data.local.entity.AccountEntity
import com.robson.financas.data.local.relation.AccountWithBalance
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(account: AccountEntity): Long

    @Update
    suspend fun update(account: AccountEntity)

    @Delete
    suspend fun delete(account: AccountEntity)

    @Query("SELECT * FROM accounts WHERE id = :id")
    fun observeById(id: Long): Flow<AccountEntity?>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getById(id: Long): AccountEntity?

    @Query(
        """
        SELECT a.*,
            a.initialBalanceCents + COALESCE(SUM(
                CASE
                    WHEN t.type = 'INCOME' AND t.accountId = a.id THEN t.amountCents
                    WHEN t.type = 'EXPENSE' AND t.accountId = a.id THEN -t.amountCents
                    WHEN t.type = 'TRANSFER' AND t.accountId = a.id THEN -t.amountCents
                    WHEN t.type = 'TRANSFER' AND t.transferToAccountId = a.id THEN t.amountCents
                    ELSE 0
                END
            ), 0) AS balanceCents
        FROM accounts a
        LEFT JOIN transactions t ON (t.accountId = a.id OR t.transferToAccountId = a.id)
        WHERE a.isArchived = 0
        GROUP BY a.id
        ORDER BY a.createdAt ASC
        """,
    )
    fun observeActiveAccountsWithBalance(): Flow<List<AccountWithBalance>>

    @Query("SELECT COUNT(*) FROM transactions WHERE accountId = :accountId OR transferToAccountId = :accountId")
    suspend fun countTransactionsForAccount(accountId: Long): Int
}
