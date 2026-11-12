package com.example.uptencuentra.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.uptencuentra.ui.components.*
import com.example.uptencuentra.ui.theme.Dimensions
import com.example.uptencuentra.ui.viewmodel.EncargadoViewModel

@Composable
fun EncargadoScreen(onBack: () -> Unit, viewModel: EncargadoViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var recibir by remember { mutableStateOf<String?>(null) }
    var area by remember { mutableStateOf("") }
    var solicitud by remember { mutableStateOf<String?>(null) }
    var accion by remember { mutableStateOf("") }
    var verificado by remember { mutableStateOf(false) }
    AppScaffold("Recepción y entrega", onBack) { padding ->
        LazyColumn(Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(Dimensions.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimensions.itemSpacing)) {
            item { Text("Las recepciones y entregas requieren conexión y comprobación presencial.") }
            item { OutlinedButton(onClick = viewModel::actualizar, enabled = !state.cargando) { Text("Actualizar") } }
            if (state.cargando) item { CircularProgressIndicator() }
            if (state.error) item { Text("No se pudo completar la acción. Revisa la conexión, los permisos y el estado del objeto.") }
            item { Text("Hallazgos", style = MaterialTheme.typography.titleLarge) }
            state.hallazgos.forEach { h -> item(key = "h-${h.datos.id}") {
                Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(Dimensions.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.compactSpacing)) {
                    Text("${h.datos.categoria} · ${h.datos.zona}", style = MaterialTheme.typography.titleMedium)
                    Text(h.datos.descripcion)
                    FotoReporteView(h.foto)
                    h.datos.particulares?.let { Text(listOf(it.senal, it.accesorios, it.inscripcion).filter { t -> t.isNotBlank() }.joinToString(" · ")) }
                    Text(if (h.datos.estado == "EN_CUSTODIA") "En custodia" else "Pendiente de recepción")
                    if (h.datos.estado == "ACTIVO") Button(enabled = !state.cargando, onClick = {
                        recibir = h.datos.id; area = h.datos.areaEntrega.orEmpty(); verificado = false
                    }) { Text("Registrar recepción") }
                } }
            } }
            item { Text("Solicitudes", style = MaterialTheme.typography.titleLarge) }
            state.solicitudes.forEach { s -> item(key = "s-${s.id}") {
                Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(Dimensions.screenPadding)) {
                    val objeto = state.hallazgos.find { it.datos.id == s.hallazgoId }
                    Text(objeto?.let { "${it.datos.categoria} · ${it.datos.zona}" } ?: "Objeto: ${s.hallazgoId.takeLast(8)}",
                        style = MaterialTheme.typography.titleMedium)
                    objeto?.let { Text(it.datos.descripcion) }
                    Text(s.nombre ?: "Solicitante", style = MaterialTheme.typography.titleMedium)
                    Text(s.explicacion)
                    Text("Estado: ${s.estado}")
                    if (s.estado == "PENDIENTE" || s.estado == "APROBADA") Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.compactSpacing)) {
                        TextButton(enabled = !state.cargando, onClick = { solicitud = s.id; accion = "RECHAZADA"; verificado = false }) { Text("Rechazar") }
                        Button(enabled = !state.cargando, onClick = {
                            solicitud = s.id; accion = if (s.estado == "PENDIENTE") "APROBADA" else "ENTREGADA"; verificado = false
                        }) { Text(if (s.estado == "PENDIENTE") "Validar propiedad" else "Confirmar entrega") }
                    }
                } }
            } }
        }
    }
    if (recibir != null || solicitud != null) AlertDialog(onDismissRequest = { recibir = null; solicitud = null },
        title = { Text(if (recibir != null) "Registrar recepción" else "Confirmar acción") },
        text = { Column {
            if (recibir != null) OutlinedTextField(area, { area = it }, label = { Text("Área universitaria de custodia") })
            if (recibir != null || accion != "RECHAZADA") Row {
                Checkbox(verificado, { verificado = it })
                Text(if (recibir != null) "Confirmo que recibí físicamente el objeto." else "He comprobado presencialmente la propiedad y la acción que registraré.")
            } else Text("Se registrará el rechazo de esta solicitud.")
        } },
        confirmButton = { TextButton(enabled = if (recibir != null) verificado && area.trim().length in 1..120 else accion == "RECHAZADA" || verificado,
            onClick = {
                recibir?.let { viewModel.recibir(it, area) }
                solicitud?.let { viewModel.resolver(it, accion, verificado) }
                recibir = null; solicitud = null
            }) { Text("Confirmar") } },
        dismissButton = { TextButton(onClick = { recibir = null; solicitud = null }) { Text("Volver") } })
}
