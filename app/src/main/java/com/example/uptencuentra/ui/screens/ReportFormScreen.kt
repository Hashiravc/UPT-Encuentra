package com.example.uptencuentra.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.example.uptencuentra.ui.components.FotoReporteView
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
import com.example.uptencuentra.domain.model.TipoReporte
import com.example.uptencuentra.domain.validation.*
import com.example.uptencuentra.ui.components.AppScaffold
import com.example.uptencuentra.ui.theme.Dimensions
import com.example.uptencuentra.ui.viewmodel.ReportFormViewModel

@Composable
fun ReportFormScreen(tipo: TipoReporte, onBack: () -> Unit, demostracion: Boolean = false, viewModel: ReportFormViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val formulario = state.formulario
    val selectorFoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(viewModel::seleccionarFoto)
    }
    val title = if (tipo == TipoReporte.PERDIDO) R.string.report_lost_title else R.string.report_found_title
    AppScaffold(title = stringResource(title), onBack = onBack) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().imePadding().verticalScroll(rememberScrollState())
            .padding(Dimensions.screenPadding), verticalArrangement = Arrangement.spacedBy(Dimensions.itemSpacing)) {
            Text(stringResource(if (demostracion) R.string.offline_demo_explanation else R.string.local_notice), style = MaterialTheme.typography.bodySmall)
            if (state.guardado) {
                Text(if (demostracion) "Reporte de demostración guardado en este dispositivo. No se enviará a internet." else "Reporte guardado. Se enviará automáticamente a Supabase cuando haya conexión.")
                Button(onClick = onBack) { Text(stringResource(R.string.done)) }
            } else {
                ReportField(CampoReporte.CATEGORIA, formulario.categoria, "Categoría del objeto", state.errores, viewModel::editar, state.guardando)
                ReportField(CampoReporte.DESCRIPCION, formulario.descripcion, "Descripción general (10 a 500 caracteres)", state.errores, viewModel::editar, state.guardando, false)
                ReportField(CampoReporte.FECHA, formulario.fecha, stringResource(R.string.event_date), state.errores, viewModel::editar, state.guardando)
                ReportField(CampoReporte.ZONA, formulario.zona, stringResource(R.string.campus_zone), state.errores, viewModel::editar, state.guardando)
                ReportField(CampoReporte.REFERENCIA, formulario.referencia, stringResource(R.string.location_reference), state.errores, viewModel::editar, state.guardando)
                ReportField(CampoReporte.MARCA, formulario.marca, stringResource(R.string.brand_optional), state.errores, viewModel::editar, state.guardando)
                ReportField(CampoReporte.MODELO, formulario.modelo, stringResource(R.string.model_optional), state.errores, viewModel::editar, state.guardando)
                ReportField(CampoReporte.COLOR, formulario.color, stringResource(R.string.color_optional), state.errores, viewModel::editar, state.guardando)
                Text("Características particulares", style = MaterialTheme.typography.titleMedium)
                Text("Registra señales que distingan el objeto. Estos detalles no se muestran a otros usuarios.")
                ReportField(CampoReporte.SENAL, formulario.senal, "Señal o daño distintivo", state.errores, viewModel::editar, state.guardando)
                ReportField(CampoReporte.ACCESORIOS, formulario.accesorios, "Funda, accesorios o adornos", state.errores, viewModel::editar, state.guardando)
                ReportField(CampoReporte.INSCRIPCION, formulario.inscripcion, "Inscripción, dibujo o marca particular", state.errores, viewModel::editar, state.guardando)
                if (tipo == TipoReporte.ENCONTRADO) {
                    OutlinedButton(enabled = !state.guardando && !state.procesandoFoto, onClick = {
                        selectorFoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) { Text(if (formulario.fotoLocal == null) "Adjuntar foto del hallazgo" else "Cambiar foto") }
                    FotoReporteView(formulario.fotoLocal)
                    if (state.procesandoFoto) CircularProgressIndicator()
                    if (state.errorFoto || state.errores.containsKey(CampoReporte.FOTO)) Text(stringResource(R.string.photo_error), color = MaterialTheme.colorScheme.error)
                    Text("La foto se guarda localmente y no se revela a quienes buscan posibles coincidencias.")
                    Text("El objeto debe entregarse a un encargado o área de la universidad. Este registro no confirma su recepción.")
                    ReportField(CampoReporte.AREA_ENTREGA, formulario.areaEntrega, stringResource(R.string.delivery_area), state.errores, viewModel::editar, state.guardando)
                }
                if (state.errorGuardado) Text(stringResource(R.string.save_error), color = MaterialTheme.colorScheme.error)
                Button(onClick = { viewModel.guardar(tipo) }, enabled = !state.guardando && !state.procesandoFoto, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(if (state.guardando) R.string.saving else R.string.save_report))
                }
            }
        }
    }
}

@Composable
private fun ReportField(campo: CampoReporte, valor: String, etiqueta: String,
                        errores: Map<CampoReporte, ErrorCampo>, onChange: (CampoReporte, String) -> Unit,
                        bloqueado: Boolean, singleLine: Boolean = true) {
    val error = errores[campo]
    OutlinedTextField(value = valor, onValueChange = { onChange(campo, it) },
        label = { Text(etiqueta) }, modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine, minLines = if (singleLine) 1 else 3,
        enabled = !bloqueado, isError = error != null,
        supportingText = if (error == null) null else { { Text(stringResource(when (error) {
            ErrorCampo.OBLIGATORIO -> R.string.required_field
            ErrorCampo.MUY_LARGO -> R.string.field_too_long
            ErrorCampo.DESCRIPCION_CORTA -> R.string.short_description
            ErrorCampo.FECHA_INVALIDA -> R.string.invalid_date
            ErrorCampo.FECHA_FUTURA -> R.string.future_date
        })) } })
}
