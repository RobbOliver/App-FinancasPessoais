package com.robson.financas.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Uma meta de gastos do mês — pode somar o gasto de várias categorias (ver [GoalCategoryCrossRef]). */
@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val yearMonth: Int,
    val name: String,
    val amountCents: Long,
)
