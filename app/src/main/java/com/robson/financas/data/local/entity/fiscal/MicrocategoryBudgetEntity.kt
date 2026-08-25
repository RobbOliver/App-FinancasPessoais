package com.robson.financas.data.local.entity.fiscal

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Orçamento mensal por microcategoria. [yearMonth] segue o padrão year*100+monthValue de GoalEntity. */
@Entity(
    tableName = "microcategory_budgets",
    foreignKeys = [
        ForeignKey(
            entity = MicrocategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["microcategoryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["microcategoryId", "yearMonth"], unique = true)],
)
data class MicrocategoryBudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val microcategoryId: Long,
    val yearMonth: Int,
    val limitCents: Long,
)
