package com.robson.financas.data.repository.fiscal

import com.robson.financas.data.local.dao.fiscal.PriceHistoryDao
import com.robson.financas.data.local.dao.fiscal.ProductDao
import com.robson.financas.data.local.entity.fiscal.PriceHistoryEntity
import com.robson.financas.data.local.entity.fiscal.ProductEntity
import com.robson.financas.data.local.relation.fiscal.EstablishmentPricePoint
import com.robson.financas.data.local.relation.fiscal.ProductPriceSummary
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PriceHistoryRepository @Inject constructor(
    private val priceHistoryDao: PriceHistoryDao,
    private val productDao: ProductDao,
) {
    suspend fun getProduct(productId: Long): ProductEntity? = productDao.getById(productId)

    fun observeHistory(productId: Long): Flow<List<PriceHistoryEntity>> = priceHistoryDao.observeForProduct(productId)

    fun observeSummary(productId: Long): Flow<ProductPriceSummary?> = priceHistoryDao.observeSummary(productId)

    fun observeEstablishmentComparison(productId: Long): Flow<List<EstablishmentPricePoint>> =
        priceHistoryDao.observeEstablishmentComparison(productId)
}
