package com.example.uptencuentra.ui.viewmodel

import androidx.lifecycle.*
import com.example.uptencuentra.data.repository.RecuperacionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatchesViewModel @Inject constructor(private val repository: RecuperacionRepository, savedState: SavedStateHandle) : ViewModel() {
    private val perdida: String = checkNotNull(savedState["objectId"])
    val coincidencias = repository.observarCoincidencias(perdida).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val mutableCargando = MutableStateFlow(false)
    val cargando = mutableCargando.asStateFlow()
    private val mutableError = MutableStateFlow(false)
    val error = mutableError.asStateFlow()
    init { buscar() }
    fun buscar() {
        if (cargando.value) return
        mutableCargando.value = true; mutableError.value = false
        viewModelScope.launch {
            try { repository.buscar(perdida)
            } catch (cancelacion: CancellationException) { throw cancelacion
            } catch (_: Exception) { mutableError.value = true
            } finally { mutableCargando.value = false }
        }
    }
}
