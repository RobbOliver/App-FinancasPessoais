package com.robson.financas.ui.designsystem

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.robson.financas.ui.theme.Spacing
import com.robson.financas.ui.theme.TextTertiary

/** Rótulo de seção em caixa alta — mesma peça visual do "GERENCIAR" do hub Mais, reutilizável. */
@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = TextTertiary,
        modifier = modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
    )
}
