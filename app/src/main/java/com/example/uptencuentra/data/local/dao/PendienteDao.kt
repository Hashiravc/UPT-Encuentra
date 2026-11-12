package com.example.uptencuentra.data.local.dao

import androidx.room.*
import com.example.uptencuentra.data.local.entity.PendienteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendienteDao {
    @Upsert
    suspend fun encolar(pendiente: PendienteEntity)

    @Query("SELECT * FROM pendientes WHERE cuentaId = :cuentaId ORDER BY creadoEn, reporteId")
    suspend fun obtenerPendientes(cuentaId: String): List<PendienteEntity>

    @Query("SELECT * FROM pendientes WHERE reporteId = :id")
    suspend fun obtener(id: String): PendienteEntity?

    @Query("SELECT COUNT(*) FROM pendientes WHERE cuentaId = :cuentaId")
    fun observarCantidad(cuentaId: String): Flow<Int>

    @Query("DELETE FROM pendientes WHERE reporteId = :id AND version = :version AND cuentaId = :cuentaId")
    suspend fun confirmar(id: String, version: Int, cuentaId: String)
}
