package com.example.uptencuentra.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.uptencuentra.data.local.dao.PendienteDao
import com.example.uptencuentra.data.local.dao.ReporteDao
import com.example.uptencuentra.data.local.entity.PendienteEntity
import com.example.uptencuentra.data.local.entity.ReporteEntity

@Database(entities = [ReporteEntity::class, PendienteEntity::class, com.example.uptencuentra.data.local.entity.CoincidenciaEntity::class, com.example.uptencuentra.data.local.entity.SolicitudEntity::class], version = 3, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reporteDao(): ReporteDao
    abstract fun pendienteDao(): PendienteDao
    abstract fun coincidenciaDao(): com.example.uptencuentra.data.local.dao.CoincidenciaDao
    abstract fun solicitudDao(): com.example.uptencuentra.data.local.dao.SolicitudDao
}
