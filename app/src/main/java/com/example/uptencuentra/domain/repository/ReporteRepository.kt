package com.example.uptencuentra.domain.repository

import com.example.uptencuentra.domain.model.Reporte
import kotlinx.coroutines.flow.Flow

interface ReporteRepository {
    fun observarReportes(): Flow<List<Reporte>>
    fun observarReporte(id: String): Flow<Reporte?>
    suspend fun guardar(reporte: Reporte)
    suspend fun cancelar(id: String)
}
