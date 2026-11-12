package com.example.uptencuentra.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uptencuentra.domain.model.Reporte
import com.example.uptencuentra.domain.repository.ReporteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class ReportsState(val reportes: List<Reporte> = emptyList(), val cargando: Boolean = true,
                        val error: Boolean = false)

@HiltViewModel
class ReportsViewModel @Inject constructor(repository: ReporteRepository) : ViewModel() {
    val state = repository.observarReportes()
        .map { ReportsState(reportes = it, cargando = false) }
        .catch { emit(ReportsState(cargando = false, error = true)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportsState())
}
