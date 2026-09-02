package com.robson.financas.ui.fiscal.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.local.relation.fiscal.PurchaseItemWithDetails
import com.robson.financas.data.repository.fiscal.FiscalDocumentRepository
import com.robson.financas.data.repository.fiscal.FiscalTaxonomyRepository
import com.robson.financas.domain.fiscal.model.ClassificationOption
import com.robson.financas.domain.fiscal.model.RuleScope
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
    val classificationOptions: List<ClassificationOption> = emptyList(),
    val pickerForItemId: Long? = null,
    val productDialogForItemId: Long? = null,
)

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val repository: FiscalDocumentRepository,
    private val taxonomyRepository: FiscalTaxonomyRepository,
) : ViewModel() {

    private val classificationOptions = MutableStateFlow<List<ClassificationOption>>(emptyList())
    private val pickerForItemId = MutableStateFlow<Long?>(null)
    private val productDialogForItemId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<ReviewUiState> = combine(
        repository.observeItemsNeedingReview(),
        classificationOptions,
        pickerForItemId,
        productDialogForItemId,
    ) { items, options, pickerId, productDialogId ->
        ReviewUiState(items = items, classificationOptions = options, pickerForItemId = pickerId, productDialogForItemId = productDialogId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReviewUiState())

    init {
        viewModelScope.launch {
            classificationOptions.value = taxonomyRepository.getClassificationOptions()
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

    fun correct(itemId: Long, option: ClassificationOption, scope: RuleScope) {
        viewModelScope.launch {
            when (option) {
                is ClassificationOption.Microcategory ->
                    repository.correctClassification(itemId, microcategoryId = option.microcategoryId, plainCategoryId = null, scope = scope)
                is ClassificationOption.PlainCategory ->
                    repository.correctClassification(itemId, microcategoryId = null, plainCategoryId = option.categoryId, scope = scope)
            }
            pickerForItemId.update { null }
        }
    }

    fun openProductDialog(itemId: Long) {
        productDialogForItemId.update { itemId }
    }

    fun dismissProductDialog() {
        productDialogForItemId.update { null }
    }

    fun updateProductIdentity(itemId: Long, brand: String?, genericName: String) {
        viewModelScope.launch {
            repository.updateProductIdentity(itemId, brand, genericName)
            productDialogForItemId.update { null }
        }
    }
}
