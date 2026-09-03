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
import com.robson.financas.data.local.dao.fiscal.ClassificationHistoryDao
import com.robson.financas.data.local.dao.fiscal.EstablishmentDao
import com.robson.financas.data.local.dao.fiscal.FiscalAuditLogDao
import com.robson.financas.data.local.dao.fiscal.FiscalDocumentDao
import com.robson.financas.data.local.dao.fiscal.MicrocategoryBudgetDao
import com.robson.financas.data.local.dao.fiscal.MicrocategoryDao
import com.robson.financas.data.local.dao.fiscal.PriceHistoryDao
import com.robson.financas.data.local.dao.fiscal.ProductAliasDao
import com.robson.financas.data.local.dao.fiscal.ProductDao
import com.robson.financas.data.local.dao.fiscal.PurchaseItemDao
import com.robson.financas.data.local.dao.fiscal.RecurringPatternDao
import com.robson.financas.data.local.dao.fiscal.UserClassificationRuleDao
import com.robson.financas.data.local.entity.AccountEntity
import com.robson.financas.data.local.entity.CategoryEntity
import com.robson.financas.data.local.entity.CreditCardEntity
import com.robson.financas.data.local.entity.CreditCardInvoiceEntity
import com.robson.financas.data.local.entity.CreditCardPurchaseEntity
import com.robson.financas.data.local.entity.GoalCategoryCrossRef
import com.robson.financas.data.local.entity.GoalEntity
import com.robson.financas.data.local.entity.NotificationAppMappingEntity
import com.robson.financas.data.local.entity.SavingsGoalContributionEntity
import com.robson.financas.data.local.entity.SavingsGoalEntity
import com.robson.financas.data.local.entity.TagEntity
import com.robson.financas.data.local.entity.TransactionEntity
import com.robson.financas.data.local.entity.TransactionTagCrossRef
import com.robson.financas.data.local.entity.fiscal.ClassificationHistoryEntity
import com.robson.financas.data.local.entity.fiscal.EstablishmentEntity
import com.robson.financas.data.local.entity.fiscal.FiscalAuditLogEntity
import com.robson.financas.data.local.entity.fiscal.FiscalDocumentEntity
import com.robson.financas.data.local.entity.fiscal.MicrocategoryBudgetEntity
import com.robson.financas.data.local.entity.fiscal.MicrocategoryEntity
import com.robson.financas.data.local.entity.fiscal.PriceHistoryEntity
import com.robson.financas.data.local.entity.fiscal.ProductAliasEntity
import com.robson.financas.data.local.entity.fiscal.ProductEntity
import com.robson.financas.data.local.entity.fiscal.PurchaseItemEntity
import com.robson.financas.data.local.entity.fiscal.RecurringPatternEntity
import com.robson.financas.data.local.entity.fiscal.UserClassificationRuleEntity

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        NotificationAppMappingEntity::class,
        GoalEntity::class,
        GoalCategoryCrossRef::class,
        CreditCardEntity::class,
        CreditCardPurchaseEntity::class,
        CreditCardInvoiceEntity::class,
        TagEntity::class,
        TransactionTagCrossRef::class,
        SavingsGoalEntity::class,
        SavingsGoalContributionEntity::class,
        FiscalDocumentEntity::class,
        EstablishmentEntity::class,
        ProductEntity::class,
        MicrocategoryEntity::class,
        PurchaseItemEntity::class,
        UserClassificationRuleEntity::class,
        ClassificationHistoryEntity::class,
        PriceHistoryEntity::class,
        MicrocategoryBudgetEntity::class,
        RecurringPatternEntity::class,
        FiscalAuditLogEntity::class,
        ProductAliasEntity::class,
    ],
    version = 13,
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
    abstract fun fiscalDocumentDao(): FiscalDocumentDao
    abstract fun establishmentDao(): EstablishmentDao
    abstract fun productDao(): ProductDao
    abstract fun microcategoryDao(): MicrocategoryDao
    abstract fun purchaseItemDao(): PurchaseItemDao
    abstract fun userClassificationRuleDao(): UserClassificationRuleDao
    abstract fun classificationHistoryDao(): ClassificationHistoryDao
    abstract fun priceHistoryDao(): PriceHistoryDao
    abstract fun microcategoryBudgetDao(): MicrocategoryBudgetDao
    abstract fun recurringPatternDao(): RecurringPatternDao
    abstract fun fiscalAuditLogDao(): FiscalAuditLogDao
    abstract fun productAliasDao(): ProductAliasDao
}
