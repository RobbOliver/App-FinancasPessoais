package com.robson.financas.ui.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.robson.financas.ui.common.CurrencyInputField
import com.robson.financas.ui.theme.Spacing

@Composable
fun SetGoalDialog(
    categoryName: String,
    initialAmountCents: Long,
    hasExistingGoal: Boolean,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit,
    onRemove: () -> Unit,
) {
    var amountCents by remember { mutableLongStateOf(initialAmountCents) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Meta: $categoryName") },
        text = {
            CurrencyInputField(
                amountCents = amountCents,
                onAmountChange = { amountCents = it },
                label = "Valor da meta neste mês",
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(amountCents) }, enabled = amountCents > 0) {
                Text("Salvar")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                if (hasExistingGoal) {
                    TextButton(onClick = onRemove) { Text("Remover") }
                }
                TextButton(onClick = onDismiss) { Text("Cancelar") }
            }
        },
    )
}
