package com.robson.financas.ui.fiscal.scanning

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.repository.fiscal.FiscalDocumentRepository
import com.robson.financas.domain.fiscal.AccessKeyValidator
import com.robson.financas.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface QrScanResultUiState {
    data object Loading : QrScanResultUiState
    data class Invalid(val accessKey: String) : QrScanResultUiState
    data class AlreadyImported(val documentId: Long) : QrScanResultUiState
    data class ValidNotYetImported(val accessKey: String) : QrScanResultUiState
}

@HiltViewModel
class QrScanResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: FiscalDocumentRepository,
) : ViewModel() {

    private val accessKey: String = checkNotNull(savedStateHandle[Screen.QrScanResult.ARG_ACCESS_KEY])
    private val _uiState = MutableStateFlow<QrScanResultUiState>(QrScanResultUiState.Loading)
    val uiState: StateFlow<QrScanResultUiState> = _uiState

    init {
        viewModelScope.launch {
            if (!AccessKeyValidator.isValid(accessKey)) {
                _uiState.update { QrScanResultUiState.Invalid(accessKey) }
                return@launch
            }
            val existingDoc = repository.findByAccessKey(accessKey)
            _uiState.update {
                if (existingDoc != null) {
                    QrScanResultUiState.AlreadyImported(existingDoc.id)
                } else {
                    QrScanResultUiState.ValidNotYetImported(accessKey)
                }
            }
        }
    }
}
