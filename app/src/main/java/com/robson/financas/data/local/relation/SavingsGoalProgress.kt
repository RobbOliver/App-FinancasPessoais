package com.robson.financas.data.local.relation

import androidx.room.Embedded
import com.robson.financas.data.local.entity.SavingsGoalEntity

data class SavingsGoalProgress(
    @Embedded val goal: SavingsGoalEntity,
    val savedCents: Long,
) {
    val progressFraction: Float
        get() = if (goal.targetCents > 0) (savedCents.toFloat() / goal.targetCents).coerceIn(0f, 1f) else 0f
}
