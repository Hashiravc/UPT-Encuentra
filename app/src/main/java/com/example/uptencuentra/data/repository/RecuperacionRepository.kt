package com.example.uptencuentra.data.repository

import androidx.room.withTransaction
import com.example.uptencuentra.data.api.SupabaseApi
import com.example.uptencuentra.data.local.AppDatabase
import com.example.uptencuentra.data.local.entity.*
import com.example.uptencuentra.data.model.*
import com.example.uptencuentra.domain.model.*
import com.example.uptencuentra.domain.repository.SesionRepository
import com.example.uptencuentra.domain.service.*
import com.google.gson.Gson
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.util.UUID
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class RecuperacionRepository @Inject constructor(
    private val database: AppDatabase, private val api: SupabaseApi, private val sesion: SesionRepository,
    private val programar: ProgramarSincronizacion, private val gson: Gson,
    private val sincronizador: com.example.uptencuentra.data.sync.SincronizadorReportes
) {
    fun observarCoincidencias(perdida: String) = sesion.usuario.flatMapLatest { usuario ->
        if (usuario == null) flowOf(emptyList()) else database.coincidenciaDao().observar(perdida, usuario.id)
            .map { lista -> lista.map { Coincidencia(it.id, it.perdidaId, it.categoria, it.zona, it.fecha, it.puntaje,
                gson.fromJson(it.motivosJson, Array<String>::class.java).toList()) } }
    }

    // En modo local compara los registros del dispositivo; en línea recibe solo resúmenes autorizados.
    suspend fun buscar(perdida: String) {
        val cuenta = checkNotNull(sesion.usuario.value)
        val reporte = checkNotNull(database.reporteDao().obtener(perdida))
        check(reporte.cuentaId == cuenta.id && reporte.tipo == "PERDIDO" && reporte.estado == "ACTIVO")
        if (!cuenta.esDemostracion && database.pendienteDao().obtener(perdida) != null) sincronizador.sincronizar(cuenta.id)
        val lista = if (cuenta.esDemostracion) {
            val motor = MotorCoincidencias(); val p = ReporteMapper.aDominio(reporte)
            database.reporteDao().observarReportes("").first().mapNotNull { motor.comparar(p, ReporteMapper.aDominio(it)) }
        } else api.buscarCoincidencias("Bearer ${sesion.tokenValido(cuenta.id)}", BuscarCoincidenciasRequest(perdida))
            .map { check(it.perdidaId == perdida); Coincidencia(it.id, it.perdidaId, it.categoria, it.zona, it.fecha, it.puntaje, it.motivos) }
        database.withTransaction {
            database.coincidenciaDao().limpiar(perdida, cuenta.id)
            database.coincidenciaDao().guardar(lista.map { CoincidenciaEntity(it.id, it.perdidaId, it.categoria,
                it.zona, it.fecha, it.puntaje, gson.toJson(it.motivos), cuenta.id) })
        }
    }

    suspend fun solicitar(coincidencia: String, explicacion: String) {
        val cuenta = checkNotNull(sesion.usuario.value)
        require(explicacion.trim().length in 10..1000)
        database.withTransaction {
            val c = checkNotNull(database.coincidenciaDao().obtener(coincidencia, cuenta.id))
            check(database.solicitudDao().activa(cuenta.id, coincidencia) == null)
            val estado = if (cuenta.esDemostracion) "PENDIENTE_LOCAL" else "PENDIENTE_ENVIO"
            val hallazgo = if (cuenta.esDemostracion) c.id.substringAfter(':') else ""
            database.solicitudDao().guardar(SolicitudEntity(UUID.randomUUID().toString(), c.id, c.perdidaId,
                hallazgo, explicacion.trim(), estado, cuenta.id))
        }
        if (!cuenta.esDemostracion) programar.programar(cuenta.id)
    }

    fun observarSolicitudes() = sesion.usuario.flatMapLatest { cuenta ->
        if (cuenta == null) flowOf(emptyList()) else database.solicitudDao().observar(cuenta.id)
    }

    suspend fun hallazgosEncargado(): List<ReporteRemotoDto> {
        val cuenta = checkNotNull(sesion.usuario.value)
        check(cuenta.esEncargado && !cuenta.esDemostracion)
        return api.hallazgosEncargado("Bearer ${sesion.tokenValido(cuenta.id)}")
    }
    suspend fun solicitudesEncargado(): List<SolicitudDto> {
        val cuenta = checkNotNull(sesion.usuario.value); check(cuenta.esEncargado)
        return api.solicitudesEncargado("Bearer ${sesion.tokenValido(cuenta.id)}")
    }
    suspend fun recibir(id: String, area: String) {
        val cuenta = checkNotNull(sesion.usuario.value); check(cuenta.esEncargado)
        require(area.trim().length in 1..120)
        api.recibir("Bearer ${sesion.tokenValido(cuenta.id)}", RecepcionRequest(id, area.trim()))
    }
    suspend fun resolver(id: String, accion: String, verificado: Boolean) {
        val cuenta = checkNotNull(sesion.usuario.value); check(cuenta.esEncargado)
        api.resolver("Bearer ${sesion.tokenValido(cuenta.id)}", ResolverRequest(id, accion, verificado))
    }
}
