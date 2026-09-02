package com.robson.financas.ui.fiscal.brands

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robson.financas.data.local.dao.fiscal.ProductDao
import com.robson.financas.data.local.entity.fiscal.ProductEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private const val NO_BRAND_LABEL = "Sem marca"

data class BrandGroup(val brand: String, val products: List<ProductEntity>)

@HiltViewModel
class BrandsViewModel @Inject constructor(
    productDao: ProductDao,
) : ViewModel() {

    val brandGroups: StateFlow<List<BrandGroup>> = productDao.observeAll()
        .map { products ->
            products
                .groupBy { it.brand?.trim()?.takeIf { b -> b.isNotEmpty() } ?: NO_BRAND_LABEL }
                .toSortedMap(compareBy { it })
                .map { (brand, productsForBrand) -> BrandGroup(brand, productsForBrand.sortedBy { it.genericName }) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
