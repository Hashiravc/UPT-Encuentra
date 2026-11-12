package com.example.uptencuentra.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migraciones {
    val VERSION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE reportes ADD COLUMN senal TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE reportes ADD COLUMN accesorios TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE reportes ADD COLUMN inscripcion TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE reportes ADD COLUMN fotoLocal TEXT")
            db.execSQL("ALTER TABLE reportes ADD COLUMN fotoRemota TEXT")
            db.execSQL("CREATE TABLE IF NOT EXISTS coincidencias (id TEXT NOT NULL PRIMARY KEY, perdidaId TEXT NOT NULL, categoria TEXT NOT NULL, zona TEXT NOT NULL, fecha TEXT NOT NULL, puntaje INTEGER NOT NULL, motivosJson TEXT NOT NULL, cuentaId TEXT NOT NULL)")
            db.execSQL("CREATE TABLE IF NOT EXISTS solicitudes (id TEXT NOT NULL PRIMARY KEY, coincidenciaId TEXT NOT NULL, perdidaId TEXT NOT NULL, hallazgoId TEXT NOT NULL, explicacion TEXT NOT NULL, estado TEXT NOT NULL, cuentaId TEXT NOT NULL)")
        }
    }

    val VERSION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE reportes ADD COLUMN cuentaId TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE pendientes ADD COLUMN cuentaId TEXT NOT NULL DEFAULT ''")
        }
    }
}
