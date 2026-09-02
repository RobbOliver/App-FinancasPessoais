package com.robson.financas.domain.fiscal.ai

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "DanfceHtmlFetcher"

/** Falha ao buscar a página pública da DANFCe no portal da Sefaz do estado emissor. */
sealed interface DanfceFetchError {
    data object Timeout : DanfceFetchError
    data class HttpError(val code: Int) : DanfceFetchError
    data class NetworkError(val message: String?) : DanfceFetchError
}

/**
 * Busca o HTML da DANFCe direto na URL que já vem embutida no conteúdo bruto do QR Code da
 * NFC-e — essa URL já inclui o hash de segurança exigido pelo portal, então não há formato a
 * adivinhar por estado (ver `NfceQrCodeParser` para o porquê da chave sozinha não bastar).
 */
@Singleton
class DanfceHtmlFetcher @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun fetch(url: String): Result<String> = withContext(Dispatchers.IO) {
        Log.i(TAG, "Buscando DANFCe em: $url")
        try {
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(TAG, "Portal respondeu HTTP ${response.code} para $url")
                    Result.failure(DanfceFetchException(DanfceFetchError.HttpError(response.code)))
                } else {
                    val body = response.body?.string()
                    if (body.isNullOrBlank()) {
                        Log.w(TAG, "Portal respondeu corpo vazio para $url")
                        Result.failure(DanfceFetchException(DanfceFetchError.NetworkError("Resposta vazia do portal.")))
                    } else {
                        Log.i(TAG, "HTML recebido (${body.length} chars)")
                        Result.success(body)
                    }
                }
            }
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Timeout ao buscar $url", e)
            Result.failure(DanfceFetchException(DanfceFetchError.Timeout))
        } catch (e: IOException) {
            Log.e(TAG, "Erro de rede ao buscar $url", e)
            Result.failure(DanfceFetchException(DanfceFetchError.NetworkError(e.message)))
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "URL inválida: $url", e)
            Result.failure(DanfceFetchException(DanfceFetchError.NetworkError("URL do QR Code inválida: ${e.message}")))
        }
    }
}

class DanfceFetchException(val error: DanfceFetchError) : Exception()
