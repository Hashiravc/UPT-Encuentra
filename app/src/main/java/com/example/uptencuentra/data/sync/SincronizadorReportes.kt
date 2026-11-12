package com.example.uptencuentra.data.sync

import androidx.room.withTransaction
import com.example.uptencuentra.data.api.*
import com.example.uptencuentra.data.local.AppDatabase
import com.example.uptencuentra.data.local.entity.ReporteEntity
import com.example.uptencuentra.data.model.*
import com.example.uptencuentra.domain.repository.SesionRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

enum class EstadoSincronizacion { EN_ESPERA, SINCRONIZANDO, ACTUALIZADO, SIN_CONEXION, SESION_VENCIDA, ERROR }

@Singleton
class SincronizadorReportes @Inject constructor(
    private val database: AppDatabase, private val remoto: SupabaseReportesAdapter,
    private val sesion: SesionRepository, private val gson: Gson,
    private val fotos: SincronizadorFotos, private val api: SupabaseApi
) {
    private val mutex = Mutex()
    private val mutableEstado = MutableStateFlow(EstadoSincronizacion.EN_ESPERA)
    val estado = mutableEstado.asStateFlow()
    fun informar(estado: EstadoSincronizacion) { mutableEstado.value = estado }

    suspend fun sincronizar(cuentaId: String) = mutex.withLock {
        comprobarCuenta(cuentaId); informar(EstadoSincronizacion.SINCRONIZANDO)
        enviarReportes(cuentaId)
        descargarReportes(cuentaId)
        enviarSolicitudes(cuentaId)
        descargarSolicitudes(cuentaId)
        informar(EstadoSincronizacion.ACTUALIZADO)
    }

    private suspend fun enviarReportes(cuenta: String) {
        for (pendiente in database.pendienteDao().obtenerPendientes(cuenta)) {
            comprobarCuenta(cuenta)
            val token = sesion.tokenValido(cuenta)
            val datos = fotos.preparar(gson.fromJson(pendiente.payloadJson, ReporteDto::class.java), token, cuenta)
            val respuesta = remoto.publicar(token, datos)
            check(respuesta.id == pendiente.reporteId && respuesta.autorId == cuenta && respuesta.version >= pendiente.version)
            val entity = prepararLocal(respuesta, token)
            database.withTransaction {
                aplicar(entity, cuenta)
                database.pendienteDao().confirmar(pendiente.reporteId, pendiente.version, cuenta)
            }
        }
    }

    private suspend fun descargarReportes(cuenta: String) {
        var inicio = 0
        do {
            comprobarCuenta(cuenta)
            val token = sesion.tokenValido(cuenta)
            val pagina = remoto.descargarPagina(token, cuenta, inicio)
            for (reporte in pagina) {
                check(reporte.autorId == cuenta)
                val entity = prepararLocal(reporte, token)
                database.withTransaction { aplicar(entity, cuenta) }
            }
            inicio += pagina.size
        } while (pagina.size == 200)
    }

    private suspend fun prepararLocal(dto: ReporteRemotoDto, token: String): ReporteEntity {
        val anterior = database.reporteDao().obtener(dto.id)
        return dto.aEntity().copy(fotoLocal = fotos.localizar(dto, token, anterior?.fotoLocal))
    }

    private suspend fun aplicar(remoto: ReporteEntity, cuenta: String) {
        val local = database.reporteDao().obtener(remoto.id)
        check(local == null || local.cuentaId == cuenta)
        val pendiente = database.pendienteDao().obtener(remoto.id)
        if (pendiente != null && pendiente.version > remoto.version) return
        if (local != null && local.version > remoto.version) return
        if (local == null) database.reporteDao().insertar(remoto) else database.reporteDao().actualizar(remoto)
    }

    private suspend fun enviarSolicitudes(cuenta: String) {
        for (local in database.solicitudDao().pendientes(cuenta)) {
            comprobarCuenta(cuenta)
            val respuesta = api.solicitar("Bearer ${sesion.tokenValido(cuenta)}",
                SolicitudRequest(NuevaSolicitudDto(local.id, local.coincidenciaId, local.explicacion)))
            check(respuesta.id == local.id && respuesta.cuentaId == cuenta)
            database.solicitudDao().guardar(respuesta.aEntity())
        }
    }

    private suspend fun descargarSolicitudes(cuenta: String) {
        var inicio = 0
        do {
            comprobarCuenta(cuenta)
            val pagina = api.solicitudes("Bearer ${sesion.tokenValido(cuenta)}", "eq.$cuenta", inicio = inicio)
            for (solicitud in pagina) {
                check(solicitud.cuentaId == cuenta)
                database.solicitudDao().guardar(solicitud.aEntity())
            }
            inicio += pagina.size
        } while (pagina.size == 200)
    }

    private fun comprobarCuenta(cuenta: String) { check(cuenta.isNotBlank() && sesion.usuario.value?.id == cuenta) }
}
