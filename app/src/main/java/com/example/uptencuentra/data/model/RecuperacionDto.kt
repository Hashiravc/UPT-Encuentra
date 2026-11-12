package com.example.uptencuentra.data.model

import com.google.gson.annotations.SerializedName
import com.example.uptencuentra.data.local.entity.*

data class BuscarCoincidenciasRequest(@SerializedName("perdida_id") val perdidaId: String)
data class CoincidenciaDto(val id: String, @SerializedName("perdida_id") val perdidaId: String,
    val categoria: String, val zona: String, @SerializedName("fecha_evento") val fecha: String,
    val puntaje: Int, val motivos: List<String>)
data class SolicitudDto(val id: String, @SerializedName("coincidencia_id") val coincidenciaId: String,
    @SerializedName("perdida_id") val perdidaId: String, @SerializedName("hallazgo_id") val hallazgoId: String,
    val explicacion: String, val estado: String, @SerializedName("solicitante_id") val cuentaId: String,
    @SerializedName("nombre_solicitante") val nombre: String? = null) {
    fun aEntity() = SolicitudEntity(id, coincidenciaId, perdidaId, hallazgoId, explicacion, estado, cuentaId)
}
data class NuevaSolicitudDto(val id: String, val coincidenciaId: String, val explicacion: String)
data class SolicitudRequest(val datos: NuevaSolicitudDto)
data class RecepcionRequest(@SerializedName("reporte_id") val reporteId: String, val area: String)
data class ResolverRequest(@SerializedName("solicitud_id") val solicitudId: String, val accion: String,
    @SerializedName("propiedad_verificada") val propiedadVerificada: Boolean)
