package com.robson.financas.domain.fiscal.ai

import com.robson.financas.data.local.entity.fiscal.DocumentStatus
import com.robson.financas.data.local.entity.fiscal.FiscalDocumentSource
import com.robson.financas.domain.fiscal.model.ParsedFiscalDocument
import android.util.Log
import com.robson.financas.domain.fiscal.model.ParsedItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

sealed interface AiExtractionError {
    data object MissingApiKey : AiExtractionError
    data class HttpError(val code: Int, val body: String?) : AiExtractionError
    data class NetworkError(val message: String?) : AiExtractionError
    data class MalformedResponse(val reason: String) : AiExtractionError
}

class AiExtractionException(val error: AiExtractionError) : Exception()

/** Resultado da normalização de uma descrição bruta de nota — nome do produto pronto pra exibição + marca. */
data class NormalizedProductName(val canonicalName: String, val brand: String?)

private const val NORMALIZE_SYSTEM_PROMPT = """
Você recebe uma lista de descrições brutas e abreviadas de itens de cupom fiscal brasileiro
(supermercado, loja de conveniência etc.) e devolve APENAS um JSON (sem markdown, sem texto
ao redor) mapeando cada descrição bruta exatamente como veio pro nome canônico do produto e
sua marca:
{
  "DESCRICAO BRUTA EXATA 1": {"nome_canonico": "Nome legível do produto", "marca": "Marca"|null},
  "DESCRICAO BRUTA EXATA 2": {"nome_canonico": "...", "marca": "..."}
}
A chave de cada entrada deve ser IDÊNTICA (caractere por caractere) à descrição bruta
recebida — é usada depois como chave de cache. "marca" é null quando não há marca
identificável (produto genérico/a granel). Nunca invente item que não estava na lista.
"""

private const val TAG = "OpenRouterClient"
private const val OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions"
private const val OPENROUTER_KEY_CHECK_URL = "https://openrouter.ai/api/v1/auth/key"
private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

private const val SYSTEM_PROMPT = """
Você lê o HTML de uma página pública de consulta de NFC-e (Nota Fiscal de Consumidor
Eletrônica) de um portal da Sefaz brasileiro e devolve APENAS um JSON (sem markdown, sem
texto ao redor) com este formato exato:
{
  "estabelecimento": {"nome": string, "cnpj": string|null, "cidade": string|null, "uf": string|null},
  "data_emissao": "AAAA-MM-DD",
  "status": "autorizada" | "cancelada" | "desconhecido",
  "total": "123.45",
  "itens": [
    {"descricao": string, "quantidade": number, "unidade": string, "valor_unitario": "1.23", "valor_total": "1.23", "desconto": "0.00"}
  ]
}
Valores monetários sempre como string decimal com ponto (nunca vírgula, nunca número puro
para evitar erro de arredondamento). Nunca invente item que não está no HTML. Se não
encontrar um campo, use null (ou "0.00" para desconto).
"""

/**
 * Manda o HTML da DANFCe pra um modelo via OpenRouter (API compatível com OpenAI) e devolve
 * os itens já estruturados. Igual `NfeXmlParser`, nunca lança pra fora: erros viram
 * `Result.failure(AiExtractionException)`.
 */
@Singleton
class OpenRouterExtractionClient @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    /** Chamada mínima só pra confirmar que a chave é válida — usada pelo botão "Testar chave" em Configurações. */
    suspend fun validateApiKey(apiKey: String): Boolean {
        if (apiKey.isBlank()) return false
        val request = Request.Builder()
            .url(OPENROUTER_KEY_CHECK_URL)
            .addHeader("Authorization", "Bearer $apiKey")
            .get()
            .build()
        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { it.isSuccessful }
            } catch (e: IOException) {
                false
            }
        }
    }

    suspend fun extractItems(html: String, apiKey: String, model: String): Result<ParsedFiscalDocument> {
        if (apiKey.isBlank()) return Result.failure(AiExtractionException(AiExtractionError.MissingApiKey))

        val cleanedHtml = stripNoise(html).take(MAX_HTML_CHARS)

        val payload = JSONObject().apply {
            put("model", model)
            put(
                "messages",
                org.json.JSONArray().apply {
                    put(JSONObject().apply { put("role", "system"); put("content", SYSTEM_PROMPT) })
                    put(JSONObject().apply { put("role", "user"); put("content", cleanedHtml) })
                },
            )
            put("response_format", JSONObject().apply { put("type", "json_object") })
        }

        val request = Request.Builder()
            .url(OPENROUTER_URL)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        Log.i(TAG, "Chamando OpenRouter (model=$model, html=${cleanedHtml.length} chars)")
        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    val bodyString = response.body?.string()
                    if (!response.isSuccessful) {
                        Log.w(TAG, "OpenRouter respondeu HTTP ${response.code}: ${bodyString?.take(2000)}")
                        Result.failure(AiExtractionException(AiExtractionError.HttpError(response.code, bodyString)))
                    } else if (bodyString.isNullOrBlank()) {
                        Log.w(TAG, "OpenRouter respondeu corpo vazio")
                        Result.failure(AiExtractionException(AiExtractionError.MalformedResponse("Resposta vazia da OpenRouter.")))
                    } else {
                        Log.i(TAG, "OpenRouter respondeu OK (${bodyString.length} chars)")
                        parseCompletion(bodyString)
                    }
                }
            } catch (e: java.net.SocketTimeoutException) {
                Log.e(TAG, "Timeout ao chamar OpenRouter", e)
                Result.failure(AiExtractionException(AiExtractionError.NetworkError("Tempo esgotado ao falar com a IA.")))
            } catch (e: IOException) {
                Log.e(TAG, "Erro de rede ao chamar OpenRouter", e)
                Result.failure(AiExtractionException(AiExtractionError.NetworkError(e.message)))
            }
        }
    }

    /**
     * Normaliza um lote de descrições brutas em uma única chamada (nunca uma por item) — só
     * é chamada pelo `AiExtractionViewModel` para as descrições que ainda não estão no cache
     * local (`ProductAliasRepository`). Nunca lança: falha vira `Result.failure`, e quem chama
     * trata isso como "sem normalização disponível agora", sem travar a extração principal.
     */
    suspend fun normalizeProductNames(
        rawDescriptions: List<String>,
        apiKey: String,
        model: String,
    ): Result<Map<String, NormalizedProductName>> {
        if (apiKey.isBlank()) return Result.failure(AiExtractionException(AiExtractionError.MissingApiKey))
        if (rawDescriptions.isEmpty()) return Result.success(emptyMap())

        val userContent = org.json.JSONArray(rawDescriptions).toString()
        val payload = JSONObject().apply {
            put("model", model)
            put(
                "messages",
                org.json.JSONArray().apply {
                    put(JSONObject().apply { put("role", "system"); put("content", NORMALIZE_SYSTEM_PROMPT) })
                    put(JSONObject().apply { put("role", "user"); put("content", userContent) })
                },
            )
            put("response_format", JSONObject().apply { put("type", "json_object") })
        }

        val request = Request.Builder()
            .url(OPENROUTER_URL)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        Log.i(TAG, "Normalizando ${rawDescriptions.size} descrição(ões) via OpenRouter")
        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    val bodyString = response.body?.string()
                    if (!response.isSuccessful) {
                        Log.w(TAG, "OpenRouter (normalização) respondeu HTTP ${response.code}: ${bodyString?.take(2000)}")
                        Result.failure(AiExtractionException(AiExtractionError.HttpError(response.code, bodyString)))
                    } else if (bodyString.isNullOrBlank()) {
                        Result.failure(AiExtractionException(AiExtractionError.MalformedResponse("Resposta vazia da OpenRouter.")))
                    } else {
                        parseNormalizationCompletion(bodyString)
                    }
                }
            } catch (e: java.net.SocketTimeoutException) {
                Result.failure(AiExtractionException(AiExtractionError.NetworkError("Tempo esgotado ao normalizar nomes.")))
            } catch (e: IOException) {
                Result.failure(AiExtractionException(AiExtractionError.NetworkError(e.message)))
            }
        }
    }

    private fun parseNormalizationCompletion(responseBody: String): Result<Map<String, NormalizedProductName>> = try {
        val root = JSONObject(responseBody)
        val content = root.getJSONArray("choices").getJSONObject(0)
            .getJSONObject("message").getString("content")
        Result.success(parseNormalizedNames(content))
    } catch (e: org.json.JSONException) {
        Log.e(TAG, "Falha ao interpretar resposta de normalização: $responseBody", e)
        Result.failure(AiExtractionException(AiExtractionError.MalformedResponse(e.message ?: "JSON inesperado da OpenRouter.")))
    }

    /** Isolado para ser testável sem rede, igual `parseExtractedJson`. */
    fun parseNormalizedNames(content: String): Map<String, NormalizedProductName> {
        val json = JSONObject(extractJsonObject(content))
        val result = mutableMapOf<String, NormalizedProductName>()
        json.keys().forEach { rawDescription ->
            val entry = json.optJSONObject(rawDescription) ?: return@forEach
            val canonicalName = entry.optString("nome_canonico").trim()
            if (canonicalName.isNotBlank()) {
                val brand = entry.optString("marca").trim().takeIf { it.isNotBlank() && it != "null" }
                result[rawDescription] = NormalizedProductName(canonicalName, brand)
            }
        }
        return result
    }

    private fun parseCompletion(responseBody: String): Result<ParsedFiscalDocument> = try {
        val root = JSONObject(responseBody)
        val content = root.getJSONArray("choices").getJSONObject(0)
            .getJSONObject("message").getString("content")
        Log.i(TAG, "Conteúdo devolvido pelo modelo: ${content.take(2000)}")
        Result.success(parseExtractedJson(content))
    } catch (e: org.json.JSONException) {
        Log.e(TAG, "Falha ao interpretar resposta da OpenRouter: $responseBody", e)
        Result.failure(AiExtractionException(AiExtractionError.MalformedResponse(e.message ?: "JSON inesperado da OpenRouter.")))
    }

    /** Isolado para ser testável sem rede: recebe o texto que o modelo devolveu como `content`. */
    fun parseExtractedJson(content: String): ParsedFiscalDocument {
        val json = JSONObject(extractJsonObject(content))

        val estabelecimento = json.optJSONObject("estabelecimento")
        val itensJson = json.optJSONArray("itens") ?: org.json.JSONArray()

        val items = (0 until itensJson.length()).map { i ->
            val item = itensJson.getJSONObject(i)
            val unitPrice = item.optString("valor_unitario", "0").toCents()
            val totalPrice = item.optString("valor_total", "0").toCents()
            ParsedItem(
                originalDescription = item.optString("descricao").trim(),
                gtin = null,
                quantity = item.optDouble("quantidade", 1.0),
                unit = item.optString("unidade", "UN"),
                unitPriceCents = unitPrice,
                totalPriceCents = totalPrice,
                discountCents = item.optString("desconto", "0").toCents(),
            )
        }

        val status = when (json.optString("status").lowercase()) {
            "autorizada" -> DocumentStatus.AUTHORIZED
            "cancelada" -> DocumentStatus.CANCELLED
            else -> DocumentStatus.UNKNOWN
        }

        val issuedAt = json.optString("data_emissao").toLocalDateOrToday()

        return ParsedFiscalDocument(
            accessKey = null,
            source = FiscalDocumentSource.AI_QRCODE,
            issuerCnpj = estabelecimento?.optString("cnpj")?.takeIf { it.isNotBlank() && it != "null" },
            issuerName = estabelecimento?.optString("nome")?.takeIf { it.isNotBlank() && it != "null" },
            issuerCity = estabelecimento?.optString("cidade")?.takeIf { it.isNotBlank() && it != "null" },
            issuerState = estabelecimento?.optString("uf")?.takeIf { it.isNotBlank() && it != "null" },
            issuedAt = issuedAt,
            totalCents = json.optString("total", "0").toCents(),
            status = status,
            items = items,
            rawData = content,
        )
    }

    /** Modelos às vezes cercam o JSON com texto/markdown mesmo pedindo `json_object` — extrai o bloco `{...}` mais externo. */
    private fun extractJsonObject(content: String): String {
        val start = content.indexOf('{')
        val end = content.lastIndexOf('}')
        if (start == -1 || end == -1 || end < start) {
            throw org.json.JSONException("Resposta da IA não contém um objeto JSON.")
        }
        return content.substring(start, end + 1)
    }

    private fun stripNoise(html: String): String =
        html.replace(Regex("(?is)<script.*?</script>"), "")
            .replace(Regex("(?is)<style.*?</style>"), "")
            .replace(Regex("(?is)<!--.*?-->"), "")

    private fun String.toCents(): Long = try {
        BigDecimal(this.trim()).movePointRight(2).setScale(0, RoundingMode.HALF_UP).toLong()
    } catch (e: NumberFormatException) {
        0L
    }

    private fun String.toLocalDateOrToday(): LocalDate = try {
        LocalDate.parse(this.trim())
    } catch (e: java.time.format.DateTimeParseException) {
        LocalDate.now()
    }

    private companion object {
        /** ~40k caracteres é folga suficiente pra qualquer DANFCe real e mantém o custo de tokens baixo. */
        const val MAX_HTML_CHARS = 40_000
    }
}
