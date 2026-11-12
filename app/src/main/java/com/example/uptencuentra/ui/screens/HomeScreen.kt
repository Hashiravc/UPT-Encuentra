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
import com.example.uptencuentra.data.sync.EstadoSincronizacion
import com.example.uptencuentra.ui.components.AppScaffold
import com.example.uptencuentra.ui.theme.Dimensions
import com.example.uptencuentra.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(onReportLost: () -> Unit, onReportFound: () -> Unit, onMyReports: () -> Unit,
               onLogout: () -> Unit, correo: String, onRequests: () -> Unit, onEncargado: () -> Unit, esEncargado: Boolean, demostracion: Boolean = false, viewModel: HomeViewModel = hiltViewModel()) {
    val pendientes by viewModel.pendientes.collectAsStateWithLifecycle()
    val estado by viewModel.estadoSync.collectAsStateWithLifecycle()
    val sinCuenta by viewModel.sinCuenta.collectAsStateWithLifecycle()
    val importando by viewModel.importando.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    var confirmarImportacion by remember { mutableStateOf(false) }
    AppScaffold(title = "Inicio") { padding ->
        Column(Modifier.padding(padding).fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(Dimensions.screenPadding), verticalArrangement = Arrangement.spacedBy(Dimensions.itemSpacing)) {
            if (demostracion) {
                Text("Demostración sin conexión", style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.offline_demo_explanation))
            } else {
                Text(correo)
                Text("Reportes pendientes de enviar: $pendientes")
                Text(when (estado) {
                EstadoSincronizacion.EN_ESPERA -> "Los cambios se enviarán cuando haya conexión."
                EstadoSincronizacion.SINCRONIZANDO -> "Sincronizando con Supabase…"
                EstadoSincronizacion.ACTUALIZADO -> "Última sincronización completada."
                EstadoSincronizacion.SIN_CONEXION -> "Sin conexión. Tus cambios siguen guardados en este dispositivo."
                EstadoSincronizacion.SESION_VENCIDA -> "La sesión necesita renovarse. Cierra sesión e ingresa otra vez; tus reportes se conservarán."
                EstadoSincronizacion.ERROR -> "No se pudo sincronizar. Los pendientes se conservan. Puedes volver a intentar."
                })
            }
            Button(onClick = onReportLost, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.report_lost_title)) }
            Button(onClick = onReportFound, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.report_found_title)) }
            OutlinedButton(onClick = onMyReports, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.local_reports)) }
            OutlinedButton(onClick = onRequests, modifier = Modifier.fillMaxWidth()) { Text("Mis solicitudes") }
            if (esEncargado) OutlinedButton(onClick = onEncargado, modifier = Modifier.fillMaxWidth()) { Text("Recepción y entrega") }
            if (!demostracion) OutlinedButton(onClick = viewModel::sincronizar, enabled = estado != EstadoSincronizacion.SINCRONIZANDO,
                modifier = Modifier.fillMaxWidth()) { Text("Sincronizar ahora") }
            if (!demostracion && sinCuenta > 0) OutlinedButton(onClick = { confirmarImportacion = true }, enabled = !importando) {
                Text("Asociar reportes de demostración ($sinCuenta)")
            }
            if (error) Text(stringResource(R.string.save_error), color = MaterialTheme.colorScheme.error)
            TextButton(onClick = onLogout) { Text(if (demostracion) "Salir de demostración" else stringResource(R.string.logout)) }
        }
    }
    if (confirmarImportacion) AlertDialog(onDismissRequest = { confirmarImportacion = false },
        title = { Text("Asociar reportes a tu cuenta") },
        text = { Text("Los reportes anteriores de este dispositivo se asociarán a $correo y se enviarán a Supabase. Confirma solamente si te pertenecen.") },
        confirmButton = { TextButton(onClick = { confirmarImportacion = false; viewModel.importar() }) { Text(stringResource(R.string.confirm)) } },
        dismissButton = { TextButton(onClick = { confirmarImportacion = false }) { Text(stringResource(R.string.go_back)) } })
}
