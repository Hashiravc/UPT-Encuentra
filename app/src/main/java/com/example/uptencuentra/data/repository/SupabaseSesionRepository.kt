package com.example.uptencuentra.data.repository

import com.example.uptencuentra.data.api.SupabaseApi
import com.example.uptencuentra.data.auth.AlmacenSesion
import com.example.uptencuentra.data.model.*
import com.example.uptencuentra.domain.model.UsuarioSesion
import com.example.uptencuentra.domain.repository.*
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class SesionGuardada(val datos: SesionDto, val venceEn: Long, val perfil: PerfilDto? = null)

@Singleton
class SupabaseSesionRepository @Inject constructor(
    private val api: SupabaseApi, private val almacen: AlmacenSesion, private val gson: Gson
) : SesionRepository {
    private val mutex = Mutex()
    private var sesion = almacen.leer("sesion")?.let { runCatching { gson.fromJson(it, SesionGuardada::class.java) }.getOrNull() }
    private val mutableUsuario = MutableStateFlow(sesion?.let(::usuarioDe)
        ?: if (almacen.leer("modo_demo") == "activo") UsuarioSesion("", "", "Demostración") else null)
    override val usuario = mutableUsuario.asStateFlow()

    override suspend fun entrarDemostracion() = withContext(Dispatchers.IO) {
        mutex.withLock {
            check(sesion == null)
            almacen.guardar("modo_demo", "activo")
            mutableUsuario.value = UsuarioSesion("", "", "Demostración")
        }
    }

    override suspend fun iniciarSesion(correo: String, clave: String) = withContext(Dispatchers.IO) {
        mutex.withLock { conservar(api.iniciarSesion(AccesoDto(normalizarCorreo(correo), clave))) }
    }

    // Guarda la sesión del registro; devuelve false si el servidor no entrega credenciales de acceso.
    override suspend fun registrar(nombre: String, correo: String, clave: String): Boolean = withContext(Dispatchers.IO) {
        mutex.withLock {
            require(nombre.trim().length in 1..100 && clave.length >= 8)
            val respuesta = api.registrar(RegistroDto(normalizarCorreo(correo), clave, mapOf("nombre" to nombre.trim())))
            if (!respuesta.accessToken.isNullOrBlank() && !respuesta.refreshToken.isNullOrBlank() && respuesta.user != null) {
                conservar(SesionDto(respuesta.accessToken, respuesta.refreshToken, respuesta.expiresIn ?: 3600, respuesta.user))
                true
            } else false
        }
    }

    // Renueva el token sin impedir el trabajo local cuando no hay conexión.
    override suspend fun tokenValido(usuarioId: String): String = withContext(Dispatchers.IO) {
        mutex.withLock {
            val actual = sesion ?: throw SesionExpiradaException()
            if (actual.datos.user.id != usuarioId) throw SesionExpiradaException()
            if (actual.venceEn > System.currentTimeMillis() + 60_000) {
                if (actual.perfil == null) cargarPerfil(actual.datos)
                return@withLock actual.datos.accessToken
            }
            val renovada = try { api.renovarSesion(RenovarSesionDto(actual.datos.refreshToken))
            } catch (error: HttpException) {
                if (error.code() == 400 || error.code() == 401) throw SesionExpiradaException()
                throw error
            }
            check(renovada.user.id == usuarioId)
            conservar(renovada)
            renovada.accessToken
        }
    }

    override suspend fun cerrarSesion() = withContext(Dispatchers.IO) {
        val token = mutex.withLock {
            val anterior = sesion?.datos?.accessToken
            listOf("sesion", "modo_demo", "google").forEach(almacen::borrar)
            sesion = null; mutableUsuario.value = null
            anterior
        }
        if (token != null) try { api.cerrarSesion("Bearer $token")
        } catch (cancelacion: CancellationException) { throw cancelacion
        } catch (_: Exception) { /* El cierre local ya se completó. */ }
    }

    private suspend fun conservar(datos: SesionDto) {
        require(datos.accessToken.isNotBlank() && datos.refreshToken.isNotBlank() && datos.user.id.isNotBlank())
        val guardada = SesionGuardada(datos, System.currentTimeMillis() + datos.expiresIn * 1000)
        almacen.guardar("sesion", gson.toJson(guardada)); almacen.borrar("modo_demo")
        sesion = guardada; mutableUsuario.value = usuarioDe(guardada)
        cargarPerfil(datos)
    }

    private suspend fun cargarPerfil(datos: SesionDto) {
        try {
            val perfil = api.obtenerPerfil("Bearer ${datos.accessToken}", "eq.${datos.user.id}").single()
            check(perfil.id == datos.user.id)
            val actual = checkNotNull(sesion).copy(perfil = perfil)
            almacen.guardar("sesion", gson.toJson(actual)); sesion = actual; mutableUsuario.value = usuarioDe(actual)
        } catch (cancelacion: CancellationException) { throw cancelacion
        } catch (_: Exception) { /* Se conserva el perfil básico para trabajar offline. */ }
    }

    private fun usuarioDe(s: SesionGuardada) = UsuarioSesion(s.datos.user.id, s.datos.user.email.orEmpty(),
        s.perfil?.nombre.orEmpty(), s.perfil?.rol == "encargado")
    private fun normalizarCorreo(correo: String): String {
        val valor = correo.trim().lowercase(Locale.ROOT)
        require(valor.matches(Regex("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")))
        return valor
    }
}
