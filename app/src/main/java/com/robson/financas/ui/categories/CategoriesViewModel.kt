package com.robson.financas.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.local.entity.CategoryEntity
import com.robson.financas.data.local.entity.CategoryType
import com.robson.financas.data.repository.CategoryRepository
import com.robson.financas.data.repository.DeletionBlockedException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** As três abas da tela de Categorias — "IA" reúne o que [com.robson.financas.data.local.seed.fiscal.FiscalTaxonomySeeder] gerencia. */
enum class CategoryTab { RECEITAS, DESPESAS, IA }

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    /** Lista completa, sem filtro de aba — usada para resolver o pai "de verdade" de uma categoria mesmo quando ele caiu em outra aba (ex.: virou IA). */
    val allCategories: StateFlow<List<CategoryEntity>> = categoryRepository
        .observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTab = MutableStateFlow(CategoryTab.DESPESAS)
    val selectedTab: StateFlow<CategoryTab> = _selectedTab

    val categories: StateFlow<List<CategoryEntity>> = combine(allCategories, _selectedTab) { all, tab ->
        when (tab) {
            CategoryTab.RECEITAS -> all.filter { it.type == CategoryType.INCOME }
            CategoryTab.DESPESAS -> all.filter { it.type == CategoryType.EXPENSE && !it.isAiTaxonomy }
            CategoryTab.IA -> all.filter { it.type == CategoryType.EXPENSE && it.isAiTaxonomy }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _deletionError = MutableStateFlow<String?>(null)
    val deletionError: StateFlow<String?> = _deletionError

    fun selectTab(tab: CategoryTab) {
        _selectedTab.value = tab
    }

    fun deleteCategory(category: CategoryEntity, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                categoryRepository.delete(category)
                onResult(true)
            } catch (e: DeletionBlockedException) {
                _deletionError.value = e.message
                onResult(false)
            }
        }
    }

    fun clearDeletionError() {
        _deletionError.value = null
    }
}
