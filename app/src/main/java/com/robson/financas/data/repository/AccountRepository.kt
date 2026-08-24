package com.robson.financas.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.robson.financas.data.local.dao.AccountDao
import com.robson.financas.data.local.entity.AccountEntity
import com.robson.financas.data.local.relation.AccountWithBalance
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val accountDao: AccountDao,
) {
    fun observeActiveAccountsWithBalance(): Flow<List<AccountWithBalance>> =
        accountDao.observeActiveAccountsWithBalance()

    fun observeById(id: Long): Flow<AccountEntity?> = accountDao.observeById(id)

    suspend fun getById(id: Long): AccountEntity? = accountDao.getById(id)

    suspend fun create(account: AccountEntity): Long = accountDao.insert(account)

    suspend fun update(account: AccountEntity) = accountDao.update(account)

    suspend fun delete(account: AccountEntity) {
        try {
            accountDao.delete(account)
        } catch (e: SQLiteConstraintException) {
            throw DeletionBlockedException(
                "Não é possível excluir: existem transações nesta conta.",
            )
        }
    }
}
