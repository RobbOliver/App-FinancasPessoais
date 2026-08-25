package com.robson.financas.data.local.dao.fiscal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.robson.financas.data.local.entity.fiscal.MicrocategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MicrocategoryDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(microcategory: MicrocategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(microcategories: List<MicrocategoryEntity>)

    @Query("SELECT COUNT(*) FROM microcategories")
    suspend fun count(): Int

    @Query("SELECT * FROM microcategories WHERE active = 1")
    suspend fun getAllActive(): List<MicrocategoryEntity>

    @Query("SELECT * FROM microcategories WHERE active = 1")
    fun observeAllActive(): Flow<List<MicrocategoryEntity>>

    @Query("SELECT * FROM microcategories WHERE id = :id")
    suspend fun getById(id: Long): MicrocategoryEntity?

    @Query("SELECT * FROM microcategories WHERE systemKey = :systemKey LIMIT 1")
    suspend fun findByKey(systemKey: String): MicrocategoryEntity?

    @Query("SELECT * FROM microcategories WHERE subcategoryId = :subcategoryId AND active = 1")
    fun observeBySubcategory(subcategoryId: Long): Flow<List<MicrocategoryEntity>>
}
