package com.example.uptencuentra.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uptencuentra.data.local.AppDatabase
import com.example.uptencuentra.data.repository.OfflineFirstReporteRepository
import com.example.uptencuentra.data.sync.*
import com.example.uptencuentra.domain.repository.SesionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sesion: SesionRepository,
    private val programador: ProgramadorSincronizacion,
    repository: OfflineFirstReporteRepository,
    database: AppDatabase,
    sincronizador: SincronizadorReportes
) : ViewModel() {
    private val reportes = repository
    val estadoSync = sincronizador.estado
    val sinCuenta = database.reporteDao().observarSinCuenta()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
    val pendientes = sesion.usuario.flatMapLatest {
        if (it == null) flowOf(0) else database.pendienteDao().observarCantidad(it.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
    private val mutableImportando = MutableStateFlow(false)
    val importando = mutableImportando.asStateFlow()
    private val mutableError = MutableStateFlow(false)
    val error = mutableError.asStateFlow()

    init { sincronizar() }
    fun sincronizar() { sesion.usuario.value?.takeUnless { it.esDemostracion }?.let { programador.programar(it.id) } }

    fun importar() {
        if (importando.value) return
        mutableImportando.value = true
        mutableError.value = false
        viewModelScope.launch {
            try { reportes.importarDemostracion()
            } catch (cancelacion: CancellationException) { throw cancelacion
            } catch (_: Exception) { mutableError.value = true
            } finally { mutableImportando.value = false }
        }
    }
}
