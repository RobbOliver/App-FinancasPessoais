package com.robson.financas.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.robson.financas.data.local.entity.TransactionType
import com.robson.financas.data.local.relation.TransactionWithDetails

@Composable
fun TransactionListItem(
    item: TransactionWithDetails,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onDeleteClick: (() -> Unit)? = null,
) {
    val transaction = item.transaction
    val title = when (transaction.type) {
        TransactionType.TRANSFER -> "${item.accountName} → ${item.transferToAccountName}"
        else -> transaction.description.ifBlank { item.categoryName ?: "Sem categoria" }
    }
    val subtitle = when (transaction.type) {
        TransactionType.TRANSFER -> "Transferência"
        else -> item.categoryName ?: "Sem categoria"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (transaction.type == TransactionType.TRANSFER) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            item.categoryColorHex?.let { ColorCatalog.toColor(it) } ?: MaterialTheme.colorScheme.surfaceVariant
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (transaction.type == TransactionType.TRANSFER) {
                    Icon(
                        Icons.AutoMirrored.Filled.CompareArrows,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                } else {
                    Icon(
                        imageVector = IconCatalog.resolve(item.categoryIcon),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
                if (transaction.needsReview) {
                    Text(
                        "Pendente de revisão",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            AmountText(amountCents = transaction.amountCents, type = transaction.type)
            if (onDeleteClick != null) {
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Filled.Delete, contentDescription = "Excluir")
                }
            }
        }
    }
}
