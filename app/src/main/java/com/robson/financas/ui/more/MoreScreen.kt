package com.robson.financas.ui.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onNavigateToAccounts: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToTags: () -> Unit,
    onNavigateToObjectives: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Mais opções") }) },
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.fillMaxWidth().padding(innerPadding)) {
            item {
                Text(
                    "GERENCIAR",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
                )
            }
            item {
                MoreListItem(icon = Icons.Filled.AccountBalanceWallet, label = "Contas", onClick = onNavigateToAccounts)
            }
            item { HorizontalDivider() }
            item {
                MoreListItem(icon = Icons.Filled.Category, label = "Categorias", onClick = onNavigateToCategories)
            }
            item { HorizontalDivider() }
            item {
                MoreListItem(icon = Icons.Filled.Label, label = "Tags", onClick = onNavigateToTags)
            }
            item { HorizontalDivider() }
            item {
                MoreListItem(icon = Icons.Filled.TrackChanges, label = "Objetivos", onClick = onNavigateToObjectives)
            }
            item { HorizontalDivider() }
            item {
                MoreListItem(icon = Icons.Filled.Settings, label = "Configurações", onClick = onNavigateToSettings)
            }
            item { HorizontalDivider() }
        }
    }
}

@Composable
private fun MoreListItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.padding(end = 16.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
