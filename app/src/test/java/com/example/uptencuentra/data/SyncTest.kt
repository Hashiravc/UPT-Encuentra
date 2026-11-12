package com.example.uptencuentra.data

import android.app.Application
import androidx.room.Room
import com.example.uptencuentra.data.api.*
import com.example.uptencuentra.data.local.AppDatabase
import com.example.uptencuentra.data.model.*
import com.example.uptencuentra.data.repository.OfflineFirstReporteRepository
import com.example.uptencuentra.data.sync.*
import com.example.uptencuentra.domain.model.*
import com.example.uptencuentra.domain.repository.SesionRepository
import com.example.uptencuentra.domain.service.ProgramarSincronizacion
import com.google.gson.Gson
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class SyncTest {
    private lateinit var database: AppDatabase
    private lateinit var servidor: MockWebServer
    private lateinit var repository: OfflineFirstReporteRepository
    private lateinit var sincronizador: SincronizadorReportes
    private val gson = Gson()
    private val usuarioA = "11111111-1111-4111-8111-111111111111"
    private val usuarioB = "22222222-2222-4222-8222-222222222222"
    private val sesion = object : SesionRepository {
        override val usuario = MutableStateFlow<UsuarioSesion?>(null)
        override suspend fun entrarDemostracion() { usuario.value = UsuarioSesion("", "") }
        override suspend fun iniciarSesion(correo: String, clave: String) = Unit
        override suspend fun registrar(nombre: String, correo: String, clave: String): Boolean = true
        override suspend fun tokenValido(usuarioId: String): String {
            check(usuario.value?.id == usuarioId)
            return "token-de-prueba"
        }
        override suspend fun cerrarSesion() { usuario.value = null }
    }

    @Before fun preparar() {
        sesion.usuario.value = UsuarioSesion(usuarioA, "prueba@example.com")
        database = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), AppDatabase::class.java).build()
        servidor = MockWebServer().apply { start() }
        val api = Retrofit.Builder().baseUrl(servidor.url("/"))
            .addConverterFactory(GsonConverterFactory.create(gson)).build().create(SupabaseApi::class.java)
        repository = OfflineFirstReporteRepository(database, gson, sesion, ProgramarSincronizacion { })
        sincronizador = SincronizadorReportes(database, SupabaseReportesAdapter(api), sesion, gson, SincronizadorFotos(api, com.example.uptencuentra.data.photos.AlmacenFotos(RuntimeEnvironment.getApplication())), api)
    }

    @After fun cerrar() { database.close(); servidor.shutdown() }

    private fun reporte() = Reporte.crear(TipoReporte.PERDIDO, "Celular", "Celular negro con funda azul",
        LocalDate.of(2026, 1, 1), UbicacionReporte("Campus", "Patio"), CaracteristicasObjeto("Samsung", "", "Negro"), null)

    private fun respuesta(reporte: Reporte, autor: String = usuarioA) = ReporteRemotoDto(
        reporte.id, autor, reporte.tipo.name, reporte.categoria, reporte.descripcion, reporte.fechaEvento.toString(),
        reporte.ubicacion.zona, reporte.ubicacion.referencia,
        CaracteristicasDto(reporte.caracteristicas.marca, reporte.caracteristicas.modelo, reporte.caracteristicas.color),
        reporte.areaEntrega, reporte.estado.name, reporte.version, "2026-01-01T12:00:00Z")

    private fun json(datos: Any) = MockResponse().setHeader("Content-Type", "application/json").setBody(gson.toJson(datos))

    @Test fun enviaJsonYConfirmaRoom() = runBlocking {
        val reporte = reporte()
        repository.guardar(reporte)
        servidor.enqueue(json(respuesta(reporte)))
        servidor.enqueue(json(listOf(respuesta(reporte))))
        servidor.enqueue(json(emptyList<SolicitudDto>()))
        sincronizador.sincronizar(usuarioA)
        assertTrue(database.pendienteDao().obtenerPendientes(usuarioA).isEmpty())
        assertEquals(reporte.id, repository.observarReportes().first().single().id)
        val peticion = servidor.takeRequest(2, TimeUnit.SECONDS)!!
        assertEquals("/rest/v1/rpc/guardar_reporte", peticion.path)
        assertEquals("Bearer token-de-prueba", peticion.getHeader("Authorization"))
        assertNotEquals("application/vnd.pgrst.object+json", peticion.getHeader("Accept"))
        val cuerpo = gson.fromJson(peticion.body.readUtf8(), GuardarReporteRequest::class.java)
        assertEquals(reporte.id, cuerpo.datos.id)
        val consulta = servidor.takeRequest(2, TimeUnit.SECONDS)!!
        assertEquals("/rest/v1/rpc/mis_reportes", consulta.path)
        assertEquals("POST", consulta.method)
    }

    @Test fun falloDeRedConservaPendiente() = runBlocking {
        val reporte = reporte()
        repository.guardar(reporte)
        servidor.enqueue(MockResponse().setResponseCode(503))
        try {
            sincronizador.sincronizar(usuarioA)
            fail("Debía fallar la red")
        } catch (_: HttpException) {
            assertEquals(1, database.pendienteDao().obtenerPendientes(usuarioA).size)
            assertEquals(reporte.id, repository.observarReportes().first().single().id)
        }
    }

    @Test fun confirmacionViejaNoBorraCambioNuevo() = runBlocking {
        val reporte = reporte()
        repository.guardar(reporte)
        var primera = true
        servidor.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                if (request.path!!.startsWith("/rest/v1/solicitudes_recuperacion")) return json(emptyList<SolicitudDto>())
                if (request.path!!.startsWith("/rest/v1/rpc/mis_reportes")) return json(listOf(respuesta(reporte)))
                if (request.path!!.startsWith("/rest/v1/rpc/guardar_reporte")) {
                    if (primera) {
                        primera = false
                        runBlocking { repository.cancelar(reporte.id) }
                        return json(respuesta(reporte))
                    }
                    return json(respuesta(reporte.cancelar()))
                }
                return json(listOf(respuesta(if (primera) reporte else reporte.cancelar())))
            }
        }
        sincronizador.sincronizar(usuarioA)
        assertEquals(2, database.pendienteDao().obtenerPendientes(usuarioA).single().version)
        assertEquals(EstadoReporte.CANCELADO, repository.observarReportes().first().single().estado)
        sincronizador.sincronizar(usuarioA)
        assertTrue(database.pendienteDao().obtenerPendientes(usuarioA).isEmpty())
    }

    @Test fun otraCuentaNoVeNiCancelaDatosAjenos() = runBlocking {
        val reporte = reporte()
        repository.guardar(reporte)
        sesion.usuario.value = UsuarioSesion(usuarioB, "otra@example.com")
        assertTrue(repository.observarReportes().first().isEmpty())
        assertNull(repository.observarReporte(reporte.id).first())
        try { repository.cancelar(reporte.id); fail("No debe cancelar reportes ajenos")
        } catch (_: IllegalStateException) { }
        assertEquals(1, database.pendienteDao().obtenerPendientes(usuarioA).size)
        assertTrue(database.pendienteDao().obtenerPendientes(usuarioB).isEmpty())
    }

    @Test fun respuestaDeOtroAutorNoSeAplica() = runBlocking {
        val reporte = reporte()
        repository.guardar(reporte)
        servidor.enqueue(json(respuesta(reporte, usuarioB)))
        try { sincronizador.sincronizar(usuarioA); fail("No debe aceptar otro autor")
        } catch (_: IllegalStateException) { }
        assertEquals(1, database.pendienteDao().obtenerPendientes(usuarioA).size)
    }

    @Test fun descargaUnaPaginaAdicionalCuandoHayDoscientos() = runBlocking {
        val reportes = List(200) { respuesta(reporte()) }
        servidor.enqueue(json(reportes))
        servidor.enqueue(json(emptyList<ReporteRemotoDto>()))
        servidor.enqueue(json(emptyList<SolicitudDto>()))
        sincronizador.sincronizar(usuarioA)
        assertEquals(200, repository.observarReportes().first().size)
        servidor.takeRequest(2, TimeUnit.SECONDS)
        assertEquals(200, gson.fromJson(servidor.takeRequest(2, TimeUnit.SECONDS)!!.body.readUtf8(), PaginaPropia::class.java).inicio)
    }

    @Test fun demostracionNoProgramaEnvios() = runBlocking {
        var envios = 0
        val local = OfflineFirstReporteRepository(database, gson, sesion, ProgramarSincronizacion { envios++ })
        sesion.entrarDemostracion()
        val reporte = reporte()
        local.guardar(reporte)
        local.cancelar(reporte.id)
        assertEquals(0, envios)
        assertEquals(0, servidor.requestCount)
        assertEquals(EstadoReporte.CANCELADO, local.observarReportes().first().single().estado)
        assertEquals(1, database.pendienteDao().obtenerPendientes("").size)
    }

    @Test fun datosDeDemoSeAsocianSoloAlConfirmar() = runBlocking {
        sesion.entrarDemostracion()
        val reporte = reporte()
        repository.guardar(reporte)
        sesion.usuario.value = UsuarioSesion(usuarioA, "prueba@example.com")
        assertTrue(repository.observarReportes().first().isEmpty())
        assertTrue(database.pendienteDao().obtenerPendientes(usuarioA).isEmpty())
        repository.importarDemostracion()
        assertEquals(reporte.id, repository.observarReportes().first().single().id)
        assertTrue(database.pendienteDao().obtenerPendientes("").isEmpty())
        assertEquals(1, database.pendienteDao().obtenerPendientes(usuarioA).size)
    }

    @Test fun demoNoPuedeAsignarseComoCuentaReal() = runBlocking {
        sesion.entrarDemostracion()
        try { repository.importarDemostracion(); fail("La demostración no es una cuenta autenticada")
        } catch (_: IllegalStateException) { }
        assertEquals(0, servidor.requestCount)
    }
}
