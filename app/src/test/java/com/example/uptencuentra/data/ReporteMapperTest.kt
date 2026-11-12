package com.example.uptencuentra.data

import com.example.uptencuentra.data.model.*
import com.example.uptencuentra.domain.model.*
import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class ReporteMapperTest {
    @Test fun roomYGsonConservanDatosAlIrYVolver() {
        val original = Reporte.crear(TipoReporte.ENCONTRADO, "Cuaderno", "Cuaderno azul con etiqueta",
            LocalDate.of(2026, 1, 1), UbicacionReporte("Patio", "Entrada de la facultad"),
            CaracteristicasObjeto("", "", "Azul"), "Área universitaria").cancelar()
        val restaurado = ReporteMapper.aDominio(ReporteMapper.aEntity(original, 100L))
        val dto = ReporteMapper.aDto(restaurado)
        val gson = Gson()
        val desdeJson = gson.fromJson(gson.toJson(dto), ReporteDto::class.java)
        assertEquals(dto, desdeJson)
        assertEquals(original.id, restaurado.id)
        assertEquals(original.estado, restaurado.estado)
        assertEquals(original.version, restaurado.version)
        assertEquals(original.caracteristicas, restaurado.caracteristicas)
    }
}
