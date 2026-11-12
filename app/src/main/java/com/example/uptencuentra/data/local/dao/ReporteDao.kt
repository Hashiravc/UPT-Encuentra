package com.example.uptencuentra.data.local.dao

import androidx.room.*
import com.example.uptencuentra.data.local.entity.ReporteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReporteDao {
    @Query("SELECT * FROM reportes WHERE cuentaId = :cuentaId ORDER BY creadoEn DESC, id")
    fun observarReportes(cuentaId: String): Flow<List<ReporteEntity>>

    @Query("SELECT * FROM reportes WHERE id = :id AND cuentaId = :cuentaId")
    fun observarReporte(id: String, cuentaId: String): Flow<ReporteEntity?>

    @Query("SELECT * FROM reportes WHERE id = :id")
    suspend fun obtener(id: String): ReporteEntity?

    @Query("SELECT COUNT(*) FROM reportes WHERE cuentaId = ''")
    fun observarSinCuenta(): Flow<Int>

    @Query("SELECT * FROM reportes WHERE cuentaId = ''")
    suspend fun obtenerSinCuenta(): List<ReporteEntity>

    @Insert
    suspend fun insertar(reporte: ReporteEntity)

    @Update
    suspend fun actualizar(reporte: ReporteEntity)
}
