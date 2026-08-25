package com.robson.financas.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.robson.financas.data.local.dao.AccountDao
import com.robson.financas.data.local.dao.CategoryDao
import com.robson.financas.data.local.dao.GoalDao
import com.robson.financas.data.local.dao.NotificationAppMappingDao
import com.robson.financas.data.local.dao.TransactionDao
import com.robson.financas.data.local.entity.AccountEntity
import com.robson.financas.data.local.entity.CategoryEntity
import com.robson.financas.data.local.entity.GoalEntity
import com.robson.financas.data.local.entity.NotificationAppMappingEntity
import com.robson.financas.data.local.entity.TransactionEntity

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        NotificationAppMappingEntity::class,
        GoalEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun notificationAppMappingDao(): NotificationAppMappingDao
    abstract fun goalDao(): GoalDao
}
