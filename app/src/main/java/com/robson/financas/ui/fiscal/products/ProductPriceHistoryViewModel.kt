package com.robson.financas.ui.fiscal.products

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.local.entity.fiscal.PriceHistoryEntity
import com.robson.financas.data.local.entity.fiscal.ProductEntity
import com.robson.financas.data.local.relation.fiscal.EstablishmentPricePoint
import com.robson.financas.data.local.relation.fiscal.ProductPriceSummary
import com.robson.financas.data.repository.fiscal.PriceHistoryRepository
import com.robson.financas.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductPriceHistoryUiState(
    val product: ProductEntity? = null,
    val summary: ProductPriceSummary? = null,
    val history: List<PriceHistoryEntity> = emptyList(),
    val comparison: List<EstablishmentPricePoint> = emptyList(),
)

@HiltViewModel
class ProductPriceHistoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PriceHistoryRepository,
) : ViewModel() {

    private val productId: Long = checkNotNull(savedStateHandle[Screen.ProductPriceHistory.ARG_PRODUCT_ID])
    private val product = MutableStateFlow<ProductEntity?>(null)

    val uiState: StateFlow<ProductPriceHistoryUiState> = combine(
        repository.observeSummary(productId),
        repository.observeHistory(productId),
        repository.observeEstablishmentComparison(productId),
        product,
    ) { summary, history, comparison, productEntity ->
        ProductPriceHistoryUiState(productEntity, summary, history, comparison)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProductPriceHistoryUiState())

    init {
        viewModelScope.launch { product.update { repository.getProduct(productId) } }
    }
}
