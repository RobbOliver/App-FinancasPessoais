package com.robson.financas.data.preferences

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Modelo padrão sugerido — o usuário pode trocar livremente em Configurações. */
const val DEFAULT_OPENROUTER_MODEL = "google/gemini-2.5-flash"

private const val PREFS_FILE_NAME = "ai_settings_encrypted"
private const val KEY_API_KEY = "openrouter_api_key"
private const val KEY_MODEL = "openrouter_model"

/**
 * Guarda a chave de API da OpenRouter e o modelo escolhido em [EncryptedSharedPreferences] —
 * é a única credencial de terceiros que o app passa a reter, então fica cifrada em repouso
 * (diferente do resto do banco, que é local e sem segredo nenhum).
 */
@Singleton
class AiSettingsRepository @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS_FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    private val _apiKey = MutableStateFlow(prefs.getString(KEY_API_KEY, null))
    val apiKey: StateFlow<String?> = _apiKey.asStateFlow()

    private val _model = MutableStateFlow(prefs.getString(KEY_MODEL, null) ?: DEFAULT_OPENROUTER_MODEL)
    val model: StateFlow<String> = _model.asStateFlow()

    fun setApiKey(value: String?) {
        val trimmed = value?.trim()?.takeIf { it.isNotEmpty() }
        prefs.edit().putString(KEY_API_KEY, trimmed).apply()
        _apiKey.value = trimmed
    }

    fun setModel(value: String) {
        val trimmed = value.trim().ifEmpty { DEFAULT_OPENROUTER_MODEL }
        prefs.edit().putString(KEY_MODEL, trimmed).apply()
        _model.value = trimmed
    }

    fun hasApiKey(): Boolean = !_apiKey.value.isNullOrBlank()
}
