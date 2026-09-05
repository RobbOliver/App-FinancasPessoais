package com.robson.financas.ui.accounts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.local.entity.TransactionEntity
import com.robson.financas.data.local.entity.TransactionSource
import com.robson.financas.data.local.entity.TransactionType
import com.robson.financas.data.local.relation.TransactionWithDetails
import com.robson.financas.data.repository.AccountRepository
import com.robson.financas.data.repository.TransactionRepository
import com.robson.financas.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/** Uma linha do extrato — saldo acumulado é null quando a transação ainda está pendente (não afeta o saldo). */
data class StatementRow(
    val item: TransactionWithDetails,
    val runningBalanceCents: Long?,
)

data class AccountStatementUiState(
    val accountName: String = "",
    val currentBalanceCents: Long = 0L,
    val rows: List<StatementRow> = emptyList(),
)

@HiltViewModel
class AccountStatementViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
) : ViewModel() {

    private val accountId: Long = checkNotNull(savedStateHandle[Screen.AccountStatement.ARG_ACCOUNT_ID])

    val uiState: StateFlow<AccountStatementUiState> = combine(
        accountRepository.observeById(accountId),
        transactionRepository.observeStatementForAccount(accountId),
    ) { account, transactions ->
        if (account == null) return@combine AccountStatementUiState()

        var running = account.initialBalanceCents
        val ascendingRows = transactions.map { item ->
            val t = item.transaction
            if (!t.isPaid) {
                StatementRow(item, null)
            } else {
                val delta = when {
                    t.type == TransactionType.INCOME && t.accountId == accountId -> t.amountCents
                    t.type == TransactionType.EXPENSE && t.accountId == accountId -> -t.amountCents
                    t.type == TransactionType.TRANSFER && t.accountId == accountId -> -t.amountCents
                    t.type == TransactionType.TRANSFER && t.transferToAccountId == accountId -> t.amountCents
                    else -> 0L
                }
                running += delta
                StatementRow(item, running)
            }
        }

        AccountStatementUiState(
            accountName = account.name,
            currentBalanceCents = running,
            rows = ascendingRows.asReversed(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AccountStatementUiState())

    /**
     * O usuário informa o saldo correto (ex.: conforme o extrato do banco) e a diferença em
     * relação ao saldo calculado vira um lançamento de ajuste — nunca sobrescreve nada
     * silenciosamente, sempre fica rastreável como uma transação normal.
     */
    fun adjustBalance(targetBalanceCents: Long) {
        val diff = targetBalanceCents - uiState.value.currentBalanceCents
        if (diff == 0L) return
        viewModelScope.launch {
            transactionRepository.create(
                TransactionEntity(
                    type = if (diff > 0) TransactionType.INCOME else TransactionType.EXPENSE,
                    amountCents = kotlin.math.abs(diff),
                    accountId = accountId,
                    categoryId = null,
                    date = LocalDate.now(),
                    description = "Ajuste de saldo",
                    source = TransactionSource.ADJUSTMENT,
                    isPaid = true,
                ),
            )
        }
    }
}
