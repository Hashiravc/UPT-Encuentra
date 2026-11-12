package com.example.uptencuentra.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.uptencuentra.ui.components.AppScaffold
import com.example.uptencuentra.ui.theme.Dimensions
import com.example.uptencuentra.ui.viewmodel.RecoveryViewModel

@Composable
fun RecoveryScreen(onBack: () -> Unit, viewModel: RecoveryViewModel = hiltViewModel()) {
    val solicitudes by viewModel.solicitudes.collectAsStateWithLifecycle()
    AppScaffold("Mis solicitudes", onBack) { padding ->
        LazyColumn(Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(Dimensions.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimensions.itemSpacing)) {
            item { Text("La solicitud no acredita propiedad. La entrega se confirma presencialmente por el encargado.") }
            if (solicitudes.isEmpty()) item { Text("Todavía no tienes solicitudes.") }
            items(solicitudes, key = { it.id }) { s -> Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(Dimensions.screenPadding)) {
                    Text(when(s.estado) {
                        "PENDIENTE_ENVIO" -> "Pendiente de envío"
                        "PENDIENTE_LOCAL" -> "Solicitud local de demostración"
                        "PENDIENTE" -> "Pendiente de revisión"
                        "APROBADA" -> "Propiedad validada; pendiente de entrega"
                        "ENTREGADA" -> "Objeto entregado"
                        "RECHAZADA" -> "Solicitud rechazada"
                        else -> "Revisa la sincronización"
                    }, style = MaterialTheme.typography.titleMedium)
                    Text(s.explicacion)
                }
            } }
        }
    }
}
