package com.example.uptencuentra.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.uptencuentra.ui.components.AppScaffold

@Composable
fun ObjectDetailScreen(objectId: Int, onMatches: () -> Unit, onBack: () -> Unit) {
    AppScaffold(title = "Detalle", onBack = onBack) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Recibí el objectId = $objectId", style = MaterialTheme.typography.titleMedium)
            Button(onClick = onMatches) { Text("Ver posibles coincidencias") }
        }
    }
}