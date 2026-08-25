package com.robson.financas.domain.fiscal.classification

import com.robson.financas.data.local.entity.fiscal.ClassificationSource
import com.robson.financas.data.local.entity.fiscal.MatchType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClassificationEngineTest {

    private val soapPowder = MicrocategoryTaxonomy(
        microcategoryId = 10, subcategoryId = 100, categoryId = 1000,
        keywords = listOf("SABAO PO", "LAV ROUPAS PO"),
    )
    private val cheese = MicrocategoryTaxonomy(
        microcategoryId = 11, subcategoryId = 101, categoryId = 1000,
        keywords = listOf("QUEIJO", "MUSS"),
    )

    private fun baseContext(description: String) = ClassificationContext(
        normalizedDescription = description.uppercase(),
        establishmentId = 5,
        productId = null,
        gtin = null,
        userRules = emptyList(),
        microcategories = listOf(soapPowder, cheese),
        priorConfirmedMatch = null,
    )

    @Test
    fun `user rule always wins even when a keyword would also match`() {
        val engine = ClassificationEngine.default()
        val rule = UserRule(
            id = 1, matchType = MatchType.DESCRIPTION_CONTAINS, matchValue = """["QUEIJO","MUSS"]""",
            productId = null, categoryId = 999, subcategoryId = 998, microcategoryId = 997, priority = 100,
        )
        val context = baseContext("QUEIJO MUSS KG").copy(userRules = listOf(rule))

        val result = engine.classify(context)

        assertEquals(ClassificationSource.USER_RULE, result.source)
        assertEquals(999L, result.categoryId)
        assertEquals(1.0f, result.confidence, 0.0001f)
    }

    @Test
    fun `establishment-scoped rule only applies at that establishment`() {
        val engine = ClassificationEngine.default()
        val rule = UserRule(
            id = 2, matchType = MatchType.ESTABLISHMENT, matchValue = "5|PADARIA",
            productId = null, categoryId = 1, subcategoryId = 2, microcategoryId = 3, priority = 100,
        )

        val atRightEstablishment = baseContext("PAO PADARIA").copy(userRules = listOf(rule), establishmentId = 5)
        assertEquals(ClassificationSource.USER_RULE, engine.classify(atRightEstablishment).source)

        val atOtherEstablishment = baseContext("PAO PADARIA").copy(userRules = listOf(rule), establishmentId = 9)
        assertEquals(ClassificationSource.NEEDS_REVIEW, engine.classify(atOtherEstablishment).source)
    }

    @Test
    fun `prior confirmed match beats keyword rule`() {
        val engine = ClassificationEngine.default()
        val context = baseContext("QUEIJO MUSS KG").copy(
            priorConfirmedMatch = PriorMatch(categoryId = 5, subcategoryId = 6, microcategoryId = 7),
        )

        val result = engine.classify(context)

        assertEquals(ClassificationSource.EXACT_MATCH, result.source)
        assertEquals(7L, result.microcategoryId)
        assertEquals(0.95f, result.confidence, 0.0001f)
    }

    @Test
    fun `keyword rule prefers the longer, more specific match`() {
        val engine = ClassificationEngine.default()
        // "LAV ROUPAS PO" (13 chars) é mais específico que qualquer termo genérico curto.
        val result = engine.classify(baseContext("OMO LAV ROUPAS PO 2.2KG"))

        assertEquals(ClassificationSource.KEYWORD_RULE, result.source)
        assertEquals(10L, result.microcategoryId)
        assertEquals(0.92f, result.confidence, 0.0001f) // >= 8 chars => confiança "automática"
    }

    @Test
    fun `no strategy matches falls back to review with null ids`() {
        val engine = ClassificationEngine.default()
        val result = engine.classify(baseContext("PRODUTO DESCONHECIDO XYZ"))

        assertEquals(ClassificationSource.NEEDS_REVIEW, result.source)
        assertNull(result.categoryId)
        assertNull(result.subcategoryId)
        assertNull(result.microcategoryId)
        assertEquals(0f, result.confidence, 0.0001f)
    }

    @Test
    fun `confidence router maps confidence to the right status`() {
        assertEquals(
            com.robson.financas.data.local.entity.fiscal.ClassificationStatus.AUTOMATIC,
            ConfidenceRouter.statusFor(0.92f),
        )
        assertEquals(
            com.robson.financas.data.local.entity.fiscal.ClassificationStatus.SUGGESTED,
            ConfidenceRouter.statusFor(0.80f),
        )
        assertEquals(
            com.robson.financas.data.local.entity.fiscal.ClassificationStatus.NEEDS_CONFIRMATION,
            ConfidenceRouter.statusFor(0.60f),
        )
        assertEquals(
            com.robson.financas.data.local.entity.fiscal.ClassificationStatus.NEEDS_REVIEW,
            ConfidenceRouter.statusFor(0.10f),
        )
    }
}
