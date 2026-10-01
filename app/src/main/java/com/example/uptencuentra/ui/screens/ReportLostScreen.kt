package com.example.uptencuentra.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.uptencuentra.ui.components.AppScaffold

@Composable
fun ReportLostScreen(onBack: () -> Unit) {
    AppScaffold(title = "Reportar perdido", onBack = onBack) { padding ->
        Text("Formulario (Fase 4)", modifier = Modifier.padding(padding).padding(16.dp))
    }
}