package com.robson.financas.data.local.dao.fiscal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.robson.financas.data.local.entity.fiscal.EstablishmentEntity

@Dao
interface EstablishmentDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(establishment: EstablishmentEntity): Long

    @Query("SELECT * FROM establishments WHERE cnpj = :cnpj LIMIT 1")
    suspend fun findByCnpj(cnpj: String): EstablishmentEntity?

    @Query("SELECT * FROM establishments WHERE id = :id")
    suspend fun getById(id: Long): EstablishmentEntity?
}
