package com.robson.financas.ui.fiscal.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.local.entity.fiscal.FiscalDocumentEntity
import com.robson.financas.data.repository.fiscal.FiscalDocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FiscalDocumentsViewModel @Inject constructor(
    repository: FiscalDocumentRepository,
) : ViewModel() {
    val documents: StateFlow<List<FiscalDocumentEntity>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
