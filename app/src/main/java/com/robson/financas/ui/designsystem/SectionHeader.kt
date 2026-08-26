package com.robson.financas.ui.designsystem

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.robson.financas.ui.theme.EyebrowStyle
import com.robson.financas.ui.theme.Spacing

/**
 * Rótulo "eyebrow" do HUD — caixa alta, monoespaçada, ciano, prefixo `//`. Separador de seção
 * autônomo (com padding próprio); dentro de um `AppCard` use [CardLabel] (sem padding extra).
 */
@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier, prefix: String = "//") {
    Text(
        "$prefix ${text.uppercase()}",
        style = MaterialTheme.typography.labelMedium.merge(EyebrowStyle),
        modifier = modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
    )
}

/** Mesmo rótulo eyebrow de [SectionHeader], sem padding — para título de seção dentro de um card. */
@Composable
fun CardLabel(text: String, modifier: Modifier = Modifier, prefix: String = "//") {
    Text(
        "$prefix ${text.uppercase()}",
        style = MaterialTheme.typography.labelMedium.merge(EyebrowStyle),
        modifier = modifier,
    )
}
