package com.example.uptencuentra.ui.screens

import androidx.compose.runtime.Composable
import com.example.uptencuentra.domain.model.TipoReporte

@Composable
fun ReportFoundScreen(onBack: () -> Unit, demostracion: Boolean = false) {
    ReportFormScreen(tipo = TipoReporte.ENCONTRADO, onBack = onBack, demostracion = demostracion)
}
