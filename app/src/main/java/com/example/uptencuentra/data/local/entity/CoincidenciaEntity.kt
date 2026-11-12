package com.example.uptencuentra.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coincidencias")
data class CoincidenciaEntity(@PrimaryKey val id: String, val perdidaId: String, val categoria: String,
    val zona: String, val fecha: String, val puntaje: Int, val motivosJson: String, val cuentaId: String)

@Entity(tableName = "solicitudes")
data class SolicitudEntity(@PrimaryKey val id: String, val coincidenciaId: String, val perdidaId: String,
    val hallazgoId: String, val explicacion: String, val estado: String, val cuentaId: String)
