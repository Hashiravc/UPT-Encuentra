package com.example.uptencuentra.domain

import com.example.uptencuentra.domain.model.*
import com.example.uptencuentra.domain.service.MotorCoincidencias
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class MotorCoincidenciasTest {
    private val motor = MotorCoincidencias()
    private val fecha = LocalDate.of(2026, 11, 1)
    private fun reporte(tipo: TipoReporte, senal: String = "", modelo: String = "A54", dias: Long = 0) =
        Reporte.crear(tipo, "Celular", "Celular negro Samsung", fecha.plusDays(dias),
            UbicacionReporte("Lab-A", "Entrada"), CaracteristicasObjeto("Samsung", modelo, "Negro"),
            if (tipo == TipoReporte.ENCONTRADO) "Secretaría" else null, fecha.plusDays(40),
            particulares = CaracteristicasParticulares(senal = senal))

    @Test fun datosGeneralesSolosNoGeneranCoincidencia() {
        assertNull(motor.comparar(reporte(TipoReporte.PERDIDO), reporte(TipoReporte.ENCONTRADO)))
    }
    @Test fun normalizaTildesYOrdenSinRevelarLaPrueba() {
        val resultado = motor.comparar(reporte(TipoReporte.PERDIDO, "Rayón diagonal único"),
            reporte(TipoReporte.ENCONTRADO, "UNICO rayón diagonal"))!!
        assertTrue(resultado.puntaje >= 35)
        assertFalse(resultado.motivos.joinToString().contains("diagonal"))
    }
    @Test fun modeloContradictorioExcluyeAunqueLaSenalCoincida() {
        assertNull(motor.comparar(reporte(TipoReporte.PERDIDO, "Esquina rota"),
            reporte(TipoReporte.ENCONTRADO, "Esquina rota", "S24")))
    }
    @Test fun fechaFueraDelIntervaloExcluye() {
        assertNull(motor.comparar(reporte(TipoReporte.PERDIDO, "Esquina rota"),
            reporte(TipoReporte.ENCONTRADO, "Esquina rota", dias = 31)))
    }
    @Test fun canceladoNoParticipa() {
        assertNull(motor.comparar(reporte(TipoReporte.PERDIDO, "Esquina rota").cancelar(),
            reporte(TipoReporte.ENCONTRADO, "Esquina rota")))
    }
}
