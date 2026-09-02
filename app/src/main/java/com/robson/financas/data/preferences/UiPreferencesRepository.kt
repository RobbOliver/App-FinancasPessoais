package com.robson.financas.data.preferences

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS_FILE_NAME = "ui_preferences"
private const val KEY_HIDE_BALANCES = "hide_balances"

/** Preferências de UI puramente locais, sem dado sensível — por isso `SharedPreferences` normal, sem cifra. */
@Singleton
class UiPreferencesRepository @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)

    private val _hideBalances = MutableStateFlow(prefs.getBoolean(KEY_HIDE_BALANCES, false))
    val hideBalances: StateFlow<Boolean> = _hideBalances.asStateFlow()

    fun toggleHideBalances() {
        val newValue = !_hideBalances.value
        prefs.edit().putBoolean(KEY_HIDE_BALANCES, newValue).apply()
        _hideBalances.value = newValue
    }
}
