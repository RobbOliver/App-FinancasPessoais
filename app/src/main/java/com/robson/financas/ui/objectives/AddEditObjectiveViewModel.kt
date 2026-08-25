package com.robson.financas.ui.objectives

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.local.entity.SavingsGoalEntity
import com.robson.financas.data.repository.SavingsGoalRepository
import com.robson.financas.ui.common.ColorCatalog
import com.robson.financas.ui.common.IconCatalog
import com.robson.financas.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AddEditObjectiveUiState(
    val name: String = "",
    val targetCents: Long = 0L,
    val targetDate: LocalDate? = null,
    val colorHex: String = ColorCatalog.hexValues.first(),
    val icon: String = IconCatalog.defaultKey,
    val existingGoal: SavingsGoalEntity? = null,
    val isSaved: Boolean = false,
) {
    val isEditing: Boolean get() = existingGoal != null
    val isValid: Boolean get() = name.isNotBlank() && targetCents > 0
}

@HiltViewModel
class AddEditObjectiveViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val savingsGoalRepository: SavingsGoalRepository,
) : ViewModel() {

    private val objectiveId: Long? =
        savedStateHandle.get<Long>(Screen.AddEditObjective.ARG_OBJECTIVE_ID)?.takeIf { it >= 0 }

    private val _uiState = MutableStateFlow(AddEditObjectiveUiState())
    val uiState: StateFlow<AddEditObjectiveUiState> = _uiState

    init {
        objectiveId?.let { id ->
            viewModelScope.launch {
                savingsGoalRepository.getById(id)?.let { goal ->
                    _uiState.update {
                        it.copy(
                            name = goal.name,
                            targetCents = goal.targetCents,
                            targetDate = goal.targetDate,
                            colorHex = goal.colorHex,
                            icon = goal.icon,
                            existingGoal = goal,
                        )
                    }
                }
            }
        }
    }

    fun updateName(name: String) = _uiState.update { it.copy(name = name) }
    fun updateTargetCents(cents: Long) = _uiState.update { it.copy(targetCents = cents) }
    fun updateTargetDate(date: LocalDate?) = _uiState.update { it.copy(targetDate = date) }
    fun updateColor(hex: String) = _uiState.update { it.copy(colorHex = hex) }
    fun updateIcon(icon: String) = _uiState.update { it.copy(icon = icon) }

    fun save() {
        val state = _uiState.value
        if (!state.isValid) return
        viewModelScope.launch {
            val entity = SavingsGoalEntity(
                id = state.existingGoal?.id ?: 0,
                name = state.name.trim(),
                targetCents = state.targetCents,
                targetDate = state.targetDate,
                colorHex = state.colorHex,
                icon = state.icon,
                isArchived = state.existingGoal?.isArchived ?: false,
            )
            if (state.existingGoal != null) {
                savingsGoalRepository.update(entity)
            } else {
                savingsGoalRepository.create(entity)
            }
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
