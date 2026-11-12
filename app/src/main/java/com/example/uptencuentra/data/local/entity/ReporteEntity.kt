package com.example.uptencuentra.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reportes")
data class ReporteEntity(
    @PrimaryKey val id: String,
    val tipo: String,
    val categoria: String,
    val descripcion: String,
    val fechaEvento: String,
    val zona: String,
    val referencia: String,
    val marca: String,
    val modelo: String,
    val color: String,
    val areaEntrega: String?,
    val estado: String,
    val version: Int,
    val creadoEn: Long,
    @androidx.room.ColumnInfo(defaultValue = "''") val cuentaId: String = "",
    @androidx.room.ColumnInfo(defaultValue = "''") val senal: String = "",
    @androidx.room.ColumnInfo(defaultValue = "''") val accesorios: String = "",
    @androidx.room.ColumnInfo(defaultValue = "''") val inscripcion: String = "",
    val fotoLocal: String? = null,
    val fotoRemota: String? = null
)
