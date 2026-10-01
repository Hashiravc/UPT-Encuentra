package com.example.uptencuentra.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.uptencuentra.ui.components.AppScaffold

@Composable
fun HomeScreen(
    onReportLost: () -> Unit,
    onReportFound: () -> Unit,
    onMyReports: () -> Unit
) {
    AppScaffold(title = "Inicio") { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = onReportLost, modifier = Modifier.fillMaxWidth()) {
                Text("Reportar objeto perdido")
            }
            Button(onClick = onReportFound, modifier = Modifier.fillMaxWidth()) {
                Text("Reportar objeto encontrado")
            }
            OutlinedButton(onClick = onMyReports, modifier = Modifier.fillMaxWidth()) {
                Text("Mis reportes")
            }
        }
    }
}