package com.robson.financas.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.robson.financas.data.local.AppDatabase
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
import com.robson.financas.data.local.dao.fiscal.ProductDao
import com.robson.financas.data.local.dao.fiscal.PurchaseItemDao
import com.robson.financas.data.local.dao.fiscal.RecurringPatternDao
import com.robson.financas.data.local.dao.fiscal.UserClassificationRuleDao
import com.robson.financas.data.local.seed.DefaultCategorySeeder
import com.robson.financas.data.local.seed.fiscal.FiscalTaxonomySeeder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        databaseProvider: Provider<AppDatabase>,
    ): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "financas.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    CoroutineScope(Dispatchers.IO).launch {
                        val database = databaseProvider.get()
                        val categoryDao = database.categoryDao()
                        if (categoryDao.count() == 0) {
                            categoryDao.insertAll(DefaultCategorySeeder.buildDefaultCategories())
                        }
                        // Idempotente por si só — seguro chamar toda vez, cobre tanto instalação
                        // limpa quanto a chegada de novas microcategorias em uma atualização futura.
                        FiscalTaxonomySeeder.seed(categoryDao, database.microcategoryDao())
                    }
                }
            })
            .build()

    @Provides
    fun provideAccountDao(database: AppDatabase): AccountDao = database.accountDao()

    @Provides
    fun provideCategoryDao(database: AppDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideTransactionDao(database: AppDatabase): TransactionDao = database.transactionDao()

    @Provides
    fun provideNotificationAppMappingDao(database: AppDatabase): NotificationAppMappingDao =
        database.notificationAppMappingDao()

    @Provides
    fun provideGoalDao(database: AppDatabase): GoalDao = database.goalDao()

    @Provides
    fun provideCreditCardDao(database: AppDatabase): CreditCardDao = database.creditCardDao()

    @Provides
    fun provideTagDao(database: AppDatabase): TagDao = database.tagDao()

    @Provides
    fun provideSavingsGoalDao(database: AppDatabase): SavingsGoalDao = database.savingsGoalDao()

    @Provides
    fun provideFiscalDocumentDao(database: AppDatabase): FiscalDocumentDao = database.fiscalDocumentDao()

    @Provides
    fun provideEstablishmentDao(database: AppDatabase): EstablishmentDao = database.establishmentDao()

    @Provides
    fun provideProductDao(database: AppDatabase): ProductDao = database.productDao()

    @Provides
    fun provideMicrocategoryDao(database: AppDatabase): MicrocategoryDao = database.microcategoryDao()

    @Provides
    fun providePurchaseItemDao(database: AppDatabase): PurchaseItemDao = database.purchaseItemDao()

    @Provides
    fun provideUserClassificationRuleDao(database: AppDatabase): UserClassificationRuleDao =
        database.userClassificationRuleDao()

    @Provides
    fun provideClassificationHistoryDao(database: AppDatabase): ClassificationHistoryDao =
        database.classificationHistoryDao()

    @Provides
    fun providePriceHistoryDao(database: AppDatabase): PriceHistoryDao = database.priceHistoryDao()

    @Provides
    fun provideMicrocategoryBudgetDao(database: AppDatabase): MicrocategoryBudgetDao =
        database.microcategoryBudgetDao()

    @Provides
    fun provideRecurringPatternDao(database: AppDatabase): RecurringPatternDao = database.recurringPatternDao()

    @Provides
    fun provideFiscalAuditLogDao(database: AppDatabase): FiscalAuditLogDao = database.fiscalAuditLogDao()
}
