package com.robson.financas.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetCents: Long,
    val targetDate: LocalDate? = null,
    val colorHex: String,
    val icon: String,
    val isArchived: Boolean = false,
)
