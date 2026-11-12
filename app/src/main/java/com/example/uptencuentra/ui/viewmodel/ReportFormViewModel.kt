package com.example.uptencuentra.ui.viewmodel

import android.net.Uri
import com.example.uptencuentra.data.photos.AlmacenFotos
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uptencuentra.domain.model.*
import com.example.uptencuentra.domain.repository.ReporteRepository
import com.example.uptencuentra.domain.validation.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

data class ReportFormState(
    val formulario: FormularioReporte = FormularioReporte(),
    val errores: Map<CampoReporte, ErrorCampo> = emptyMap(),
    val guardando: Boolean = false,
    val guardado: Boolean = false,
    val errorGuardado: Boolean = false,
    val procesandoFoto: Boolean = false,
    val errorFoto: Boolean = false
)

@HiltViewModel
class ReportFormViewModel @Inject constructor(
    private val repository: ReporteRepository,
    private val validarReporte: ValidarReporte,
    private val savedState: SavedStateHandle,
    private val fotos: AlmacenFotos
) : ViewModel() {
    private val borradorId: String = savedState.get<String>("borradorId")
        ?: UUID.randomUUID().toString().also { savedState["borradorId"] = it }
    private val mutableState = MutableStateFlow(ReportFormState(formulario = restaurarFormulario(),
        guardado = savedState["guardado"] ?: false))
    val state = mutableState.asStateFlow()

    // Actualiza un campo del formulario y conserva el borrador al recrear la pantalla.
    fun editar(campo: CampoReporte, valor: String) {
        if (state.value.guardando || state.value.guardado) return
        val formulario = state.value.formulario
        val actualizado = when (campo) {
            CampoReporte.CATEGORIA -> formulario.copy(categoria = valor)
            CampoReporte.DESCRIPCION -> formulario.copy(descripcion = valor)
            CampoReporte.FECHA -> formulario.copy(fecha = valor)
            CampoReporte.ZONA -> formulario.copy(zona = valor)
            CampoReporte.REFERENCIA -> formulario.copy(referencia = valor)
            CampoReporte.MARCA -> formulario.copy(marca = valor)
            CampoReporte.MODELO -> formulario.copy(modelo = valor)
            CampoReporte.COLOR -> formulario.copy(color = valor)
            CampoReporte.AREA_ENTREGA -> formulario.copy(areaEntrega = valor)
            CampoReporte.SENAL -> formulario.copy(senal = valor)
            CampoReporte.ACCESORIOS -> formulario.copy(accesorios = valor)
            CampoReporte.INSCRIPCION -> formulario.copy(inscripcion = valor)
            CampoReporte.FOTO -> return
        }
        savedState[campo.name] = valor
        mutableState.value = state.value.copy(formulario = actualizado,
            errores = state.value.errores - campo, errorGuardado = false)
    }

    fun seleccionarFoto(uri: Uri) {
        if (state.value.guardando || state.value.guardado || state.value.procesandoFoto) return
        mutableState.value = state.value.copy(procesandoFoto = true, errorFoto = false)
        viewModelScope.launch {
            try {
                val ruta = fotos.importar(uri)
                savedState["fotoLocal"] = ruta
                mutableState.value = state.value.copy(formulario = state.value.formulario.copy(fotoLocal = ruta),
                    procesandoFoto = false, errores = state.value.errores - CampoReporte.FOTO)
            } catch (cancelacion: CancellationException) { throw cancelacion
            } catch (_: Exception) { mutableState.value = state.value.copy(procesandoFoto = false, errorFoto = true) }
        }
    }

    // Valida el formulario y guarda una sola vez el reporte junto con su pendiente.
    fun guardar(tipo: TipoReporte) {
        if (state.value.guardando || state.value.guardado || state.value.procesandoFoto) return
        val formulario = state.value.formulario
        val errores = validarReporte.validar(formulario, tipo)
        if (errores.isNotEmpty()) {
            mutableState.value = state.value.copy(errores = errores)
            return
        }
        mutableState.value = state.value.copy(guardando = true, errorGuardado = false)
        viewModelScope.launch {
            try {
                val reporte = with(formulario) {
                    Reporte.crear(tipo, categoria, descripcion, LocalDate.parse(fecha),
                        UbicacionReporte(zona.trim(), referencia.trim()),
                        CaracteristicasObjeto(marca.trim(), modelo.trim(), color.trim()), areaEntrega, id = borradorId, particulares = CaracteristicasParticulares(senal.trim(), accesorios.trim(), inscripcion.trim()), fotoLocal = fotoLocal)
                }
                repository.guardar(reporte)
                savedState["guardado"] = true
                mutableState.value = state.value.copy(guardando = false, guardado = true)
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                mutableState.value = state.value.copy(guardando = false, errorGuardado = true)
            }
        }
    }

    private fun restaurarFormulario() = FormularioReporte(
        categoria = savedState[CampoReporte.CATEGORIA.name] ?: "",
        descripcion = savedState[CampoReporte.DESCRIPCION.name] ?: "",
        fecha = savedState[CampoReporte.FECHA.name] ?: LocalDate.now().toString(),
        zona = savedState[CampoReporte.ZONA.name] ?: "",
        referencia = savedState[CampoReporte.REFERENCIA.name] ?: "",
        marca = savedState[CampoReporte.MARCA.name] ?: "",
        modelo = savedState[CampoReporte.MODELO.name] ?: "",
        color = savedState[CampoReporte.COLOR.name] ?: "",
        areaEntrega = savedState[CampoReporte.AREA_ENTREGA.name] ?: "",
        senal = savedState[CampoReporte.SENAL.name] ?: "",
        accesorios = savedState[CampoReporte.ACCESORIOS.name] ?: "",
        inscripcion = savedState[CampoReporte.INSCRIPCION.name] ?: "",
        fotoLocal = savedState["fotoLocal"]
    )
}
