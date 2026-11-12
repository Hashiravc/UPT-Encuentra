package com.example.uptencuentra.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uptencuentra.data.sync.ProgramadorSincronizacion
import com.example.uptencuentra.domain.repository.SesionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginState(val procesando: Boolean = false, val error: Boolean = false, val confirmarCorreo: Boolean = false)

@HiltViewModel
class SessionViewModel @Inject constructor(private val sesion: SesionRepository,
    private val programador: ProgramadorSincronizacion) : ViewModel() {
    val usuario = sesion.usuario
    private val mutableState = MutableStateFlow(LoginState())
    val state = mutableState.asStateFlow()

    fun acceder(nombre: String, correo: String, clave: String, registro: Boolean) {
        if (state.value.procesando) return
        mutableState.value = LoginState(procesando = true)
        viewModelScope.launch {
            try {
                val listo = if (registro) sesion.registrar(nombre, correo, clave)
                    else { sesion.iniciarSesion(correo, clave); true }
                if (listo) sesion.usuario.value?.let { programador.programar(it.id) }
                mutableState.value = LoginState(confirmarCorreo = !listo)
            } catch (cancelacion: CancellationException) { throw cancelacion
            } catch (_: Exception) { mutableState.value = LoginState(error = true) }
        }
    }

    fun entrarDemostracion() {
        if (state.value.procesando) return
        mutableState.value = LoginState(procesando = true)
        viewModelScope.launch {
            try { programador.detener(); sesion.entrarDemostracion(); mutableState.value = LoginState()
            } catch (cancelacion: CancellationException) { throw cancelacion
            } catch (_: Exception) { mutableState.value = LoginState(error = true) }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch { programador.detener(); sesion.cerrarSesion(); mutableState.value = LoginState() }
    }
}
