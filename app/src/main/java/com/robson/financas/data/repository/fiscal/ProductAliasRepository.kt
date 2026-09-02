package com.robson.financas.data.repository.fiscal

import com.robson.financas.data.local.dao.fiscal.ProductAliasDao
import com.robson.financas.data.local.entity.fiscal.ProductAliasEntity
import com.robson.financas.domain.fiscal.ai.NormalizedProductName
import javax.inject.Inject
import javax.inject.Singleton

/** Cache local de descrição bruta da loja → nome canônico + marca já resolvidos pela IA. */
@Singleton
class ProductAliasRepository @Inject constructor(
    private val productAliasDao: ProductAliasDao,
) {
    suspend fun getCached(rawDescriptions: List<String>): Map<String, ProductAliasEntity> =
        productAliasDao.findByRawDescriptions(rawDescriptions).associateBy { it.rawDescription }

    suspend fun saveResolved(entries: Map<String, NormalizedProductName>) {
        entries.forEach { (rawDescription, resolved) ->
            productAliasDao.insert(
                ProductAliasEntity(
                    rawDescription = rawDescription,
                    canonicalName = resolved.canonicalName,
                    brand = resolved.brand,
                ),
            )
        }
    }
}
