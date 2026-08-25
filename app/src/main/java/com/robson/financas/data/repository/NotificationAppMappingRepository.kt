package com.robson.financas.data.repository

import com.robson.financas.data.local.dao.NotificationAppMappingDao
import com.robson.financas.data.local.entity.NotificationAppMappingEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationAppMappingRepository @Inject constructor(
    private val dao: NotificationAppMappingDao,
) {
    fun observeAll(): Flow<List<NotificationAppMappingEntity>> = dao.observeAll()

    suspend fun getByPackageName(packageName: String): NotificationAppMappingEntity? =
        dao.getByPackageName(packageName)

    suspend fun setAccount(packageName: String, accountId: Long) =
        dao.upsert(NotificationAppMappingEntity(packageName = packageName, accountId = accountId, enabled = true))

    suspend fun setEnabled(packageName: String, accountId: Long, enabled: Boolean) =
        dao.upsert(NotificationAppMappingEntity(packageName = packageName, accountId = accountId, enabled = enabled))

    suspend fun remove(packageName: String) = dao.deleteByPackageName(packageName)
}
