package com.robson.financas.notifications

import com.robson.financas.data.local.dao.CategoryDao
import com.robson.financas.data.local.dao.TransactionDao
import com.robson.financas.data.local.entity.CategoryType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategorySuggestionEngine @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
) {
    suspend fun suggestCategoryId(
        counterpartyName: String?,
        description: String,
        type: CategoryType,
    ): Long? {
        if (!counterpartyName.isNullOrBlank()) {
            transactionDao.findRecentCategoryIdForCounterparty(counterpartyName)?.let { return it }
        }

        val haystack = "${counterpartyName.orEmpty()} $description".lowercase()
        for ((keyword, categoryName) in keywordMap(type)) {
            if (haystack.contains(keyword)) {
                categoryDao.findByNameAndType(categoryName, type)?.let { return it.id }
            }
        }

        return null
    }

    private fun keywordMap(type: CategoryType): List<Pair<String, String>> = when (type) {
        CategoryType.EXPENSE -> listOf(
            "mercado" to "Alimentação",
            "supermercado" to "Alimentação",
            "restaurante" to "Alimentação",
            "lanchonete" to "Alimentação",
            "padaria" to "Alimentação",
            "ifood" to "Alimentação",
            "uber" to "Transporte",
            "99app" to "Transporte",
            "posto" to "Transporte",
            "combustivel" to "Transporte",
            "combustível" to "Transporte",
            "farmacia" to "Saúde",
            "farmácia" to "Saúde",
            "drogaria" to "Saúde",
            "cinema" to "Lazer",
            "netflix" to "Lazer",
            "spotify" to "Lazer",
            "shopping" to "Compras",
            "loja" to "Compras",
            "luz" to "Contas e Serviços",
            "energia" to "Contas e Serviços",
            "agua" to "Contas e Serviços",
            "água" to "Contas e Serviços",
            "internet" to "Contas e Serviços",
            "telefone" to "Contas e Serviços",
            "aluguel" to "Moradia",
            "condominio" to "Moradia",
            "condomínio" to "Moradia",
            "escola" to "Educação",
            "faculdade" to "Educação",
            "curso" to "Educação",
        )
        CategoryType.INCOME -> listOf(
            "salario" to "Salário",
            "salário" to "Salário",
            "folha de pagamento" to "Salário",
        )
    }
}
