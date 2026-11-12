package com.example.uptencuentra.ui.components

import com.example.uptencuentra.R
import com.example.uptencuentra.domain.model.EstadoReporte

fun estadoReporteRecurso(estado: EstadoReporte): Int = when (estado) {
    EstadoReporte.ACTIVO -> R.string.active
    EstadoReporte.CANCELADO -> R.string.cancelled
    EstadoReporte.EN_CUSTODIA -> R.string.in_custody
    EstadoReporte.RECUPERADO -> R.string.recovered
}
