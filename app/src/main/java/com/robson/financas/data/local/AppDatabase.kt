package com.robson.financas.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.robson.financas.data.local.dao.AccountDao
import com.robson.financas.data.local.dao.CategoryDao
import com.robson.financas.data.local.dao.TransactionDao
import com.robson.financas.data.local.entity.AccountEntity
import com.robson.financas.data.local.entity.CategoryEntity
import com.robson.financas.data.local.entity.TransactionEntity

@Database(
    entities = [AccountEntity::class, CategoryEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
}
