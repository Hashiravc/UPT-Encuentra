package com.example.uptencuentra.domain.model

import java.time.LocalDate
import java.util.UUID

class Reporte private constructor(
    val id: String,
    val tipo: TipoReporte,
    val categoria: String,
    val descripcion: String,
    val fechaEvento: LocalDate,
    val ubicacion: UbicacionReporte,
    val caracteristicas: CaracteristicasObjeto,
    val areaEntrega: String?,
    val estado: EstadoReporte,
    val version: Int,
    val particulares: CaracteristicasParticulares,
    val fotoLocal: String?,
    val fotoRemota: String?
) {
    init {
        require(id.isNotBlank() && categoria.isNotBlank() && categoria.length <= 80)
        require(descripcion.length in 10..500)
        require(tipo != TipoReporte.ENCONTRADO || !areaEntrega.isNullOrBlank())
        require(areaEntrega == null || areaEntrega.length <= 120)
        require(version > 0)
    }

    // Cancela un reporte activo y aumenta su versión sin modificar sus características.
    fun cancelar(): Reporte {
        check(estado == EstadoReporte.ACTIVO)
        return Reporte(id, tipo, categoria, descripcion, fechaEvento, ubicacion,
            caracteristicas, areaEntrega, EstadoReporte.CANCELADO, version + 1, particulares, fotoLocal, fotoRemota)
    }

    companion object {
        fun crear(tipo: TipoReporte, categoria: String, descripcion: String,
                  fechaEvento: LocalDate, ubicacion: UbicacionReporte,
                  caracteristicas: CaracteristicasObjeto, areaEntrega: String?,
                  hoy: LocalDate = LocalDate.now(), id: String = UUID.randomUUID().toString(),
                  particulares: CaracteristicasParticulares = CaracteristicasParticulares(),
                  fotoLocal: String? = null): Reporte {
            require(!fechaEvento.isAfter(hoy))
            return Reporte(id, tipo, categoria.trim(),
                descripcion.trim(), fechaEvento, ubicacion, caracteristicas,
                if (tipo == TipoReporte.ENCONTRADO) areaEntrega?.trim() else null,
                EstadoReporte.ACTIVO, 1, particulares, fotoLocal, null)
        }

        // Reconstruye el reporte persistido conservando su identidad, estado y versión.
        fun restaurar(id: String, tipo: TipoReporte, categoria: String, descripcion: String,
                      fechaEvento: LocalDate, ubicacion: UbicacionReporte,
                      caracteristicas: CaracteristicasObjeto, areaEntrega: String?,
                      estado: EstadoReporte, version: Int,
                      particulares: CaracteristicasParticulares = CaracteristicasParticulares(),
                      fotoLocal: String? = null, fotoRemota: String? = null): Reporte =
            Reporte(id, tipo, categoria, descripcion, fechaEvento, ubicacion,
                caracteristicas, areaEntrega, estado, version, particulares, fotoLocal, fotoRemota)
    }
}
