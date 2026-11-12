package com.example.uptencuentra.data

import androidx.room.Room
import android.app.Application
import org.robolectric.RuntimeEnvironment
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.example.uptencuentra.data.local.AppDatabase
import com.example.uptencuentra.data.model.ReporteDto
import com.example.uptencuentra.data.repository.OfflineFirstReporteRepository
import com.example.uptencuentra.domain.model.*
import com.google.gson.*
import com.example.uptencuentra.domain.repository.SesionRepository
import com.example.uptencuentra.domain.model.UsuarioSesion
import com.example.uptencuentra.domain.service.ProgramarSincronizacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class RoomRepositoryTest {
    private val sesion = object : SesionRepository {
        override val usuario = MutableStateFlow<UsuarioSesion?>(UsuarioSesion("cuenta-prueba", "prueba@example.com"))
        override suspend fun entrarDemostracion() { usuario.value = UsuarioSesion("", "") }
        override suspend fun iniciarSesion(correo: String, clave: String) = Unit
        override suspend fun registrar(nombre: String, correo: String, clave: String): Boolean = true
        override suspend fun tokenValido(usuarioId: String): String = "token-prueba"
        override suspend fun cerrarSesion() { usuario.value = null }
    }
    private val programador = ProgramarSincronizacion { }
    private lateinit var database: AppDatabase
    private lateinit var repository: OfflineFirstReporteRepository
    private val context get() = RuntimeEnvironment.getApplication()
    private val nombre = "prueba-offline.db"

    @Before fun preparar() {
        context.deleteDatabase(nombre)
        abrir()
    }

    private fun abrir() {
        database = Room.databaseBuilder(context, AppDatabase::class.java, nombre).build()
        repository = OfflineFirstReporteRepository(database, Gson(), sesion, programador)
    }

    @After fun cerrar() {
        database.close()
        context.deleteDatabase(nombre)
    }

    private fun reporte() = Reporte.crear(TipoReporte.PERDIDO, "Cuaderno", "Cuaderno azul con anotaciones",
        LocalDate.of(2026, 1, 1), UbicacionReporte("Campus", "Patio central"),
        CaracteristicasObjeto("", "", "Azul"), null)

    @Test fun persisteAlReabrir() = runBlocking {
        val reporte = reporte()
        repository.guardar(reporte)
        database.close()
        abrir()
        assertEquals(reporte.id, repository.observarReportes().first().single().id)
        val pendiente = database.pendienteDao().obtenerPendientes("cuenta-prueba").single()
        assertEquals(reporte.id, pendiente.reporteId)
        assertEquals(reporte.id, Gson().fromJson(pendiente.payloadJson, ReporteDto::class.java).id)
    }

    @Test fun reintentoSinDuplicados() = runBlocking {
        val reporte = reporte()
        repository.guardar(reporte)
        repository.guardar(reporte)
        assertEquals(1, repository.observarReportes().first().size)
        assertEquals(1, database.pendienteDao().obtenerPendientes("cuenta-prueba").size)
    }

    @Test fun revierteGuardadoIncompleto() = runBlocking {
        val gson = GsonBuilder().registerTypeAdapter(ReporteDto::class.java,
            JsonSerializer<ReporteDto> { _, _, _ -> throw JsonIOException("Fallo de prueba") }).create()
        val repositoryConFallo = OfflineFirstReporteRepository(database, gson, sesion, programador)
        try {
            repositoryConFallo.guardar(reporte())
            fail("Debía fallar la serialización")
        } catch (_: JsonIOException) {
            assertTrue(repository.observarReportes().first().isEmpty())
            assertTrue(database.pendienteDao().obtenerPendientes("cuenta-prueba").isEmpty())
        }
    }

    @Test fun confirmacionAntiguaConservaNuevaVersion() = runBlocking {
        val reporte = reporte()
        repository.guardar(reporte)
        repository.cancelar(reporte.id)
        val actualizado = repository.observarReporte(reporte.id).first()!!
        assertEquals(EstadoReporte.CANCELADO, actualizado.estado)
        assertEquals(2, actualizado.version)
        assertEquals(2, database.pendienteDao().obtenerPendientes("cuenta-prueba").single().version)
        database.pendienteDao().confirmar(reporte.id, 1, "cuenta-prueba")
        assertEquals(1, database.pendienteDao().obtenerPendientes("cuenta-prueba").size)
        database.pendienteDao().confirmar(reporte.id, 2, "cuenta-prueba")
        assertTrue(database.pendienteDao().obtenerPendientes("cuenta-prueba").isEmpty())
    }
}
