package com.example.uptencuentra.data.repository

import androidx.room.withTransaction
import com.example.uptencuentra.data.local.AppDatabase
import com.example.uptencuentra.data.local.entity.PendienteEntity
import com.example.uptencuentra.data.model.ReporteMapper
import com.example.uptencuentra.domain.service.ProgramarSincronizacion
import com.example.uptencuentra.domain.model.Reporte
import com.example.uptencuentra.domain.repository.*
import com.google.gson.Gson
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class OfflineFirstReporteRepository @Inject constructor(
    private val database: AppDatabase,
    private val gson: Gson,
    private val sesion: SesionRepository,
    private val programador: ProgramarSincronizacion
) : ReporteRepository {
    private val reportes = database.reporteDao()
    private val pendientes = database.pendienteDao()

    override fun observarReportes() = sesion.usuario.flatMapLatest { usuario ->
        if (usuario == null) flowOf(emptyList()) else reportes.observarReportes(usuario.id)
            .map { lista -> lista.map(ReporteMapper::aDominio) }
    }

    override fun observarReporte(id: String) = sesion.usuario.flatMapLatest { usuario ->
        if (usuario == null) flowOf(null) else reportes.observarReporte(id, usuario.id)
            .map { it?.let(ReporteMapper::aDominio) }
    }

    // Guarda el reporte y su pendiente juntos, y después programa el envío a Supabase.
    override suspend fun guardar(reporte: Reporte) {
        val cuenta = checkNotNull(sesion.usuario.value).id
        database.withTransaction {
            val existente = reportes.obtener(reporte.id)
            if (existente != null) {
                check(existente.cuentaId == cuenta)
                check(ReporteMapper.aDto(ReporteMapper.aDominio(existente)) == ReporteMapper.aDto(reporte))
                return@withTransaction
            }
            reportes.insertar(ReporteMapper.aEntity(reporte, System.currentTimeMillis()).copy(cuentaId = cuenta))
            encolar(reporte, cuenta)
        }
        if (cuenta.isNotBlank()) programador.programar(cuenta)
    }

    override suspend fun cancelar(id: String) {
        val cuenta = checkNotNull(sesion.usuario.value).id
        database.withTransaction {
            val entity = checkNotNull(reportes.obtener(id))
            check(entity.cuentaId == cuenta)
            val cancelado = ReporteMapper.aDominio(entity).cancelar()
            reportes.actualizar(ReporteMapper.aEntity(cancelado, entity.creadoEn).copy(cuentaId = cuenta))
            encolar(cancelado, cuenta)
        }
        if (cuenta.isNotBlank()) programador.programar(cuenta)
    }

    // Asocia los reportes de demostración únicamente tras la confirmación del usuario en la pantalla.
    suspend fun importarDemostracion() {
        val cuenta = checkNotNull(sesion.usuario.value).id
        check(cuenta.isNotBlank())
        database.withTransaction {
            for (entity in reportes.obtenerSinCuenta()) {
                reportes.actualizar(entity.copy(cuentaId = cuenta))
                encolar(ReporteMapper.aDominio(entity), cuenta)
            }
        }
        if (cuenta.isNotBlank()) programador.programar(cuenta)
    }

    private suspend fun encolar(reporte: Reporte, cuenta: String) {
        pendientes.encolar(PendienteEntity(reporte.id, reporte.version,
            gson.toJson(ReporteMapper.aDto(reporte)), System.currentTimeMillis(), cuenta))
    }
}
