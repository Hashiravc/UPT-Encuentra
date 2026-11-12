package com.example.uptencuentra.domain.validation

import com.example.uptencuentra.domain.model.TipoReporte
import java.time.LocalDate
import java.time.format.DateTimeParseException

data class FormularioReporte(
    val categoria: String = "",
    val descripcion: String = "",
    val fecha: String = LocalDate.now().toString(),
    val zona: String = "",
    val referencia: String = "",
    val marca: String = "",
    val modelo: String = "",
    val color: String = "",
    val areaEntrega: String = "",
    val senal: String = "",
    val accesorios: String = "",
    val inscripcion: String = "",
    val fotoLocal: String? = null
)

enum class CampoReporte { CATEGORIA, DESCRIPCION, FECHA, ZONA, REFERENCIA, MARCA, MODELO, COLOR, AREA_ENTREGA, SENAL, ACCESORIOS, INSCRIPCION, FOTO }
enum class ErrorCampo { OBLIGATORIO, MUY_LARGO, DESCRIPCION_CORTA, FECHA_INVALIDA, FECHA_FUTURA }

class ValidarReporte {
    fun validar(formulario: FormularioReporte, tipo: TipoReporte,
                hoy: LocalDate = LocalDate.now()): Map<CampoReporte, ErrorCampo> = buildMap {
        fun texto(campo: CampoReporte, valor: String, maximo: Int, obligatorio: Boolean = true) {
            when {
                obligatorio && valor.isBlank() -> put(campo, ErrorCampo.OBLIGATORIO)
                valor.trim().length > maximo -> put(campo, ErrorCampo.MUY_LARGO)
            }
        }
        texto(CampoReporte.CATEGORIA, formulario.categoria, 80)
        texto(CampoReporte.DESCRIPCION, formulario.descripcion, 500)
        if (formulario.descripcion.trim().length in 1..9) {
            put(CampoReporte.DESCRIPCION, ErrorCampo.DESCRIPCION_CORTA)
        }
        texto(CampoReporte.ZONA, formulario.zona, 80)
        texto(CampoReporte.REFERENCIA, formulario.referencia, 160)
        texto(CampoReporte.MARCA, formulario.marca, 80, false)
        texto(CampoReporte.MODELO, formulario.modelo, 80, false)
        texto(CampoReporte.COLOR, formulario.color, 80, false)
        if (tipo == TipoReporte.ENCONTRADO) texto(CampoReporte.AREA_ENTREGA, formulario.areaEntrega, 120)
        texto(CampoReporte.SENAL, formulario.senal, 160, false)
        texto(CampoReporte.ACCESORIOS, formulario.accesorios, 160, false)
        texto(CampoReporte.INSCRIPCION, formulario.inscripcion, 160, false)
        if (listOf(formulario.senal, formulario.accesorios, formulario.inscripcion).all { it.isBlank() }) {
            put(CampoReporte.SENAL, ErrorCampo.OBLIGATORIO)
        }
        if (tipo == TipoReporte.ENCONTRADO && formulario.fotoLocal.isNullOrBlank()) put(CampoReporte.FOTO, ErrorCampo.OBLIGATORIO)
        try {
            if (LocalDate.parse(formulario.fecha).isAfter(hoy)) put(CampoReporte.FECHA, ErrorCampo.FECHA_FUTURA)
        } catch (_: DateTimeParseException) {
            put(CampoReporte.FECHA, ErrorCampo.FECHA_INVALIDA)
        }
    }
}
