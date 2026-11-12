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
import com.example.uptencuentra.ui.viewmodel.MatchesViewModel

@Composable
fun MatchesScreen(onVerify: (String) -> Unit, onBack: () -> Unit, viewModel: MatchesViewModel = hiltViewModel()) {
    val lista by viewModel.coincidencias.collectAsStateWithLifecycle()
    val cargando by viewModel.cargando.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    AppScaffold("Posibles coincidencias", onBack) { padding ->
        LazyColumn(Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(Dimensions.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimensions.itemSpacing)) {
            item { Text("Son sugerencias, no una confirmación de propiedad. La foto y las características identificativas del hallazgo permanecen privadas.") }
            item { OutlinedButton(onClick = viewModel::buscar, enabled = !cargando) { Text("Actualizar coincidencias") } }
            if (cargando) item { CircularProgressIndicator() }
            if (error) item { Text("No se pudo actualizar. Se conservan los resultados guardados. Revisa la conexión y que tu reporte esté sincronizado.") }
            if (!cargando && lista.isEmpty()) item { Text("Aún no hay coincidencias con suficientes características particulares.") }
            items(lista, key = { it.id }) { c ->
                Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(Dimensions.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.compactSpacing)) {
                    Text(c.categoria, style = MaterialTheme.typography.titleMedium)
                    Text("${c.zona} · ${c.fecha}")
                    Text("Puntaje: ${c.puntaje}/100")
                    c.motivos.forEach { Text("• $it") }
                    Button(onClick = { onVerify(c.id) }) { Text("Solicitar recuperación") }
                } }
            }
        }
    }
}
