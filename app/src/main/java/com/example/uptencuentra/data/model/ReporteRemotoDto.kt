package com.example.uptencuentra.data.model

import com.example.uptencuentra.domain.model.*
import com.google.gson.annotations.SerializedName
import java.time.Instant
import java.time.LocalDate

data class ReporteRemotoDto(
    val id: String,
    @SerializedName("autor_id") val autorId: String,
    val tipo: String,
    val categoria: String,
    val descripcion: String,
    @SerializedName("fecha_evento") val fechaEvento: String,
    val zona: String,
    val referencia: String,
    val caracteristicas: CaracteristicasDto,
    @SerializedName("area_entrega") val areaEntrega: String?,
    val estado: String,
    val version: Int,
    @SerializedName("creado_en") val creadoEn: String,
    val particulares: CaracteristicasParticulares? = null,
    @SerializedName("foto_ruta") val fotoRuta: String? = null
) {
    fun aEntity() = ReporteMapper.aEntity(
        Reporte.restaurar(id, TipoReporte.valueOf(tipo), categoria, descripcion,
            LocalDate.parse(fechaEvento), UbicacionReporte(zona, referencia),
            CaracteristicasObjeto(caracteristicas.marca.orEmpty(), caracteristicas.modelo.orEmpty(),
                caracteristicas.color.orEmpty()), areaEntrega, EstadoReporte.valueOf(estado), version, particulares ?: CaracteristicasParticulares(), fotoRemota = fotoRuta),
        Instant.parse(creadoEn).toEpochMilli()).copy(cuentaId = autorId)
}
