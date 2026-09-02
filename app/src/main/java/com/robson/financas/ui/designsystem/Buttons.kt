package com.robson.financas.ui.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.robson.financas.ui.theme.BorderSubtleStrong
import com.robson.financas.ui.theme.Spacing
import com.robson.financas.ui.theme.SurfaceElevatedHigh
import com.robson.financas.ui.theme.TextTertiary

/** CTA principal — accent azul cheio, com leve encolhimento no toque em vez de sombra/elevação. */
@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = SurfaceElevatedHigh,
            disabledContentColor = TextTertiary,
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
        contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.md),
        modifier = modifier.pressScale(interactionSource),
    ) {
        if (icon != null) {
            icon()
            Spacer(Modifier.width(Spacing.xs))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * Espaço mínimo que uma `LazyColumn` com `AppFab` flutuando por cima deve reservar no fim do
 * `contentPadding` — sem isso o FAB fica sobre (e esconde) o valor do último item da lista.
 */
val FabClearance = 72.dp

/** FAB — sempre accent azul cheio, consistente em todas as telas (nunca o container apagado do M3). */
@Composable
fun AppFab(
    onClick: () -> Unit,
    contentDescription: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = MaterialTheme.shapes.large,
        modifier = modifier,
    ) {
        Icon(icon, contentDescription = contentDescription)
    }
}

/** Ação secundária — borda fina, sem preenchimento, mesma linguagem de estados do primário. */
@Composable
fun AppOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, BorderSubtleStrong),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContentColor = TextTertiary,
        ),
        contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.md),
        modifier = modifier.pressScale(interactionSource),
    ) {
        if (icon != null) {
            icon()
            Spacer(Modifier.width(Spacing.xs))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}
