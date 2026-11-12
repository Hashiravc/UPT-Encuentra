package com.example.uptencuentra.data.api

import com.example.uptencuentra.data.model.*
import javax.inject.Inject

class SupabaseReportesAdapter @Inject constructor(private val api: SupabaseApi) {
    suspend fun publicar(token: String, reporte: ReporteDto): ReporteRemotoDto =
        api.guardarReporte("Bearer $token", GuardarReporteRequest(reporte.copy(fotoLocal = null)))
    suspend fun descargarPagina(token: String, usuarioId: String, inicio: Int): List<ReporteRemotoDto> =
        api.obtenerReportes("Bearer $token", PaginaPropia(inicio))
}
