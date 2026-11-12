package com.example.uptencuentra.data.model

import com.example.uptencuentra.data.local.entity.ReporteEntity
import com.example.uptencuentra.domain.model.*
import java.time.LocalDate

object ReporteMapper {
    fun aEntity(reporte: Reporte, creadoEn: Long): ReporteEntity = with(reporte) {
        ReporteEntity(id, tipo.name, categoria, descripcion, fechaEvento.toString(),
            ubicacion.zona, ubicacion.referencia, caracteristicas.marca, caracteristicas.modelo,
            caracteristicas.color, areaEntrega, estado.name, version, creadoEn,
            senal = particulares.senal, accesorios = particulares.accesorios, inscripcion = particulares.inscripcion,
            fotoLocal = fotoLocal, fotoRemota = fotoRemota)
    }

    fun aDominio(entity: ReporteEntity): Reporte = with(entity) {
        Reporte.restaurar(id, TipoReporte.valueOf(tipo), categoria, descripcion,
            LocalDate.parse(fechaEvento), UbicacionReporte(zona, referencia),
            CaracteristicasObjeto(marca, modelo, color), areaEntrega,
            EstadoReporte.valueOf(estado), version, CaracteristicasParticulares(senal, accesorios, inscripcion), fotoLocal, fotoRemota)
    }

    fun aDto(reporte: Reporte): ReporteDto = with(reporte) {
        ReporteDto(id, tipo.name, categoria, descripcion, fechaEvento.toString(),
            ubicacion.zona, ubicacion.referencia,
            CaracteristicasDto(caracteristicas.marca, caracteristicas.modelo, caracteristicas.color),
            areaEntrega, estado.name, version, particulares, fotoLocal, fotoRemota)
    }
}
