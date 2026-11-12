package com.example.uptencuentra.domain.model

data class UsuarioSesion(val id: String, val correo: String, val nombre: String = "", val esEncargado: Boolean = false) {
    val esDemostracion: Boolean get() = id.isBlank()
}
