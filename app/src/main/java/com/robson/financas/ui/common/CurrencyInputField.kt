package com.robson.financas.ui.common

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.robson.financas.ui.designsystem.appTextFieldColors
import com.robson.financas.util.CurrencyFormatter

/**
 * Accepts raw digits and treats them as cents (e.g. typing "1234" means R$ 12,34),
 * which is how most Brazilian finance apps handle currency entry.
 */
@Composable
fun CurrencyInputField(
    amountCents: Long,
    onAmountChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Valor",
    isError: Boolean = false,
) {
    OutlinedTextField(
        value = amountCents.toString(),
        onValueChange = { input ->
            val cleaned = input.filter { it.isDigit() }
            val newCents = cleaned.trimStart('0').ifEmpty { "0" }.toLongOrNull() ?: 0L
            onAmountChange(newCents.coerceAtMost(9_999_999_999L))
        },
        label = { Text(label) },
        visualTransformation = CurrencyVisualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        isError = isError,
        colors = appTextFieldColors(),
        shape = MaterialTheme.shapes.medium,
        modifier = modifier,
    )
}

private object CurrencyVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val cents = text.text.toLongOrNull() ?: 0L
        val formatted = CurrencyFormatter.formatCents(cents)
        return TransformedText(
            AnnotatedString(formatted),
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int) = formatted.length
                override fun transformedToOriginal(offset: Int) = text.text.length
            },
        )
    }
}
