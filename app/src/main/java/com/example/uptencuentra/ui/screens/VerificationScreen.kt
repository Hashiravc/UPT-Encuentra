package com.example.uptencuentra.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.uptencuentra.ui.components.AppScaffold

@Composable
fun VerificationScreen(matchId: Int, onConfirm: () -> Unit, onBack: () -> Unit) {
    AppScaffold(title = "Verificación", onBack = onBack) { padding ->
        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Verificando la coincidencia #$matchId")
            Button(onClick = onConfirm) { Text("Continuar a recuperación") }
        }
    }
}