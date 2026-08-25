package com.robson.financas.data.local

import androidx.room.TypeConverter
import com.robson.financas.data.local.entity.AccountType
import com.robson.financas.data.local.entity.CategoryType
import com.robson.financas.data.local.entity.TransactionSource
import com.robson.financas.data.local.entity.TransactionType
import com.robson.financas.data.local.entity.fiscal.ClassificationSource
import com.robson.financas.data.local.entity.fiscal.ClassificationStatus
import com.robson.financas.data.local.entity.fiscal.DocumentStatus
import com.robson.financas.data.local.entity.fiscal.FiscalDocumentSource
import com.robson.financas.data.local.entity.fiscal.MatchType
import com.robson.financas.data.local.entity.fiscal.RecurrenceFrequency
import java.time.Instant
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromEpochDay(value: Long?): LocalDate? = value?.let { LocalDate.ofEpochDay(it) }

    @TypeConverter
    fun localDateToEpochDay(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun fromEpochMilli(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }

    @TypeConverter
    fun instantToEpochMilli(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun toAccountType(value: String?): AccountType? = value?.let { AccountType.valueOf(it) }

    @TypeConverter
    fun fromAccountType(value: AccountType?): String? = value?.name

    @TypeConverter
    fun toCategoryType(value: String?): CategoryType? = value?.let { CategoryType.valueOf(it) }

    @TypeConverter
    fun fromCategoryType(value: CategoryType?): String? = value?.name

    @TypeConverter
    fun toTransactionType(value: String?): TransactionType? = value?.let { TransactionType.valueOf(it) }

    @TypeConverter
    fun fromTransactionType(value: TransactionType?): String? = value?.name

    @TypeConverter
    fun toTransactionSource(value: String?): TransactionSource? = value?.let { TransactionSource.valueOf(it) }

    @TypeConverter
    fun fromTransactionSource(value: TransactionSource?): String? = value?.name

    @TypeConverter
    fun toFiscalDocumentSource(value: String?): FiscalDocumentSource? = value?.let { FiscalDocumentSource.valueOf(it) }

    @TypeConverter
    fun fromFiscalDocumentSource(value: FiscalDocumentSource?): String? = value?.name

    @TypeConverter
    fun toDocumentStatus(value: String?): DocumentStatus? = value?.let { DocumentStatus.valueOf(it) }

    @TypeConverter
    fun fromDocumentStatus(value: DocumentStatus?): String? = value?.name

    @TypeConverter
    fun toClassificationSource(value: String?): ClassificationSource? = value?.let { ClassificationSource.valueOf(it) }

    @TypeConverter
    fun fromClassificationSource(value: ClassificationSource?): String? = value?.name

    @TypeConverter
    fun toClassificationStatus(value: String?): ClassificationStatus? = value?.let { ClassificationStatus.valueOf(it) }

    @TypeConverter
    fun fromClassificationStatus(value: ClassificationStatus?): String? = value?.name

    @TypeConverter
    fun toMatchType(value: String?): MatchType? = value?.let { MatchType.valueOf(it) }

    @TypeConverter
    fun fromMatchType(value: MatchType?): String? = value?.name

    @TypeConverter
    fun toRecurrenceFrequency(value: String?): RecurrenceFrequency? = value?.let { RecurrenceFrequency.valueOf(it) }

    @TypeConverter
    fun fromRecurrenceFrequency(value: RecurrenceFrequency?): String? = value?.name
}
