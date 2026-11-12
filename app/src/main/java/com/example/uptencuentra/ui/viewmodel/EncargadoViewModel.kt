package com.example.uptencuentra.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uptencuentra.data.model.*
import com.example.uptencuentra.data.repository.RecuperacionRepository
import com.example.uptencuentra.data.sync.SincronizadorFotos
import com.example.uptencuentra.domain.repository.SesionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HallazgoRevision(val datos: ReporteRemotoDto, val foto: String?)
data class EncargadoState(val cargando: Boolean = false, val error: Boolean = false,
    val hallazgos: List<HallazgoRevision> = emptyList(), val solicitudes: List<SolicitudDto> = emptyList())

@HiltViewModel
class EncargadoViewModel @Inject constructor(private val repository: RecuperacionRepository,
    private val sesion: SesionRepository, private val fotos: SincronizadorFotos) : ViewModel() {
    private val mutableState = MutableStateFlow(EncargadoState())
    val state = mutableState.asStateFlow()
    init { actualizar() }
    fun actualizar() { ejecutar { } }
    fun recibir(id: String, area: String) { ejecutar { repository.recibir(id, area) } }
    fun resolver(id: String, accion: String, verificado: Boolean) { ejecutar { repository.resolver(id, accion, verificado) } }

    private fun ejecutar(accion: suspend () -> Unit) {
        if (state.value.cargando) return
        mutableState.value = state.value.copy(cargando = true, error = false)
        viewModelScope.launch {
            try {
                accion()
                val cuenta = checkNotNull(sesion.usuario.value)
                val token = sesion.tokenValido(cuenta.id)
                val hallazgos = repository.hallazgosEncargado().map { HallazgoRevision(it, fotos.localizar(it, token, state.value.hallazgos.find { anterior -> anterior.datos.id == it.id }?.foto)) }
                mutableState.value = EncargadoState(hallazgos = hallazgos, solicitudes = repository.solicitudesEncargado())
            } catch (cancelacion: CancellationException) { throw cancelacion
            } catch (_: Exception) { mutableState.value = state.value.copy(cargando = false, error = true) }
        }
    }
}
