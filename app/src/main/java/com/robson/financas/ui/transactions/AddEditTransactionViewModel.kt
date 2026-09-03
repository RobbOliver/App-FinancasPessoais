package com.robson.financas.ui.transactions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.local.entity.AccountEntity
import com.robson.financas.data.local.entity.CategoryEntity
import com.robson.financas.data.local.entity.CategoryType
import com.robson.financas.data.local.entity.TagEntity
import com.robson.financas.data.local.entity.TransactionEntity
import com.robson.financas.data.local.entity.TransactionRecurrence
import com.robson.financas.data.local.entity.TransactionType
import com.robson.financas.data.repository.AccountRepository
import com.robson.financas.data.repository.CategoryRepository
import com.robson.financas.data.repository.TagRepository
import com.robson.financas.data.repository.TransactionRepository
import com.robson.financas.data.repository.fiscal.FiscalDocumentRepository
import com.robson.financas.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AddEditTransactionUiState(
    val type: TransactionType = TransactionType.EXPENSE,
    val amountCents: Long = 0L,
    val accountId: Long? = null,
    val transferToAccountId: Long? = null,
    val categoryId: Long? = null,
    val date: LocalDate = LocalDate.now(),
    val description: String = "",
    val isPaid: Boolean = true,
    val isFavorite: Boolean = false,
    val isIgnored: Boolean = false,
    val attachmentPath: String? = null,
    val selectedTagIds: Set<Long> = emptySet(),
    val existingTransaction: TransactionEntity? = null,
    val isSaved: Boolean = false,
    val isRecurring: Boolean = false,
    val recurrenceFrequency: TransactionRecurrence? = TransactionRecurrence.MONTHLY,
    val recurrenceEndDate: LocalDate? = null,
) {
    val isEditing: Boolean get() = existingTransaction != null

    val isValid: Boolean
        get() = amountCents > 0 &&
            accountId != null &&
            (type != TransactionType.TRANSFER || (transferToAccountId != null && transferToAccountId != accountId))
}

@HiltViewModel
class AddEditTransactionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val tagRepository: TagRepository,
    private val fiscalDocumentRepository: FiscalDocumentRepository,
) : ViewModel() {

    private val transactionId: Long? =
        savedStateHandle.get<Long>(Screen.AddEditTransaction.ARG_TRANSACTION_ID)?.takeIf { it >= 0 }
    private val templateId: Long? =
        savedStateHandle.get<Long>(Screen.AddEditTransaction.ARG_TEMPLATE_ID)?.takeIf { it >= 0 }
    private val fiscalDocumentId: Long? =
        savedStateHandle.get<Long>(Screen.AddEditTransaction.ARG_FISCAL_DOCUMENT_ID)?.takeIf { it >= 0 }

    private val _uiState = MutableStateFlow(AddEditTransactionUiState())
    val uiState: StateFlow<AddEditTransactionUiState> = _uiState

    val accounts: StateFlow<List<AccountEntity>> = accountRepository
        .observeActiveAccountsWithBalance()
        .map { list -> list.map { it.account } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository
        .observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTags: StateFlow<List<TagEntity>> = tagRepository
        .observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        transactionId?.let { id ->
            viewModelScope.launch {
                transactionRepository.getById(id)?.let { transaction ->
                    _uiState.update {
                        it.copy(
                            type = transaction.type,
                            amountCents = transaction.amountCents,
                            accountId = transaction.accountId,
                            transferToAccountId = transaction.transferToAccountId,
                            categoryId = transaction.categoryId,
                            date = transaction.date,
                            description = transaction.description,
                            isPaid = transaction.isPaid,
                            isFavorite = transaction.isFavorite,
                            isIgnored = transaction.isIgnored,
                            attachmentPath = transaction.attachmentPath,
                            existingTransaction = transaction,
                            isRecurring = transaction.isRecurring,
                            recurrenceFrequency = transaction.recurrenceFrequency ?: TransactionRecurrence.MONTHLY,
                            recurrenceEndDate = transaction.recurrenceEndDate,
                        )
                    }
                }
                val tagIds = tagRepository.observeTagsForTransaction(id).first().map { it.id }.toSet()
                _uiState.update { it.copy(selectedTagIds = tagIds) }
            }
        }
        if (transactionId == null) {
            templateId?.let { id ->
                viewModelScope.launch {
                    transactionRepository.getById(id)?.let { template ->
                        _uiState.update {
                            it.copy(
                                type = template.type,
                                amountCents = template.amountCents,
                                accountId = template.accountId,
                                transferToAccountId = template.transferToAccountId,
                                categoryId = template.categoryId,
                                description = template.description,
                            )
                        }
                    }
                    val tagIds = tagRepository.observeTagsForTransaction(id).first().map { it.id }.toSet()
                    _uiState.update { it.copy(selectedTagIds = tagIds) }
                }
            } ?: fiscalDocumentId?.let { id ->
                viewModelScope.launch {
                    val document = fiscalDocumentRepository.getById(id) ?: return@launch
                    val establishmentName = document.establishmentId?.let { fiscalDocumentRepository.getEstablishmentName(it) }
                    _uiState.update {
                        it.copy(
                            type = TransactionType.EXPENSE,
                            amountCents = document.totalCents,
                            date = document.issuedAt,
                            description = establishmentName ?: "Nota fiscal",
                        )
                    }
                }
            }
        }
    }

    fun updateType(type: TransactionType) = _uiState.update {
        it.copy(type = type, categoryId = null, transferToAccountId = null)
    }

    fun updateAmount(cents: Long) = _uiState.update { it.copy(amountCents = cents) }
    fun updateAccount(accountId: Long) = _uiState.update { it.copy(accountId = accountId) }
    fun updateTransferToAccount(accountId: Long) = _uiState.update { it.copy(transferToAccountId = accountId) }
    fun updateCategory(categoryId: Long?) = _uiState.update { it.copy(categoryId = categoryId) }
    fun updateDate(date: LocalDate) = _uiState.update { it.copy(date = date) }
    fun updateDescription(description: String) = _uiState.update { it.copy(description = description) }
    fun updateIsPaid(isPaid: Boolean) = _uiState.update { it.copy(isPaid = isPaid) }
    fun updateIsRecurring(value: Boolean) = _uiState.update { it.copy(isRecurring = value) }
    fun updateRecurrenceFrequency(freq: TransactionRecurrence) = _uiState.update { it.copy(recurrenceFrequency = freq) }
    fun updateRecurrenceEndDate(date: LocalDate?) = _uiState.update { it.copy(recurrenceEndDate = date) }
    fun toggleFavorite() = _uiState.update { it.copy(isFavorite = !it.isFavorite) }
    fun updateIsIgnored(isIgnored: Boolean) = _uiState.update { it.copy(isIgnored = isIgnored) }
    fun updateAttachmentPath(path: String?) = _uiState.update { it.copy(attachmentPath = path) }

    fun toggleTag(tagId: Long) = _uiState.update {
        val newIds = if (tagId in it.selectedTagIds) it.selectedTagIds - tagId else it.selectedTagIds + tagId
        it.copy(selectedTagIds = newIds)
    }

    fun categoryTypeFor(type: TransactionType): CategoryType? = when (type) {
        TransactionType.INCOME -> CategoryType.INCOME
        TransactionType.EXPENSE -> CategoryType.EXPENSE
        TransactionType.TRANSFER -> null
    }

    fun save() {
        val state = _uiState.value
        if (!state.isValid) return
        viewModelScope.launch {
            val entity = TransactionEntity(
                id = state.existingTransaction?.id ?: 0,
                type = state.type,
                amountCents = state.amountCents,
                accountId = state.accountId!!,
                transferToAccountId = if (state.type == TransactionType.TRANSFER) state.transferToAccountId else null,
                categoryId = if (state.type == TransactionType.TRANSFER) null else state.categoryId,
                date = state.date,
                description = state.description.trim(),
                createdAt = state.existingTransaction?.createdAt ?: java.time.Instant.now(),
                source = state.existingTransaction?.source ?: com.robson.financas.data.local.entity.TransactionSource.MANUAL,
                needsReview = false,
                counterpartyName = state.existingTransaction?.counterpartyName,
                rawNotificationText = state.existingTransaction?.rawNotificationText,
                isPaid = state.isPaid,
                isIgnored = state.isIgnored,
                isFavorite = state.isFavorite,
                attachmentPath = state.attachmentPath,
                isRecurring = state.isRecurring,
                recurrenceFrequency = if (state.isRecurring) state.recurrenceFrequency else null,
                recurrenceEndDate = if (state.isRecurring) state.recurrenceEndDate else null,
            )
            val savedId = if (state.existingTransaction != null) {
                transactionRepository.update(entity)
                entity.id
            } else {
                transactionRepository.create(entity)
            }
            tagRepository.setTagsForTransaction(savedId, state.selectedTagIds.toList())
            fiscalDocumentId?.let { fiscalDocumentRepository.linkTransaction(it, savedId) }
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
