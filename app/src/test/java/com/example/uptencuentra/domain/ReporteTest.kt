package com.example.uptencuentra.domain

import com.example.uptencuentra.domain.model.*
import com.example.uptencuentra.domain.validation.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class ReporteTest {
    private val hoy = LocalDate.of(2026, 11, 11)

    private fun reporte(tipo: TipoReporte = TipoReporte.PERDIDO, area: String? = null) =
        Reporte.crear(tipo, "Celular", "Celular negro con funda azul", hoy,
            UbicacionReporte("Facultad", "Junto a la entrada"),
            CaracteristicasObjeto("Samsung", "", "Negro"), area, hoy)

    @Test fun cancelarConservaIdentidadYCaracteristicasSinMutarOriginal() {
        val original = reporte()
        val cancelado = original.cancelar()
        assertEquals(original.id, cancelado.id)
        assertEquals(original.descripcion, cancelado.descripcion)
        assertEquals(original.ubicacion, cancelado.ubicacion)
        assertEquals(original.caracteristicas, cancelado.caracteristicas)
        assertEquals(EstadoReporte.ACTIVO, original.estado)
        assertEquals(EstadoReporte.CANCELADO, cancelado.estado)
        assertEquals(2, cancelado.version)
    }

    @Test(expected = IllegalStateException::class)
    fun noSeCancelaDosVeces() { reporte().cancelar().cancelar() }

    @Test(expected = IllegalArgumentException::class)
    fun encontradoExigeAreaUniversitaria() { reporte(TipoReporte.ENCONTRADO) }

    @Test fun perdidoNoConservaUnAreaDeEntregaAjena() {
        assertNull(reporte(area = "Área ingresada por error").areaEntrega)
    }

    @Test(expected = IllegalArgumentException::class)
    fun noSeCreanFechasFuturasFueraDelFormulario() {
        Reporte.crear(TipoReporte.PERDIDO, "Celular", "Descripción del celular", hoy.plusDays(1),
            UbicacionReporte("Campus", "Entrada"), CaracteristicasObjeto("", "", ""), null, hoy)
    }

    @Test fun validacionDetectaCamposVaciosFechaYDescripcion() {
        val errores = ValidarReporte().validar(FormularioReporte(descripcion = "breve", fecha = "2026-02-30"),
            TipoReporte.ENCONTRADO, hoy)
        assertEquals(ErrorCampo.DESCRIPCION_CORTA, errores[CampoReporte.DESCRIPCION])
        assertEquals(ErrorCampo.FECHA_INVALIDA, errores[CampoReporte.FECHA])
        assertEquals(ErrorCampo.OBLIGATORIO, errores[CampoReporte.AREA_ENTREGA])
        assertFalse(errores.containsKey(CampoReporte.MARCA))
    }

    @Test fun validacionAceptaReporteMinimoSinMarcaModeloNiColor() {
        val formulario = FormularioReporte(categoria = "Cuaderno", descripcion = "Cuaderno de apuntes azul",
            fecha = hoy.toString(), zona = "Campus", referencia = "Patio", senal = "Esquina rota")
        assertTrue(ValidarReporte().validar(formulario, TipoReporte.PERDIDO, hoy).isEmpty())
    }

    @Test fun validacionRechazaFechaFuturaYTextosLargos() {
        val errores = ValidarReporte().validar(FormularioReporte(fecha = hoy.plusDays(1).toString(),
            marca = "x".repeat(81)), TipoReporte.PERDIDO, hoy)
        assertEquals(ErrorCampo.FECHA_FUTURA, errores[CampoReporte.FECHA])
        assertEquals(ErrorCampo.MUY_LARGO, errores[CampoReporte.MARCA])
    }
}
