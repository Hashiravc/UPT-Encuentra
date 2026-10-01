package com.example.uptencuentra.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.uptencuentra.ui.components.AppScaffold

@Composable
fun RecoveryScreen(matchId: Int, onFinish: () -> Unit, onBack: () -> Unit) {
    AppScaffold(title = "Recuperación", onBack = onBack) { padding ->
        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Recuperación de la coincidencia #$matchId")
            Button(onClick = onFinish) { Text("Confirmar recuperación") }
        }
    }
}