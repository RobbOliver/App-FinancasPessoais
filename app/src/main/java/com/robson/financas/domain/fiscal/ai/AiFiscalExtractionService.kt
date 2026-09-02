package com.robson.financas.domain.fiscal.ai

import com.robson.financas.data.preferences.AiSettingsRepository
import com.robson.financas.domain.fiscal.model.ParsedFiscalDocument
import com.robson.financas.util.NetworkAvailabilityChecker
import javax.inject.Inject
import javax.inject.Singleton

/** Nunca lança exceção pra UI — mesmo espírito de `FiscalImportResult`. */
sealed interface AiExtractionResult {
    data class Success(val document: ParsedFiscalDocument) : AiExtractionResult
    data object NoNetwork : AiExtractionResult
    data object ApiKeyMissing : AiExtractionResult
    data class FetchFailed(val error: DanfceFetchError) : AiExtractionResult
    data class AiFailed(val error: AiExtractionError) : AiExtractionResult
}

/**
 * Orquestra a extração por IA: chave de acesso já validada localmente (feito antes, na tela
 * de resultado do QR) → checa rede → busca o HTML da DANFCe na URL bruta do QR → manda pra
 * OpenRouter → devolve um `ParsedFiscalDocument` pronto para `FiscalDocumentRepository
 * .importParsedDocument` (depois de o usuário revisar na tela de pré-visualização).
 */
@Singleton
class AiFiscalExtractionService @Inject constructor(
    private val networkChecker: NetworkAvailabilityChecker,
    private val htmlFetcher: DanfceHtmlFetcher,
    private val openRouterClient: OpenRouterExtractionClient,
    private val aiSettingsRepository: AiSettingsRepository,
) {
    suspend fun extractFromQrUrl(rawQrUrl: String): AiExtractionResult {
        if (!networkChecker.isConnected()) return AiExtractionResult.NoNetwork

        val apiKey = aiSettingsRepository.apiKey.value
        if (apiKey.isNullOrBlank()) return AiExtractionResult.ApiKeyMissing

        val html = htmlFetcher.fetch(rawQrUrl).getOrElse { throwable ->
            val error = (throwable as? DanfceFetchException)?.error ?: DanfceFetchError.NetworkError(throwable.message)
            return AiExtractionResult.FetchFailed(error)
        }

        val model = aiSettingsRepository.model.value
        val parsed = openRouterClient.extractItems(html, apiKey, model).getOrElse { throwable ->
            val error = (throwable as? AiExtractionException)?.error ?: AiExtractionError.NetworkError(throwable.message)
            return AiExtractionResult.AiFailed(error)
        }

        return AiExtractionResult.Success(parsed)
    }
}
