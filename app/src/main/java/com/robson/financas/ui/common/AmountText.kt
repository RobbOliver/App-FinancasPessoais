package com.robson.financas.ui.common

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.robson.financas.data.local.entity.TransactionType
import com.robson.financas.ui.theme.GreenIncome
import com.robson.financas.ui.theme.RedExpense
import com.robson.financas.util.CurrencyFormatter

@Composable
fun AmountText(
    amountCents: Long,
    type: TransactionType,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
) {
    val color = when (type) {
        TransactionType.INCOME -> GreenIncome
        TransactionType.EXPENSE -> RedExpense
        TransactionType.TRANSFER -> LocalContentColor.current
    }
    val sign = when (type) {
        TransactionType.INCOME -> "+ "
        TransactionType.EXPENSE -> "- "
        TransactionType.TRANSFER -> ""
    }
    Text(
        text = sign + CurrencyFormatter.formatCents(amountCents),
        color = color,
        style = style,
        modifier = modifier,
    )
}
