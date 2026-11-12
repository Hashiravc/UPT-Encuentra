package com.example.uptencuentra.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "pendientes", foreignKeys = [ForeignKey(
    entity = ReporteEntity::class, parentColumns = ["id"], childColumns = ["reporteId"],
    onDelete = ForeignKey.CASCADE
)])
data class PendienteEntity(
    @PrimaryKey val reporteId: String,
    val version: Int,
    val payloadJson: String,
    val creadoEn: Long,
    @androidx.room.ColumnInfo(defaultValue = "''") val cuentaId: String = ""
)
