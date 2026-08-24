package com.robson.financas.ui.categories

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.local.entity.CategoryEntity
import com.robson.financas.data.local.entity.CategoryType
import com.robson.financas.data.repository.CategoryRepository
import com.robson.financas.ui.common.ColorCatalog
import com.robson.financas.ui.common.IconCatalog
import com.robson.financas.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditCategoryUiState(
    val name: String = "",
    val type: CategoryType = CategoryType.EXPENSE,
    val parentCategoryId: Long? = null,
    val colorHex: String = ColorCatalog.hexValues.first(),
    val icon: String = IconCatalog.defaultKey,
    val existingCategory: CategoryEntity? = null,
    val isSaved: Boolean = false,
) {
    val isEditing: Boolean get() = existingCategory != null
}

@HiltViewModel
class AddEditCategoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val categoryId: Long? =
        savedStateHandle.get<Long>(Screen.AddEditCategory.ARG_CATEGORY_ID)?.takeIf { it >= 0 }

    private val _uiState = MutableStateFlow(AddEditCategoryUiState())
    val uiState: StateFlow<AddEditCategoryUiState> = _uiState

    val allCategories: StateFlow<List<CategoryEntity>> = categoryRepository
        .observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        categoryId?.let { id ->
            viewModelScope.launch {
                categoryRepository.getById(id)?.let { category ->
                    _uiState.update {
                        it.copy(
                            name = category.name,
                            type = category.type,
                            parentCategoryId = category.parentCategoryId,
                            colorHex = category.colorHex,
                            icon = category.icon,
                            existingCategory = category,
                        )
                    }
                }
            }
        }
    }

    fun updateName(name: String) = _uiState.update { it.copy(name = name) }

    fun updateType(type: CategoryType) = _uiState.update {
        it.copy(type = type, parentCategoryId = null)
    }

    fun updateParent(parentCategoryId: Long?) = _uiState.update { it.copy(parentCategoryId = parentCategoryId) }
    fun updateColor(hex: String) = _uiState.update { it.copy(colorHex = hex) }
    fun updateIcon(icon: String) = _uiState.update { it.copy(icon = icon) }

    fun save() {
        val state = _uiState.value
        if (state.name.isBlank()) return
        viewModelScope.launch {
            val entity = CategoryEntity(
                id = state.existingCategory?.id ?: 0,
                name = state.name.trim(),
                type = state.type,
                parentCategoryId = state.parentCategoryId,
                icon = state.icon,
                colorHex = state.colorHex,
                isDefault = state.existingCategory?.isDefault ?: false,
            )
            if (state.existingCategory != null) {
                categoryRepository.update(entity)
            } else {
                categoryRepository.create(entity)
            }
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
