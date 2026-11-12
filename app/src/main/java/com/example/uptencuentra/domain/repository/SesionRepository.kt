package com.example.uptencuentra.domain.repository

import com.example.uptencuentra.domain.model.UsuarioSesion
import kotlinx.coroutines.flow.StateFlow

interface SesionRepository {
    val usuario: StateFlow<UsuarioSesion?>
    suspend fun entrarDemostracion()
    suspend fun iniciarSesion(correo: String, clave: String)
    suspend fun registrar(nombre: String, correo: String, clave: String): Boolean
    suspend fun tokenValido(usuarioId: String): String
    suspend fun cerrarSesion()
}
class SesionExpiradaException : Exception()
