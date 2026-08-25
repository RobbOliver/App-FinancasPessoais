package com.robson.financas.ui.objectives

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.local.relation.SavingsGoalProgress
import com.robson.financas.data.repository.SavingsGoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ObjectivesViewModel @Inject constructor(
    savingsGoalRepository: SavingsGoalRepository,
) : ViewModel() {

    val objectives: StateFlow<List<SavingsGoalProgress>> = savingsGoalRepository
        .observeAllWithProgress()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
