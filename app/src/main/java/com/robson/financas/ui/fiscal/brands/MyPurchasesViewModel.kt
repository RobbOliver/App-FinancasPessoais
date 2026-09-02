package com.robson.financas.ui.fiscal.brands

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.local.dao.fiscal.FiscalDocumentDao
import com.robson.financas.data.local.dao.fiscal.ProductDao
import com.robson.financas.data.local.dao.fiscal.PurchaseItemDao
import com.robson.financas.data.local.entity.fiscal.ProductEntity
import com.robson.financas.data.local.relation.fiscal.PurchaseItemWithDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyPurchasesDashboardStats(
    val totalItems: Int = 0,
    val totalSpentCents: Long = 0L,
    val totalDocuments: Int = 0,
    val pendingReviewCount: Int = 0,
)

data class MyPurchasesUiState(
    val dashboardStats: MyPurchasesDashboardStats = MyPurchasesDashboardStats(),
    val allItems: List<PurchaseItemWithDetails> = emptyList(),
    val itemFilter: String = "",
    val brandGroups: List<BrandGroup> = emptyList(),
    val editBrandForProduct: ProductEntity? = null,
)

private const val NO_BRAND_LABEL = "Sem marca"

@HiltViewModel
class MyPurchasesViewModel @Inject constructor(
    private val productDao: ProductDao,
    private val purchaseItemDao: PurchaseItemDao,
    private val fiscalDocumentDao: FiscalDocumentDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyPurchasesUiState())
    val uiState: StateFlow<MyPurchasesUiState> = _uiState

    val filteredItems: StateFlow<List<PurchaseItemWithDetails>> = _uiState
        .map { state ->
            val q = state.itemFilter.trim().lowercase()
            if (q.isEmpty()) state.allItems
            else state.allItems.filter { item ->
                val name = item.productGenericName ?: item.item.normalizedDescription
                name.lowercase().contains(q)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            combine(
                purchaseItemDao.observeTotalCount(),
                purchaseItemDao.observeTotalSpentCents(),
                fiscalDocumentDao.observeTotalCount(),
                purchaseItemDao.observePendingReviewCount(),
            ) { itemCount, spentCents, docCount, reviewCount ->
                MyPurchasesDashboardStats(
                    totalItems = itemCount,
                    totalSpentCents = spentCents ?: 0L,
                    totalDocuments = docCount,
                    pendingReviewCount = reviewCount,
                )
            }.collect { stats ->
                _uiState.update { it.copy(dashboardStats = stats) }
            }
        }

        viewModelScope.launch {
            purchaseItemDao.observeAll().collect { items ->
                _uiState.update { it.copy(allItems = items) }
            }
        }

        viewModelScope.launch {
            productDao.observeAll().collect { products ->
                val groups = products
                    .groupBy { it.brand?.trim()?.takeIf { b -> b.isNotEmpty() } ?: NO_BRAND_LABEL }
                    .toSortedMap(compareBy { if (it == NO_BRAND_LABEL) "￿" else it })
                    .map { (brand, productsForBrand) -> BrandGroup(brand, productsForBrand.sortedBy { it.genericName }) }
                _uiState.update { it.copy(brandGroups = groups) }
            }
        }
    }

    fun updateItemFilter(query: String) = _uiState.update { it.copy(itemFilter = query) }

    fun openEditBrand(product: ProductEntity) = _uiState.update { it.copy(editBrandForProduct = product) }

    fun dismissEditBrand() = _uiState.update { it.copy(editBrandForProduct = null) }

    fun saveBrand(product: ProductEntity, brand: String) {
        viewModelScope.launch {
            productDao.updateBrand(product.id, brand.trim().takeIf { it.isNotEmpty() })
            _uiState.update { it.copy(editBrandForProduct = null) }
        }
    }
}
