package com.robson.financas.ui.transactions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.local.relation.TransactionWithDetails
import com.robson.financas.data.local.relation.fiscal.PurchaseItemWithDetails
import com.robson.financas.data.repository.TransactionRepository
import com.robson.financas.data.repository.fiscal.FiscalDocumentRepository
import com.robson.financas.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionDetailUiState(
    val transaction: TransactionWithDetails? = null,
    val fiscalDocumentId: Long? = null,
    val fiscalItems: List<PurchaseItemWithDetails> = emptyList(),
    val deleted: Boolean = false,
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    private val fiscalDocumentRepository: FiscalDocumentRepository,
) : ViewModel() {

    private val transactionId: Long = checkNotNull(savedStateHandle[Screen.TransactionDetail.ARG_TRANSACTION_ID])

    private val transactionFlow = transactionRepository.observeByIdWithDetails(transactionId)
    private val fiscalDocumentFlow = fiscalDocumentRepository.observeByLinkedTransactionId(transactionId)
    private val _deleted = MutableStateFlow(false)

    val uiState = combine(
        transactionFlow,
        fiscalDocumentFlow,
        fiscalDocumentFlow.flatMapLatest { document ->
            if (document == null) flowOf(emptyList()) else fiscalDocumentRepository.observeItems(document.id)
        },
        _deleted,
    ) { transaction, document, items, deleted ->
        TransactionDetailUiState(
            transaction = transaction,
            fiscalDocumentId = document?.id,
            fiscalItems = items,
            deleted = deleted,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TransactionDetailUiState())

    fun deleteTransaction() {
        val transaction = uiState.value.transaction?.transaction ?: return
        viewModelScope.launch {
            transactionRepository.delete(transaction)
            _deleted.value = true
        }
    }
}
