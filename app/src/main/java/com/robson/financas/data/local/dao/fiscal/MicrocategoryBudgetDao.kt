package com.robson.financas.data.local.dao.fiscal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.robson.financas.data.local.entity.fiscal.MicrocategoryBudgetEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface MicrocategoryBudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(budget: MicrocategoryBudgetEntity)

    @Query("SELECT * FROM microcategory_budgets WHERE yearMonth = :yearMonth")
    fun observeForMonth(yearMonth: Int): Flow<List<MicrocategoryBudgetEntity>>

    @Query("SELECT * FROM microcategory_budgets WHERE microcategoryId = :microcategoryId AND yearMonth = :yearMonth LIMIT 1")
    suspend fun findFor(microcategoryId: Long, yearMonth: Int): MicrocategoryBudgetEntity?

    /** Quanto foi efetivamente gasto na microcategoria no período — só itens já classificados, nunca "a revisar". */
    @Query(
        """
        SELECT COALESCE(SUM(pi.totalPriceCents - pi.discountCents), 0)
        FROM purchase_items pi
        JOIN fiscal_documents fd ON fd.id = pi.fiscalDocumentId
        WHERE pi.microcategoryId = :microcategoryId
          AND pi.classificationStatus IN ('AUTOMATIC', 'SUGGESTED', 'CONFIRMED')
          AND fd.issuedAt >= :start AND fd.issuedAt <= :end
        """,
    )
    fun observeSpentBetween(microcategoryId: Long, start: LocalDate, end: LocalDate): Flow<Long>
}
