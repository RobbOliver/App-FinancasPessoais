package com.robson.financas.ui.objectives

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.local.entity.SavingsGoalContributionEntity
import com.robson.financas.data.local.relation.SavingsGoalProgress
import com.robson.financas.data.repository.SavingsGoalRepository
import com.robson.financas.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ObjectiveDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val savingsGoalRepository: SavingsGoalRepository,
) : ViewModel() {

    private val objectiveId: Long = checkNotNull(savedStateHandle[Screen.ObjectiveDetail.ARG_OBJECTIVE_ID])

    val progress: StateFlow<SavingsGoalProgress?> = savingsGoalRepository
        .observeAllWithProgress()
        .map { list -> list.find { it.goal.id == objectiveId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val contributions: StateFlow<List<SavingsGoalContributionEntity>> = savingsGoalRepository
        .observeContributions(objectiveId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addContribution(amountCents: Long, note: String) {
        viewModelScope.launch {
            savingsGoalRepository.addContribution(
                SavingsGoalContributionEntity(
                    goalId = objectiveId,
                    amountCents = amountCents,
                    date = LocalDate.now(),
                    note = note.trim(),
                ),
            )
        }
    }

    fun deleteContribution(contribution: SavingsGoalContributionEntity) {
        viewModelScope.launch { savingsGoalRepository.deleteContribution(contribution) }
    }
}
