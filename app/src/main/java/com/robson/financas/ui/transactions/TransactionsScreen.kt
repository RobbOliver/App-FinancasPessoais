package com.robson.financas.ui.transactions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.robson.financas.data.local.entity.TransactionEntity
import com.robson.financas.data.local.relation.TransactionWithDetails
import com.robson.financas.ui.common.ConfirmDeleteDialog
import com.robson.financas.ui.common.TransactionListItem
import com.robson.financas.ui.designsystem.AppCard
import com.robson.financas.ui.designsystem.AppFab
import com.robson.financas.ui.designsystem.EmptyState
import com.robson.financas.ui.designsystem.FabClearance
import com.robson.financas.ui.designsystem.appTextFieldColors
import com.robson.financas.ui.theme.HudCyan
import com.robson.financas.ui.theme.Spacing
import com.robson.financas.util.DateFormatter
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    onAddTransaction: () -> Unit,
    onEditTransaction: (Long) -> Unit,
    onUseAsTemplate: (Long) -> Unit,
    viewModel: TransactionsViewModel = hiltViewModel(),
) {
    val filter by viewModel.filter.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val scheduledTransactions by viewModel.scheduledTransactions.collectAsState()
    var pendingDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var scheduledPanelOpen by remember { mutableStateOf(false) }

    val grouped = remember(transactions) { transactions.groupBy { it.transaction.date } }
    val groupedEntries = remember(grouped) { grouped.entries.toList() }

    Scaffold(
        topBar = { TopAppBar(expandedHeight = 40.dp, title = { Text("Transações") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            AppFab(onClick = onAddTransaction, contentDescription = "Nova transação", icon = Icons.Filled.Add)
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            // ── Navegação por mês ────────────────────────────────────────────
            MonthNavigationBar(
                yearMonth = filter.selectedMonth,
                onPrevious = viewModel::previousMonth,
                onNext = viewModel::nextMonth,
            )

            // ── Filtros ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SimpleFilterDropdown(
                    label = "Conta",
                    selectedLabel = accounts.find { it.id == filter.accountId }?.name ?: "Todas",
                    options = listOf("Todas" to null) + accounts.map { it.name to it.id },
                    onSelected = viewModel::updateAccountFilter,
                    modifier = Modifier.weight(1f),
                )
                SimpleFilterDropdown(
                    label = "Categoria",
                    selectedLabel = categories.find { it.id == filter.categoryId }?.name ?: "Todas",
                    options = listOf("Todas" to null) + categories.map { it.name to it.id },
                    onSelected = viewModel::updateCategoryFilter,
                    modifier = Modifier.weight(1f),
                )
            }
            if (tags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    SimpleFilterDropdown(
                        label = "Tag",
                        selectedLabel = tags.find { it.id == filter.tagId }?.name ?: "Todas",
                        options = listOf("Todas" to null) + tags.map { it.name to it.id },
                        onSelected = viewModel::updateTagFilter,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = filter.onlyNeedsReview,
                    onClick = { viewModel.toggleNeedsReview(!filter.onlyNeedsReview) },
                    label = { Text("Pendências") },
                )
                FilterChip(
                    selected = filter.onlyFavorite,
                    onClick = { viewModel.toggleFavorite(!filter.onlyFavorite) },
                    label = { Text("Favoritas") },
                )
            }

            // ── Painel cyberpunk de agendados ─────────────────────────────────
            AnimatedVisibility(
                visible = scheduledPanelOpen,
                enter = expandVertically(tween(400, easing = FastOutSlowInEasing)),
                exit = shrinkVertically(tween(300, easing = FastOutSlowInEasing)),
            ) {
                ScheduledPanel(
                    items = scheduledTransactions,
                    onClickItem = onEditTransaction,
                )
            }

            // ── Conteúdo principal ────────────────────────────────────────────
            if (transactions.isEmpty()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Botão AGENDADOS mesmo sem lista
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.lg, vertical = Spacing.xs),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ScheduledTabButton(
                            isOpen = scheduledPanelOpen,
                            count = scheduledTransactions.size,
                            onClick = { scheduledPanelOpen = !scheduledPanelOpen },
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(Spacing.lg),
                        contentAlignment = Alignment.Center,
                    ) {
                        EmptyState(
                            icon = Icons.Filled.Add,
                            title = "Nenhuma transação encontrada",
                            subtitle = "Ajuste os filtros ou lance uma nova transação.",
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = Spacing.lg,
                        top = Spacing.sm,
                        end = Spacing.lg,
                        bottom = Spacing.sm + FabClearance,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    groupedEntries.forEachIndexed { dateIndex, (date, dateItems) ->
                        item(key = "header_$date") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        top = if (dateIndex == 0) Spacing.sm else Spacing.lg,
                                        bottom = Spacing.xs,
                                    ),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = DateFormatter.formatDayMonth(date),
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                // Botão "AGENDADOS" só aparece no topo (primeiro grupo de datas)
                                if (dateIndex == 0) {
                                    ScheduledTabButton(
                                        isOpen = scheduledPanelOpen,
                                        count = scheduledTransactions.size,
                                        onClick = { scheduledPanelOpen = !scheduledPanelOpen },
                                    )
                                }
                            }
                        }
                        items(dateItems, key = { it.transaction.id }) { item ->
                            AppCard(
                                modifier = Modifier.fillMaxWidth().animateItem(),
                                contentPadding = PaddingValues(0.dp),
                            ) {
                                TransactionListItem(
                                    item = item,
                                    onClick = { onEditTransaction(item.transaction.id) },
                                    onDeleteClick = { pendingDelete = item.transaction },
                                    onTogglePaid = { viewModel.togglePaid(item.transaction) },
                                    onUseAsTemplate = { onUseAsTemplate(item.transaction.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { transaction ->
        ConfirmDeleteDialog(
            title = "Excluir transação",
            message = "Tem certeza que deseja excluir esta transação?",
            onConfirm = {
                viewModel.deleteTransaction(transaction)
                pendingDelete = null
                coroutineScope.launch { snackbarHostState.showSnackbar("Transação excluída") }
            },
            onDismiss = { pendingDelete = null },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Botão "AGENDADOS" — fica na linha da data do topo da lista
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ScheduledTabButton(
    isOpen: Boolean,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgAlpha = if (isOpen) 1f else 0.75f
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(HudCyan.copy(alpha = bgAlpha))
            .border(1.dp, HudCyan, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "AGENDADOS",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Black,
        )
        if (count > 0) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.22f))
                    .padding(horizontal = 4.dp, vertical = 1.dp),
            ) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Painel de agendados — abre acima da lista com animação de raios
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ScheduledPanel(
    items: List<TransactionWithDetails>,
    onClickItem: (Long) -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "lightning")
    val lightningProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "lightningProgress",
    )

    val grouped = remember(items) {
        items.sortedByDescending { it.transaction.date }.groupBy { it.transaction.date }
    }
    val totalLazyItems = remember(grouped) { grouped.values.sumOf { it.size + 1 } }
    val listState = rememberLazyListState()
    LaunchedEffect(totalLazyItems) {
        if (totalLazyItems > 0) listState.scrollToItem(totalLazyItems - 1)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(228.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .border(
                width = 1.5.dp,
                color = HudCyan.copy(alpha = 0.6f),
                shape = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp),
            ),
    ) {
        // Raios animados na borda superior
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp),
        ) {
            val segments = 12
            val segW = size.width / segments
            var prevY = size.height / 2f
            for (i in 0 until segments) {
                val x1 = i * segW
                val x2 = (i + 1) * segW
                val wave = sin((lightningProgress + i * 0.13f) * PI.toFloat() * 2)
                val spike = sin((lightningProgress * 3.7f + i * 0.4f) * PI.toFloat() * 2)
                val nextY = (size.height / 2f + wave * size.height * 1.2f + spike * size.height * 0.8f)
                    .coerceIn(0f, size.height)
                val alpha = 0.5f + 0.5f * ((wave + 1f) / 2f)
                drawLine(
                    color = HudCyan.copy(alpha = alpha),
                    start = Offset(x1, prevY),
                    end = Offset(x2, nextY),
                    strokeWidth = 2.dp.toPx(),
                )
                // raio secundário mais fino
                drawLine(
                    color = HudCyan.copy(alpha = alpha * 0.35f),
                    start = Offset(x1, prevY + 1.dp.toPx()),
                    end = Offset(x2, nextY - 1.dp.toPx()),
                    strokeWidth = 1.dp.toPx(),
                )
                prevY = nextY
            }
        }

        if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Nenhuma transação agendada",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.xs),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                grouped.entries.toList().forEachIndexed { dateIndex, (date, dateItems) ->
                    item(key = "sched_header_$date") {
                        Text(
                            text = DateFormatter.formatDayMonth(date),
                            style = MaterialTheme.typography.titleSmall,
                            color = HudCyan,
                            modifier = Modifier.padding(
                                top = if (dateIndex == 0) Spacing.xs else Spacing.sm,
                                bottom = Spacing.xs,
                            ),
                        )
                    }
                    items(dateItems, key = { "sched_${it.transaction.id}" }) { item ->
                        // Card "invertido": fundo cyan tintado em vez do cinza escuro padrão
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .background(HudCyan.copy(alpha = 0.10f))
                                .border(
                                    width = 1.dp,
                                    color = HudCyan.copy(alpha = 0.35f),
                                    shape = MaterialTheme.shapes.medium,
                                )
                                .clickable { onClickItem(item.transaction.id) },
                        ) {
                            TransactionListItem(
                                item = item,
                                onClick = { onClickItem(item.transaction.id) },
                                onDeleteClick = {},
                                onTogglePaid = {},
                                onUseAsTemplate = {},
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthNavigationBar(
    yearMonth: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val monthFormatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pt", "BR")) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.xs, vertical = 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Mês anterior")
        }
        Text(
            text = yearMonth.format(monthFormatter).replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleMedium,
        )
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Próximo mês")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleFilterDropdown(
    label: String,
    selectedLabel: String,
    options: List<Pair<String, Long?>>,
    onSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = appTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (optionLabel, id) ->
                DropdownMenuItem(
                    text = { Text(optionLabel) },
                    onClick = {
                        onSelected(id)
                        expanded = false
                    },
                )
            }
        }
    }
}
