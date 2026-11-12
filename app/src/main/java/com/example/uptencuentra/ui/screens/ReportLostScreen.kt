package com.example.uptencuentra.ui.screens

import androidx.compose.runtime.Composable
import com.example.uptencuentra.domain.model.TipoReporte

@Composable
fun ReportLostScreen(onBack: () -> Unit, demostracion: Boolean = false) {
    ReportFormScreen(tipo = TipoReporte.PERDIDO, onBack = onBack, demostracion = demostracion)
}
