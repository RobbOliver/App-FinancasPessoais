package com.robson.financas.ui.fiscal.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.local.relation.fiscal.PurchaseItemWithDetails
import com.robson.financas.data.repository.fiscal.FiscalDocumentRepository
import com.robson.financas.data.repository.fiscal.FiscalTaxonomyRepository
import com.robson.financas.domain.fiscal.model.MicrocategoryOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReviewUiState(
    val items: List<PurchaseItemWithDetails> = emptyList(),
    val microcategoryOptions: List<MicrocategoryOption> = emptyList(),
    val pickerForItemId: Long? = null,
)

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val repository: FiscalDocumentRepository,
    private val taxonomyRepository: FiscalTaxonomyRepository,
) : ViewModel() {

    private val microcategoryOptions = MutableStateFlow<List<MicrocategoryOption>>(emptyList())
    private val pickerForItemId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<ReviewUiState> = combine(
        repository.observeItemsNeedingReview(),
        microcategoryOptions,
        pickerForItemId,
    ) { items, options, pickerId ->
        ReviewUiState(items = items, microcategoryOptions = options, pickerForItemId = pickerId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReviewUiState())

    init {
        viewModelScope.launch {
            microcategoryOptions.value = taxonomyRepository.getMicrocategoryOptions()
        }
    }

    fun confirm(itemId: Long) {
        viewModelScope.launch { repository.confirmClassification(itemId) }
    }

    fun ignore(itemId: Long) {
        viewModelScope.launch { repository.ignoreItem(itemId) }
    }

    fun openPicker(itemId: Long) {
        pickerForItemId.update { itemId }
    }

    fun dismissPicker() {
        pickerForItemId.update { null }
    }

    fun correct(itemId: Long, microcategoryId: Long, createRule: Boolean) {
        viewModelScope.launch {
            repository.correctClassification(itemId, microcategoryId, createRule)
            pickerForItemId.update { null }
        }
    }
}
