package com.example.uptencuentra.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.uptencuentra.R
import com.example.uptencuentra.domain.model.EstadoReporte
import com.example.uptencuentra.domain.model.TipoReporte
import com.example.uptencuentra.ui.components.AppScaffold
import com.example.uptencuentra.ui.components.estadoReporteRecurso
import com.example.uptencuentra.ui.theme.Dimensions
import com.example.uptencuentra.ui.viewmodel.ReportsViewModel

@Composable
fun ReportsScreen(onOpen: (String) -> Unit, onBack: () -> Unit, demostracion: Boolean = false, viewModel: ReportsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AppScaffold(title = stringResource(R.string.local_reports), onBack = onBack) { padding ->
        LazyColumn(Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(Dimensions.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimensions.itemSpacing)) {
            item { Text(stringResource(if (demostracion) R.string.offline_demo_explanation else R.string.local_notice)) }
            if (state.cargando) item { CircularProgressIndicator() }
            else if (state.error) item { Text(stringResource(R.string.load_error)) }
            else if (state.reportes.isEmpty()) item { Text("Todavía no hay reportes en este dispositivo.") }
            items(state.reportes, key = { it.id }) { reporte ->
                Card(Modifier.fillMaxWidth().clickable { onOpen(reporte.id) }) {
                    Column(Modifier.padding(Dimensions.screenPadding), verticalArrangement = Arrangement.spacedBy(Dimensions.compactSpacing)) {
                        Text(reporte.categoria, style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(if (reporte.tipo == TipoReporte.PERDIDO) R.string.lost else R.string.found))
                        Text(reporte.descripcion, maxLines = 2)
                        Text(reporte.ubicacion.zona)
                        Text(stringResource(estadoReporteRecurso(reporte.estado)))
                    }
                }
            }
        }
    }
}
