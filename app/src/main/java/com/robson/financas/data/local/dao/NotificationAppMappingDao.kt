package com.robson.financas.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.robson.financas.data.local.entity.NotificationAppMappingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationAppMappingDao {
    @Upsert
    suspend fun upsert(mapping: NotificationAppMappingEntity)

    @Query("DELETE FROM notification_app_mappings WHERE packageName = :packageName")
    suspend fun deleteByPackageName(packageName: String)

    @Query("SELECT * FROM notification_app_mappings")
    fun observeAll(): Flow<List<NotificationAppMappingEntity>>

    @Query("SELECT * FROM notification_app_mappings WHERE packageName = :packageName")
    suspend fun getByPackageName(packageName: String): NotificationAppMappingEntity?
}
