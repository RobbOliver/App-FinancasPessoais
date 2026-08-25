package com.robson.financas.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.robson.financas.data.local.dao.AccountDao
import com.robson.financas.data.local.dao.CategoryDao
import com.robson.financas.data.local.dao.CreditCardDao
import com.robson.financas.data.local.dao.GoalDao
import com.robson.financas.data.local.dao.NotificationAppMappingDao
import com.robson.financas.data.local.dao.SavingsGoalDao
import com.robson.financas.data.local.dao.TagDao
import com.robson.financas.data.local.dao.TransactionDao
import com.robson.financas.data.local.entity.AccountEntity
import com.robson.financas.data.local.entity.CategoryEntity
import com.robson.financas.data.local.entity.CreditCardEntity
import com.robson.financas.data.local.entity.CreditCardInvoiceEntity
import com.robson.financas.data.local.entity.CreditCardPurchaseEntity
import com.robson.financas.data.local.entity.GoalEntity
import com.robson.financas.data.local.entity.NotificationAppMappingEntity
import com.robson.financas.data.local.entity.SavingsGoalContributionEntity
import com.robson.financas.data.local.entity.SavingsGoalEntity
import com.robson.financas.data.local.entity.TagEntity
import com.robson.financas.data.local.entity.TransactionEntity
import com.robson.financas.data.local.entity.TransactionTagCrossRef

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        NotificationAppMappingEntity::class,
        GoalEntity::class,
        CreditCardEntity::class,
        CreditCardPurchaseEntity::class,
        CreditCardInvoiceEntity::class,
        TagEntity::class,
        TransactionTagCrossRef::class,
        SavingsGoalEntity::class,
        SavingsGoalContributionEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun notificationAppMappingDao(): NotificationAppMappingDao
    abstract fun goalDao(): GoalDao
    abstract fun creditCardDao(): CreditCardDao
    abstract fun tagDao(): TagDao
    abstract fun savingsGoalDao(): SavingsGoalDao
}
