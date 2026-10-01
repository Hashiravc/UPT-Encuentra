package com.example.uptencuentra.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.uptencuentra.ui.components.AppScaffold

@Composable
fun ReportsScreen(onOpen: (Int) -> Unit, onBack: () -> Unit) {
    val pruebas = listOf(1, 2, 3) // SOLO PARA PROBAR. Se reemplaza en la Fase 4.

    AppScaffold(title = "Mis reportes", onBack = onBack) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(pruebas) { id ->
                Card(modifier = Modifier.fillMaxWidth().clickable { onOpen(id) }) {
                    Text("Reporte de prueba #$id", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}