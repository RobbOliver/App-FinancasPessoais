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
import com.robson.financas.data.local.seed.DefaultCategorySeeder
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
                        val categoryDao = databaseProvider.get().categoryDao()
                        if (categoryDao.count() == 0) {
                            categoryDao.insertAll(DefaultCategorySeeder.buildDefaultCategories())
                        }
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
}
