package com.example.uptencuentra.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.uptencuentra.ui.components.AppScaffold
import com.example.uptencuentra.ui.theme.Dimensions
import com.example.uptencuentra.ui.viewmodel.SolicitudViewModel

@Composable
fun VerificationScreen(onBack: () -> Unit, onSaved: () -> Unit, viewModel: SolicitudViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val explicacion by viewModel.explicacion.collectAsStateWithLifecycle()
    AppScaffold("Solicitar recuperación", onBack) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(Dimensions.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimensions.itemSpacing)) {
            Text("Explica en privado cómo acreditarás que el objeto te pertenece. No escribas contraseñas ni PIN. El encargado comprobará la propiedad presencialmente.")
            if (state.guardada) {
                Text("Solicitud guardada. Puedes consultar su estado en Mis solicitudes. La demostración permanece local; una cuenta real enviará el pendiente al recuperar conexión.")
                Button(onClick = onSaved) { Text("Ver mis solicitudes") }
            } else {
                OutlinedTextField(explicacion, viewModel::editar, label = { Text("Explicación (10 a 1000 caracteres)") },
                    minLines = 4, enabled = !state.guardando, modifier = Modifier.fillMaxWidth())
                if (state.error) Text("No se pudo registrar. Revisa la explicación y comprueba que no tengas ya una solicitud activa para esta coincidencia.")
                Button(onClick = viewModel::enviar, enabled = !state.guardando) { Text(if (state.guardando) "Guardando…" else "Guardar solicitud") }
            }
        }
    }
}
