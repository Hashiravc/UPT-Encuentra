package com.example.uptencuentra.domain.model

data class Coincidencia(val id: String, val perdidaId: String, val categoria: String,
    val zona: String, val fecha: String, val puntaje: Int, val motivos: List<String>)

data class SolicitudRecuperacion(val id: String, val coincidenciaId: String, val perdidaId: String,
    val hallazgoId: String, val explicacion: String, val estado: String, val cuentaId: String)
