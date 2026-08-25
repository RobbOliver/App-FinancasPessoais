package com.robson.financas.ui.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.robson.financas.ui.theme.BorderSubtle
import com.robson.financas.ui.theme.OnSurfaceGray
import com.robson.financas.ui.theme.SurfaceElevated
import com.robson.financas.ui.theme.SurfaceElevatedHigh
import com.robson.financas.ui.theme.SurfaceGray
import com.robson.financas.ui.theme.Spacing

/** Nível de profundidade — a hierarquia visual vem do contraste entre estes tons, não de sombra. */
enum class SurfaceLevel { Base, Elevated, ElevatedHigh }

/**
 * Substitui o `Card` cru do Material3 em todo o app: camada de superfície com o tom certo
 * para o nível pedido, borda fina de baixa opacidade em vez de elevação pesada, e a
 * microinteração de leve encolhimento no toque quando `onClick` é passado.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    level: SurfaceLevel = SurfaceLevel.Base,
    shape: Shape = MaterialTheme.shapes.medium,
    contentPadding: PaddingValues = PaddingValues(Spacing.lg),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val containerColor = when (level) {
        SurfaceLevel.Base -> SurfaceGray
        SurfaceLevel.Elevated -> SurfaceElevated
        SurfaceLevel.ElevatedHigh -> SurfaceElevatedHigh
    }
    val interactionSource = remember { MutableInteractionSource() }

    var base = modifier
        .clip(shape)
        .background(containerColor, shape)
        .border(1.dp, BorderSubtle, shape)

    if (onClick != null) {
        base = base
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick,
            )
            .pressScale(interactionSource)
    }

    Column(
        modifier = base.padding(contentPadding),
        content = {
            CompositionLocalProvider(LocalContentColor provides OnSurfaceGray) {
                content()
            }
        },
    )
}

/** Atalho para o card de destaque (hero) — mesmo componente, um degrau mais claro por padrão. */
@Composable
fun HeroCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    content: @Composable ColumnScope.() -> Unit,
) = AppCard(
    modifier = modifier,
    level = SurfaceLevel.Elevated,
    shape = shape,
    contentPadding = PaddingValues(Spacing.xl),
    content = content,
)
