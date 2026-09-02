package com.robson.financas.ui.fiscal.scanning

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.preferences.AiSettingsRepository
import com.robson.financas.data.repository.fiscal.FiscalDocumentRepository
import com.robson.financas.data.repository.fiscal.ProductAliasRepository
import com.robson.financas.domain.fiscal.ai.AiExtractionResult
import com.robson.financas.domain.fiscal.ai.AiFiscalExtractionService
import com.robson.financas.domain.fiscal.ai.NormalizedProductName
import com.robson.financas.domain.fiscal.ai.OpenRouterExtractionClient
import com.robson.financas.domain.fiscal.model.FiscalImportResult
import com.robson.financas.domain.fiscal.model.ParsedFiscalDocument
import com.robson.financas.domain.fiscal.model.ParsedItem
import com.robson.financas.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.time.LocalDate
import javax.inject.Inject

data class EditableExtractedItem(
    val id: Int,
    val description: String,
    val quantity: String,
    val unit: String,
    val unitPriceCents: Long,
    val totalPriceCents: Long,
)

sealed interface AiExtractionUiState {
    data object Loading : AiExtractionUiState
    data object NoNetwork : AiExtractionUiState
    data object ApiKeyMissing : AiExtractionUiState
    data class Failed(val message: String) : AiExtractionUiState
    data class Preview(
        val issuerName: String?,
        val issuedAt: LocalDate,
        val totalCents: Long,
        val items: List<EditableExtractedItem>,
        val isSaving: Boolean = false,
    ) : AiExtractionUiState
    data class Saved(val documentId: Long) : AiExtractionUiState
}

/**
 * Roda a extração por IA (ver `AiFiscalExtractionService`) e deixa o resultado editável antes
 * de gravar — a extração não é uma fonte tão confiável quanto o XML estruturado, então nunca
 * grava direto: o usuário sempre confere e pode corrigir cada item antes do "Salvar".
 */
@HiltViewModel
class AiExtractionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val extractionService: AiFiscalExtractionService,
    private val repository: FiscalDocumentRepository,
    private val productAliasRepository: ProductAliasRepository,
    private val openRouterClient: OpenRouterExtractionClient,
    private val aiSettingsRepository: AiSettingsRepository,
) : ViewModel() {

    private val rawQr: String = URLDecoder.decode(
        checkNotNull(savedStateHandle[Screen.AiExtraction.ARG_RAW_QR]),
        "UTF-8",
    )

    private var baseDocument: ParsedFiscalDocument? = null
    private val _uiState = MutableStateFlow<AiExtractionUiState>(AiExtractionUiState.Loading)
    val uiState: StateFlow<AiExtractionUiState> = _uiState

    init {
        runExtraction()
    }

    fun retry() = runExtraction()

    private fun runExtraction() {
        _uiState.update { AiExtractionUiState.Loading }
        viewModelScope.launch {
            when (val result = extractionService.extractFromQrUrl(rawQr)) {
                is AiExtractionResult.Success -> {
                    val enriched = enrichWithCanonicalNames(result.document)
                    baseDocument = enriched
                    _uiState.update { enriched.toPreviewState() }
                }
                is AiExtractionResult.NoNetwork -> _uiState.update { AiExtractionUiState.NoNetwork }
                is AiExtractionResult.ApiKeyMissing -> _uiState.update { AiExtractionUiState.ApiKeyMissing }
                is AiExtractionResult.FetchFailed -> _uiState.update {
                    AiExtractionUiState.Failed("Não conseguimos buscar a nota no portal da Sefaz. Tente de novo ou importe o XML manualmente.")
                }
                is AiExtractionResult.AiFailed -> _uiState.update {
                    AiExtractionUiState.Failed("A IA não conseguiu ler os itens dessa nota. Tente de novo ou importe o XML manualmente.")
                }
            }
        }
    }

    /**
     * Resolve nome/marca canônicos pra cada descrição bruta distinta dos itens: primeiro no
     * cache local (`ProductAliasRepository`, sem rede), só chamando a IA — em um único lote,
     * nunca um item por vez — para as descrições ainda não vistas. Falha na normalização nunca
     * bloqueia a extração: os itens sem correspondência seguem sem `canonicalName` e caem no
     * palpite determinístico de sempre (`ItemNormalizer.normalize`) lá no repositório.
     */
    private suspend fun enrichWithCanonicalNames(document: ParsedFiscalDocument): ParsedFiscalDocument {
        val rawDescriptions = document.items.map { it.originalDescription }.distinct()
        if (rawDescriptions.isEmpty()) return document

        val cached = productAliasRepository.getCached(rawDescriptions)
        val missing = rawDescriptions.filterNot { cached.containsKey(it) }

        val resolved: Map<String, NormalizedProductName> = if (missing.isEmpty()) {
            emptyMap()
        } else {
            val apiKey = aiSettingsRepository.apiKey.value
            val model = aiSettingsRepository.model.value
            if (apiKey.isNullOrBlank()) {
                emptyMap()
            } else {
                openRouterClient.normalizeProductNames(missing, apiKey, model)
                    .onSuccess { productAliasRepository.saveResolved(it) }
                    .getOrElse { emptyMap() }
            }
        }

        val byDescription = cached.mapValues { (_, alias) -> NormalizedProductName(alias.canonicalName, alias.brand) } + resolved
        if (byDescription.isEmpty()) return document

        return document.copy(
            items = document.items.map { item ->
                byDescription[item.originalDescription]?.let { canonical ->
                    item.copy(canonicalName = canonical.canonicalName, canonicalBrand = canonical.brand)
                } ?: item
            },
        )
    }

    private fun ParsedFiscalDocument.toPreviewState() = AiExtractionUiState.Preview(
        issuerName = issuerName,
        issuedAt = issuedAt,
        totalCents = totalCents,
        items = items.mapIndexed { index, item ->
            EditableExtractedItem(
                id = index,
                description = item.originalDescription,
                quantity = item.quantity.toQuantityString(),
                unit = item.unit,
                unitPriceCents = item.unitPriceCents,
                totalPriceCents = item.totalPriceCents,
            )
        },
    )

    private fun updatePreview(transform: (AiExtractionUiState.Preview) -> AiExtractionUiState.Preview) {
        _uiState.update { current -> (current as? AiExtractionUiState.Preview)?.let(transform) ?: current }
    }

    fun updateItemDescription(id: Int, value: String) = updatePreview { state ->
        state.copy(items = state.items.map { if (it.id == id) it.copy(description = value) else it })
    }

    fun updateItemQuantity(id: Int, value: String) = updatePreview { state ->
        state.copy(items = state.items.map { if (it.id == id) it.copy(quantity = value) else it })
    }

    fun updateItemUnitPrice(id: Int, cents: Long) = updatePreview { state ->
        state.copy(items = state.items.map { if (it.id == id) it.copy(unitPriceCents = cents) else it })
    }

    fun updateItemTotalPrice(id: Int, cents: Long) = updatePreview { state ->
        state.copy(items = state.items.map { if (it.id == id) it.copy(totalPriceCents = cents) else it })
    }

    fun removeItem(id: Int) = updatePreview { state ->
        state.copy(items = state.items.filterNot { it.id == id })
    }

    fun updateTotal(cents: Long) = updatePreview { state -> state.copy(totalCents = cents) }

    fun save() {
        val state = _uiState.value as? AiExtractionUiState.Preview ?: return
        val base = baseDocument ?: return
        updatePreview { it.copy(isSaving = true) }

        viewModelScope.launch {
            val edited = base.copy(
                totalCents = state.totalCents,
                items = state.items.map { item ->
                    // Só reaproveita o nome canônico se a descrição não foi editada na revisão —
                    // senão o nome resolvido não corresponde mais ao que o usuário digitou.
                    val original = base.items.getOrNull(item.id)
                        ?.takeIf { it.originalDescription == item.description }
                    ParsedItem(
                        originalDescription = item.description,
                        gtin = null,
                        quantity = item.quantity.replace(',', '.').toDoubleOrNull() ?: 1.0,
                        unit = item.unit,
                        unitPriceCents = item.unitPriceCents,
                        totalPriceCents = item.totalPriceCents,
                        canonicalName = original?.canonicalName,
                        canonicalBrand = original?.canonicalBrand,
                    )
                },
            )
            when (val result = repository.importParsedDocument(edited)) {
                is FiscalImportResult.Success -> _uiState.update { AiExtractionUiState.Saved(result.documentId) }
                is FiscalImportResult.Duplicate -> _uiState.update { AiExtractionUiState.Saved(result.existingDocumentId) }
                is FiscalImportResult.Invalid -> _uiState.update { AiExtractionUiState.Failed(result.reason) }
            }
        }
    }

    private fun Double.toQuantityString(): String =
        if (this == this.toLong().toDouble()) this.toLong().toString() else this.toString()
}
