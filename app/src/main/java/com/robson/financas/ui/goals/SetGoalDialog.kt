package com.robson.financas.ui.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.robson.financas.data.local.entity.CategoryEntity
import com.robson.financas.ui.common.ColorCatalog
import com.robson.financas.ui.common.CurrencyInputField
import com.robson.financas.ui.common.IconCatalog
import com.robson.financas.ui.designsystem.AppOutlinedButton
import com.robson.financas.ui.designsystem.AppPrimaryButton
import com.robson.financas.ui.designsystem.AppTextField
import com.robson.financas.ui.theme.RedExpense
import com.robson.financas.ui.theme.Spacing
import com.robson.financas.util.CurrencyFormatter
import kotlin.math.roundToLong

private enum class AllocationInputMode { VALUE, PERCENT }

/**
 * Folha de criação/edição de meta — nome + valor total + seleção de quais categorias de despesa
 * compõem essa meta, agrupadas por categoria pai/subcategoria (mesmo padrão visual da tela de
 * Categorias, com ícone e cor). Selecionar a categoria pai inclui o gasto de todas as suas
 * subcategorias no cálculo (ver GoalDao). Cada item marcado tem sua fatia do valor total, em R$
 * ou %. A soma das fatias precisa bater exatamente com o valor total pra poder salvar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetGoalDialog(
    goal: EditingGoal,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onAmountChange: (Long) -> Unit,
    onToggleCategory: (Long) -> Unit,
    onCategoryAllocationChange: (Long, Long) -> Unit,
    onSave: () -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    val parentGroups = remember(categories) {
        val childrenByParent = categories.filter { it.parentCategoryId != null }.groupBy { it.parentCategoryId }
        categories.filter { it.parentCategoryId == null }
            .sortedBy { it.name }
            .map { parent -> parent to childrenByParent[parent.id].orEmpty().sortedBy { it.name } }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm)) {
            Text(
                if (goal.id == null) "Nova meta" else "Editar meta",
                style = MaterialTheme.typography.titleMedium,
            )

            AppTextField(
                value = goal.name,
                onValueChange = onNameChange,
                label = "Nome da meta",
                placeholder = "Ex.: Lazer e compras",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.md),
            )

            CurrencyInputField(
                amountCents = goal.amountCents,
                onAmountChange = onAmountChange,
                label = "Valor da meta neste mês",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.sm),
            )

            Text(
                "Quais categorias fazem parte dessa meta e quanto cada uma pode gastar. " +
                    "Selecionar a categoria principal inclui todas as suas subcategorias.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = Spacing.lg, bottom = Spacing.xs),
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                contentPadding = PaddingValues(vertical = Spacing.xs),
            ) {
                items(parentGroups, key = { it.first.id }) { (parent, children) ->
                    Column(modifier = Modifier.padding(bottom = Spacing.sm)) {
                        CategoryAllocationRow(
                            category = parent,
                            isParent = true,
                            checked = parent.id in goal.selectedCategoryIds,
                            allocatedCents = goal.categoryAllocations[parent.id] ?: 0L,
                            totalCents = goal.amountCents,
                            onToggleChecked = { onToggleCategory(parent.id) },
                            onAllocationChange = { onCategoryAllocationChange(parent.id, it) },
                        )
                        children.forEach { child ->
                            CategoryAllocationRow(
                                category = child,
                                isParent = false,
                                checked = child.id in goal.selectedCategoryIds,
                                allocatedCents = goal.categoryAllocations[child.id] ?: 0L,
                                totalCents = goal.amountCents,
                                onToggleChecked = { onToggleCategory(child.id) },
                                onAllocationChange = { onCategoryAllocationChange(child.id, it) },
                            )
                        }
                    }
                }
            }

            val remaining = goal.remainingToAllocateCents
            if (goal.categoryAllocations.isNotEmpty() && remaining != 0L) {
                Text(
                    if (remaining > 0) {
                        "Falta alocar ${CurrencyFormatter.formatCents(remaining)}"
                    } else {
                        "Alocação excede o total em ${CurrencyFormatter.formatCents(-remaining)}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = RedExpense,
                    modifier = Modifier.padding(top = Spacing.xs),
                )
            }

            AppPrimaryButton(
                text = "Salvar",
                onClick = onSave,
                enabled = goal.isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.md),
            )
            if (goal.id != null && onDelete != null) {
                AppOutlinedButton(
                    text = "Excluir meta",
                    onClick = onDelete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.sm),
                )
            }
            Spacer(modifier = Modifier.height(Spacing.lg))
        }
    }
}

@Composable
private fun CategoryAllocationRow(
    category: CategoryEntity,
    isParent: Boolean,
    checked: Boolean,
    allocatedCents: Long,
    totalCents: Long,
    onToggleChecked: () -> Unit,
    onAllocationChange: (Long) -> Unit,
) {
    var mode by remember(category.id) { mutableStateOf(AllocationInputMode.VALUE) }
    val iconSize = if (isParent) 32.dp else 24.dp
    val iconInnerSize = if (isParent) 18.dp else 14.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isParent) 0.dp else 32.dp,
                top = Spacing.xs,
                bottom = Spacing.xs,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleChecked() },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = checked, onCheckedChange = { onToggleChecked() })
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .clip(CircleShape)
                    .background(ColorCatalog.toColor(category.colorHex)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = IconCatalog.resolve(category.icon),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(iconInnerSize),
                )
            }
            Text(
                category.name,
                style = if (isParent) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                color = if (isParent) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = Spacing.sm),
            )
        }

        if (checked) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = if (isParent) 72.dp else 104.dp, bottom = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.width(120.dp)) {
                    AllocationInputMode.entries.forEachIndexed { index, entry ->
                        SegmentedButton(
                            selected = mode == entry,
                            onClick = { mode = entry },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = AllocationInputMode.entries.size),
                            label = { Text(if (entry == AllocationInputMode.VALUE) "R$" else "%") },
                        )
                    }
                }
                Spacer(Modifier.width(Spacing.sm))
                if (mode == AllocationInputMode.PERCENT) {
                    val percentText = percentTextFor(allocatedCents, totalCents)
                    AppTextField(
                        value = percentText,
                        onValueChange = { input ->
                            val pct = input.replace(",", ".").toDoubleOrNull() ?: 0.0
                            onAllocationChange((totalCents * pct / 100.0).roundToLong().coerceAtLeast(0L))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.width(90.dp),
                    )
                } else {
                    CurrencyInputField(
                        amountCents = allocatedCents,
                        onAmountChange = onAllocationChange,
                        modifier = Modifier.width(150.dp),
                    )
                }
            }
        }
    }
}

private fun percentTextFor(allocatedCents: Long, totalCents: Long): String {
    if (totalCents <= 0) return "0"
    val percent = allocatedCents * 100.0 / totalCents
    val rounded = (percent * 10).roundToLong() / 10.0
    return if (rounded == rounded.toLong().toDouble()) rounded.toLong().toString() else rounded.toString()
}
