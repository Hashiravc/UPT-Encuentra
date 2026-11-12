package com.example.uptencuentra.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uptencuentra.data.repository.RecuperacionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class RecoveryViewModel @Inject constructor(repository: RecuperacionRepository) : ViewModel() {
    val solicitudes = repository.observarSolicitudes().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
