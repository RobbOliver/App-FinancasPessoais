package com.robson.financas.ui.objectives

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.robson.financas.ui.common.ColorCatalog
import com.robson.financas.ui.common.GoalProgressBar
import com.robson.financas.ui.common.IconCatalog
import com.robson.financas.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectivesScreen(
    onBack: () -> Unit,
    onAddObjective: () -> Unit,
    onOpenObjective: (Long) -> Unit,
    viewModel: ObjectivesViewModel = hiltViewModel(),
) {
    val objectives by viewModel.objectives.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Objetivos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddObjective) {
                Icon(Icons.Filled.Add, contentDescription = "Novo objetivo")
            }
        },
    ) { innerPadding ->
        if (objectives.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Nenhum objetivo cadastrado.\nToque em + para criar o primeiro.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(objectives, key = { it.goal.id }) { progress ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenObjective(progress.goal.id) },
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(ColorCatalog.toColor(progress.goal.colorHex)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = IconCatalog.resolve(progress.goal.icon),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                                Text(
                                    progress.goal.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 12.dp),
                                )
                            }
                            GoalProgressBar(
                                goalCents = progress.goal.targetCents,
                                spentCents = progress.goal.targetCents - progress.savedCents,
                                modifier = Modifier.padding(top = 10.dp),
                            )
                            Text(
                                "${CurrencyFormatter.formatCents(progress.savedCents)} de ${CurrencyFormatter.formatCents(progress.goal.targetCents)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 6.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
