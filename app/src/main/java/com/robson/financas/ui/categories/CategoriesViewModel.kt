package com.robson.financas.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.local.entity.CategoryEntity
import com.robson.financas.data.repository.CategoryRepository
import com.robson.financas.data.repository.DeletionBlockedException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository
        .observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _deletionError = MutableStateFlow<String?>(null)
    val deletionError: StateFlow<String?> = _deletionError

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            try {
                categoryRepository.delete(category)
            } catch (e: DeletionBlockedException) {
                _deletionError.value = e.message
            }
        }
    }

    fun clearDeletionError() {
        _deletionError.value = null
    }
}
