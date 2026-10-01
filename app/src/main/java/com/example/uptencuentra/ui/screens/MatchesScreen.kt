package com.example.uptencuentra.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.uptencuentra.ui.components.AppScaffold

@Composable
fun MatchesScreen(objectId: Int, onVerify: (Int) -> Unit, onBack: () -> Unit) {
    AppScaffold(title = "Coincidencias", onBack = onBack) { padding ->
        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Coincidencias para el objeto #$objectId")
            Button(onClick = { onVerify(1) }) { Text("Verificar coincidencia #1 (prueba)") }
        }
    }
}