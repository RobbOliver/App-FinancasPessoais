package com.robson.financas.ui.fiscal.review

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.robson.financas.domain.fiscal.model.ClassificationOption
import com.robson.financas.domain.fiscal.model.RuleScope
import com.robson.financas.ui.designsystem.AppTextField
import com.robson.financas.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MicrocategoryPickerSheet(
    options: List<ClassificationOption>,
    onDismiss: () -> Unit,
    onPick: (option: ClassificationOption, scope: RuleScope) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var scope by remember { mutableStateOf(RuleScope.THIS_PRODUCT) }

    val filtered = remember(query, options) {
        if (query.isBlank()) {
            options
        } else {
            options.filter {
                it.displayName.contains(query, ignoreCase = true) || it.categoryName.contains(query, ignoreCase = true)
            }
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = Spacing.lg)) {
            Text("Corrigir categoria", style = MaterialTheme.typography.titleMedium)

            AppTextField(
                value = query,
                onValueChange = { query = it },
                label = "Buscar",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.md),
            )

            Text(
                "Aplicar essa correção a",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = Spacing.md),
            )
            ScopeOptionRow(
                label = "Somente este item",
                selected = scope == RuleScope.NONE,
                onClick = { scope = RuleScope.NONE },
            )
            ScopeOptionRow(
                label = "Este produto em todas as compras futuras",
                selected = scope == RuleScope.THIS_PRODUCT,
                onClick = { scope = RuleScope.THIS_PRODUCT },
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                contentPadding = PaddingValues(vertical = Spacing.sm),
            ) {
                items(filtered, key = { optionKey(it) }) { option ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(option, scope) }
                            .padding(vertical = Spacing.sm),
                    ) {
                        Text(option.displayName, style = MaterialTheme.typography.bodyLarge)
                        if (option is ClassificationOption.Microcategory) {
                            Text(
                                "${option.categoryName} › ${option.subcategoryName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScopeOptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

private fun optionKey(option: ClassificationOption): String = when (option) {
    is ClassificationOption.Microcategory -> "micro-${option.microcategoryId}"
    is ClassificationOption.PlainCategory -> "plain-${option.categoryId}"
}
