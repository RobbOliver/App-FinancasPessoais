package com.robson.financas.ui.fiscal.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.robson.financas.data.local.entity.fiscal.ClassificationStatus
import com.robson.financas.ui.theme.AccentMutedSurface
import com.robson.financas.ui.theme.GreenIncome
import com.robson.financas.ui.theme.HudCyan
import com.robson.financas.ui.theme.HudCyanLight
import com.robson.financas.ui.theme.RedExpense

/** "automático" / "sugerido" / "confirmar" / "a revisar" — nunca inferido, sempre o status gravado. */
@Composable
fun ClassificationStatusPill(status: ClassificationStatus, modifier: Modifier = Modifier) {
    val (label, color) = when (status) {
        ClassificationStatus.AUTOMATIC -> "Automático" to GreenIncome
        ClassificationStatus.CONFIRMED -> "Confirmado" to GreenIncome
        ClassificationStatus.SUGGESTED -> "Sugerido" to HudCyanLight
        ClassificationStatus.NEEDS_CONFIRMATION -> "Confirmar" to HudCyan
        ClassificationStatus.NEEDS_REVIEW -> "A revisar" to RedExpense
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(100))
            .background(AccentMutedSurface)
            .padding(horizontal = 9.dp, vertical = 3.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}
