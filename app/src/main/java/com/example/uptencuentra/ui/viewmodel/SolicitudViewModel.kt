package com.example.uptencuentra.ui.viewmodel

import androidx.lifecycle.*
import com.example.uptencuentra.data.repository.RecuperacionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SolicitudState(val guardando: Boolean = false, val guardada: Boolean = false, val error: Boolean = false)

@HiltViewModel
class SolicitudViewModel @Inject constructor(private val repository: RecuperacionRepository,
    private val savedState: SavedStateHandle) : ViewModel() {
    private val coincidencia: String = checkNotNull(savedState["matchId"])
    val explicacion = savedState.getStateFlow("explicacion", "")
    private val mutableState = MutableStateFlow(SolicitudState(guardada = savedState["enviada"] ?: false))
    val state = mutableState.asStateFlow()
    fun editar(texto: String) { if (!state.value.guardando && !state.value.guardada) savedState["explicacion"] = texto }
    fun enviar() {
        if (state.value.guardando || state.value.guardada) return
        if (explicacion.value.trim().length !in 10..1000) { mutableState.value = SolicitudState(error = true); return }
        mutableState.value = SolicitudState(guardando = true)
        viewModelScope.launch {
            try { repository.solicitar(coincidencia, explicacion.value); savedState["enviada"] = true; mutableState.value = SolicitudState(guardada = true)
            } catch (cancelacion: CancellationException) { throw cancelacion
            } catch (_: Exception) { mutableState.value = SolicitudState(error = true) }
        }
    }
}
