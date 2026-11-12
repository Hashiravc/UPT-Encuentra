package com.example.uptencuentra.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.uptencuentra.ui.viewmodel.ReportDetailViewModel

@Composable
fun ObjectDetailScreen(onBack: () -> Unit, onMatches: (String) -> Unit, demostracion: Boolean = false, viewModel: ReportDetailViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val cancellation by viewModel.cancellation.collectAsStateWithLifecycle()
    val tituloCancelacion = "Cancelar reporte"
    var confirmar by remember { mutableStateOf(false) }
    AppScaffold(title = "Detalle del reporte", onBack = onBack) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())
            .padding(Dimensions.screenPadding), verticalArrangement = Arrangement.spacedBy(Dimensions.itemSpacing)) {
            val reporte = state.reporte
            when {
                state.cargando -> CircularProgressIndicator()
                state.error -> Text(stringResource(R.string.load_error))
                reporte == null -> Text(stringResource(R.string.report_not_found))
                else -> {
                    Text(reporte.categoria, style = MaterialTheme.typography.headlineSmall)
                    Text(stringResource(if (reporte.tipo == TipoReporte.PERDIDO) R.string.lost else R.string.found))
                    Text(reporte.descripcion)
                    com.example.uptencuentra.ui.components.FotoReporteView(reporte.fotoLocal)
                    Text("Detalles particulares: ${listOf(reporte.particulares.senal, reporte.particulares.accesorios, reporte.particulares.inscripcion).filter { it.isNotBlank() }.joinToString(" · ")}")
                    if (reporte.tipo == TipoReporte.PERDIDO && reporte.estado == EstadoReporte.ACTIVO) {
                        Button(onClick = { onMatches(reporte.id) }) { Text("Buscar posibles coincidencias") }
                    }
                    DetailField(R.string.event_date, reporte.fechaEvento.toString())
                    DetailField(R.string.campus_zone, reporte.ubicacion.zona)
                    DetailField(R.string.location_reference, reporte.ubicacion.referencia)
                    DetailField(R.string.brand_optional, reporte.caracteristicas.marca)
                    DetailField(R.string.model_optional, reporte.caracteristicas.modelo)
                    DetailField(R.string.color_optional, reporte.caracteristicas.color)
                    reporte.areaEntrega?.let { DetailField(R.string.delivery_area, it) }
                    Text(stringResource(estadoReporteRecurso(reporte.estado)))
                    Text(stringResource(if (demostracion) R.string.offline_demo_explanation else R.string.local_notice))
                    Text("Las características no se editan después de guardar. La cancelación conserva el registro.")
                    if (cancellation.error) Text(stringResource(R.string.save_error), color = MaterialTheme.colorScheme.error)
                    if (reporte.estado == EstadoReporte.ACTIVO) {
                        OutlinedButton(onClick = { confirmar = true }, enabled = !cancellation.procesando) {
                            Text(tituloCancelacion)
                        }
                    }
                }
            }
        }
    }
    if (confirmar) AlertDialog(onDismissRequest = { confirmar = false },
        title = { Text(tituloCancelacion) },
        text = { Text("El reporte dejará de estar activo. Sus características permanecerán registradas.") },
        confirmButton = { TextButton(onClick = { confirmar = false; viewModel.cancelar() }) { Text(stringResource(R.string.confirm)) } },
        dismissButton = { TextButton(onClick = { confirmar = false }) { Text(stringResource(R.string.go_back)) } })
}

@Composable
private fun DetailField(etiqueta: Int, valor: String) {
    if (valor.isNotBlank()) Column {
        Text(stringResource(etiqueta), style = MaterialTheme.typography.labelLarge)
        Text(valor)
    }
}
