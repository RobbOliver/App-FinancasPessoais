package com.robson.financas.ui.fiscal.importing

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.repository.fiscal.FiscalDocumentRepository
import com.robson.financas.domain.fiscal.model.FiscalImportResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed interface ImportUiState {
    data object Idle : ImportUiState
    data object Loading : ImportUiState
    data class Success(val documentId: Long, val itemCount: Int, val needsAttention: Boolean) : ImportUiState
    data class Duplicate(val existingDocumentId: Long) : ImportUiState
    data class Error(val message: String) : ImportUiState
}

@HiltViewModel
class ImportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: FiscalDocumentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ImportUiState>(ImportUiState.Idle)
    val uiState: StateFlow<ImportUiState> = _uiState

    fun importXmlFile(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { ImportUiState.Loading }
            val xml = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                }.getOrNull()
            }
            if (xml.isNullOrBlank()) {
                _uiState.update { ImportUiState.Error("Não foi possível ler o arquivo selecionado.") }
                return@launch
            }
            when (val result = repository.importFromXml(xml)) {
                is FiscalImportResult.Success -> _uiState.update {
                    ImportUiState.Success(result.documentId, result.itemCount, result.needsAttention)
                }
                is FiscalImportResult.Duplicate -> _uiState.update { ImportUiState.Duplicate(result.existingDocumentId) }
                is FiscalImportResult.Invalid -> _uiState.update { ImportUiState.Error(result.reason) }
            }
        }
    }

    fun consumeResult() {
        _uiState.update { ImportUiState.Idle }
    }
}
