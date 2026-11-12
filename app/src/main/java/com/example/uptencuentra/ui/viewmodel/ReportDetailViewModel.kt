package com.example.uptencuentra.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uptencuentra.domain.model.Reporte
import com.example.uptencuentra.domain.repository.ReporteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportDetailState(val reporte: Reporte? = null, val cargando: Boolean = true,
                             val error: Boolean = false)
data class CancellationState(val procesando: Boolean = false, val error: Boolean = false)

@HiltViewModel
class ReportDetailViewModel @Inject constructor(
    private val repository: ReporteRepository,
    savedState: SavedStateHandle
) : ViewModel() {
    private val id: String = checkNotNull(savedState["objectId"])
    val state = repository.observarReporte(id)
        .map { ReportDetailState(reporte = it, cargando = false) }
        .catch { emit(ReportDetailState(cargando = false, error = true)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportDetailState())
    private val mutableCancellation = MutableStateFlow(CancellationState())
    val cancellation = mutableCancellation.asStateFlow()

    // Solicita la cancelación y actualiza el estado de progreso o error de la pantalla.
    fun cancelar() {
        if (cancellation.value.procesando) return
        mutableCancellation.value = CancellationState(procesando = true)
        viewModelScope.launch {
            try {
                repository.cancelar(id)
                mutableCancellation.value = CancellationState()
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                mutableCancellation.value = CancellationState(error = true)
            }
        }
    }
}
