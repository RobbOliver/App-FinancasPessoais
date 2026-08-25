package com.robson.financas.ui.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.robson.financas.data.local.entity.AccountEntity
import com.robson.financas.data.local.relation.AccountWithBalance
import com.robson.financas.ui.common.ColorCatalog
import com.robson.financas.ui.common.ConfirmDeleteDialog
import com.robson.financas.ui.common.IconCatalog
import com.robson.financas.ui.common.label
import com.robson.financas.ui.designsystem.AppCard
import com.robson.financas.ui.designsystem.AppFab
import com.robson.financas.ui.designsystem.EmptyState
import com.robson.financas.ui.theme.GreenIncome
import com.robson.financas.ui.theme.RedExpense
import com.robson.financas.ui.theme.Spacing
import com.robson.financas.util.CurrencyFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    onBack: () -> Unit,
    onAddAccount: () -> Unit,
    onEditAccount: (Long) -> Unit,
    viewModel: AccountsViewModel = hiltViewModel(),
) {
    val accounts by viewModel.accounts.collectAsState()
    val deletionError by viewModel.deletionError.collectAsState()
    var pendingDelete by remember { mutableStateOf<AccountEntity?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            AppFab(onClick = onAddAccount, contentDescription = "Nova conta", icon = Icons.Filled.Add)
        },
    ) { innerPadding ->
        if (accounts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(Spacing.lg),
                contentAlignment = Alignment.Center,
            ) {
                EmptyState(
                    icon = Icons.Filled.Add,
                    title = "Nenhuma conta cadastrada",
                    subtitle = "Toque em + para criar a primeira.",
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                items(accounts, key = { it.account.id }) { item ->
                    AppCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp),
                        onClick = { onEditAccount(item.account.id) },
                    ) {
                        AccountRow(
                            item = item,
                            onDeleteClick = { pendingDelete = item.account },
                        )
                    }
                }
            }
        }
    }

    pendingDelete?.let { account ->
        ConfirmDeleteDialog(
            title = "Excluir conta",
            message = "Tem certeza que deseja excluir \"${account.name}\"?",
            onConfirm = {
                viewModel.deleteAccount(account) { success ->
                    if (success) {
                        coroutineScope.launch { snackbarHostState.showSnackbar("Conta excluída") }
                    }
                }
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }

    deletionError?.let { message ->
        ConfirmDeleteDialog(
            title = "Não foi possível excluir",
            message = message,
            onConfirm = { viewModel.clearDeletionError() },
            onDismiss = { viewModel.clearDeletionError() },
        )
    }
}

@Composable
private fun AccountRow(
    item: AccountWithBalance,
    onDeleteClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ColorCatalog.toColor(item.account.colorHex)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = IconCatalog.resolve(item.account.icon),
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.White,
                )
            }
            Column(modifier = Modifier.padding(start = Spacing.md)) {
                Text(item.account.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(item.account.type.label(), style = MaterialTheme.typography.bodySmall)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = CurrencyFormatter.formatCents(item.balanceCents),
                color = if (item.balanceCents >= 0) GreenIncome else RedExpense,
                style = MaterialTheme.typography.bodyLarge,
            )
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Filled.Delete, contentDescription = "Excluir")
            }
        }
    }
}
