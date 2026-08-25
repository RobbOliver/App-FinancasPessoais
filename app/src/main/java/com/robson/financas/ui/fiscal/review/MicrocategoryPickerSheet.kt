package com.robson.financas.ui.fiscal.review

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.robson.financas.domain.fiscal.model.MicrocategoryOption
import com.robson.financas.ui.designsystem.AppTextField
import com.robson.financas.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MicrocategoryPickerSheet(
    options: List<MicrocategoryOption>,
    onDismiss: () -> Unit,
    onPick: (microcategoryId: Long, createRule: Boolean) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var createRule by remember { mutableStateOf(true) }

    val filtered = remember(query, options) {
        if (query.isBlank()) {
            options
        } else {
            options.filter {
                it.name.contains(query, ignoreCase = true) ||
                    it.subcategoryName.contains(query, ignoreCase = true) ||
                    it.categoryName.contains(query, ignoreCase = true)
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = Spacing.sm),
            ) {
                Checkbox(checked = createRule, onCheckedChange = { createRule = it })
                Text(
                    "Aplicar a próximas compras com esta mesma descrição",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                contentPadding = PaddingValues(vertical = Spacing.sm),
            ) {
                items(filtered, key = { it.microcategoryId }) { option ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(option.microcategoryId, createRule) }
                            .padding(vertical = Spacing.sm),
                    ) {
                        Text(option.name, style = MaterialTheme.typography.bodyLarge)
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
