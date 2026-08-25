package com.robson.financas.data.local.dao.fiscal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.robson.financas.data.local.entity.fiscal.FiscalAuditLogEntity

@Dao
interface FiscalAuditLogDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: FiscalAuditLogEntity): Long
}
