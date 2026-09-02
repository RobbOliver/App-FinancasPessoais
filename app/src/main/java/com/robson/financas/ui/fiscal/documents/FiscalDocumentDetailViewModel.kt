package com.robson.financas.ui.fiscal.documents

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.local.entity.fiscal.FiscalDocumentEntity
import com.robson.financas.data.local.relation.fiscal.PurchaseItemWithDetails
import com.robson.financas.data.repository.fiscal.FiscalDocumentRepository
import com.robson.financas.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FiscalDocumentDetailUiState(
    val document: FiscalDocumentEntity? = null,
    val items: List<PurchaseItemWithDetails> = emptyList(),
)

@HiltViewModel
class FiscalDocumentDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: FiscalDocumentRepository,
) : ViewModel() {

    private val documentId: Long = checkNotNull(savedStateHandle[Screen.FiscalDocumentDetail.ARG_DOCUMENT_ID])

    val uiState = combine(
        repository.observeById(documentId),
        repository.observeItems(documentId),
    ) { document, items ->
        FiscalDocumentDetailUiState(document, items)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FiscalDocumentDetailUiState())

    fun deleteDocument() {
        val document = uiState.value.document ?: return
        viewModelScope.launch { repository.deleteDocument(document) }
    }
}
