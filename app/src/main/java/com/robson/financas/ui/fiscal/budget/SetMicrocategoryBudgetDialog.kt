package com.robson.financas.ui.fiscal.budget

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.robson.financas.ui.common.CurrencyInputField

@Composable
fun SetMicrocategoryBudgetDialog(
    microcategoryName: String,
    initialAmountCents: Long,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit,
) {
    var amountCents by remember { mutableLongStateOf(initialAmountCents) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Orçamento: $microcategoryName") },
        text = {
            CurrencyInputField(
                amountCents = amountCents,
                onAmountChange = { amountCents = it },
                label = "Limite mensal",
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(amountCents) }, enabled = amountCents > 0) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}
