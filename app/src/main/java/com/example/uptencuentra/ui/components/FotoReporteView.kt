package com.example.uptencuentra.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.io.File

@Composable
fun FotoReporteView(ruta: String?) {
    if (!ruta.isNullOrBlank()) AsyncImage(model = File(ruta), contentDescription = "Foto del objeto",
        modifier = Modifier.fillMaxWidth().height(220.dp), contentScale = ContentScale.Fit)
}
