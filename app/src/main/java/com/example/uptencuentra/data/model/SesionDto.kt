package com.example.uptencuentra.data.model

import com.google.gson.annotations.SerializedName

data class UsuarioDto(val id: String, val email: String?)
data class PerfilDto(val id: String, val nombre: String, val correo: String, val rol: String)
data class SesionDto(@SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("expires_in") val expiresIn: Long, val user: UsuarioDto)
data class AccesoDto(val email: String, val password: String)
data class RegistroDto(val email: String, val password: String, val data: Map<String,String>)
data class RegistroRespuestaDto(@SerializedName("access_token") val accessToken: String?,
    @SerializedName("refresh_token") val refreshToken: String?,
    @SerializedName("expires_in") val expiresIn: Long?, val user: UsuarioDto?)
data class RenovarSesionDto(@SerializedName("refresh_token") val refreshToken: String)
data class GuardarReporteRequest(val datos: ReporteDto)
data class PaginaPropia(val inicio: Int = 0, val limite: Int = 200)
