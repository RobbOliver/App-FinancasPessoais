package com.robson.financas.ui.fiscal.importing

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.robson.financas.data.repository.fiscal.FiscalDocumentRepository
import com.robson.financas.domain.fiscal.model.FiscalImportResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class PhotoImportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: FiscalDocumentRepository,
) : ViewModel() {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val _uiState = MutableStateFlow<ImportUiState>(ImportUiState.Idle)
    val uiState: StateFlow<ImportUiState> = _uiState

    fun importPhoto(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { ImportUiState.Loading }
            val recognizedText = runCatching {
                val image = InputImage.fromFilePath(context, uri)
                recognizer.process(image).await().text
            }.getOrNull()

            if (recognizedText.isNullOrBlank()) {
                _uiState.update { ImportUiState.Error("Não conseguimos ler texto nessa foto — tente uma imagem mais nítida.") }
                return@launch
            }

            when (val result = repository.importFromOcrText(recognizedText)) {
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
