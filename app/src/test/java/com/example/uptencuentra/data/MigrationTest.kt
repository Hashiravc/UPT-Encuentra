package com.example.uptencuentra.data

import android.app.Application
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import com.example.uptencuentra.data.local.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class MigrationTest {
    @Test fun conservaLaDemostracionSinAsignarla() {
        val context = RuntimeEnvironment.getApplication()
        val nombre = "migration.db"
        context.deleteDatabase(nombre)
        val archivo = context.getDatabasePath(nombre)
        archivo.parentFile!!.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(archivo, null).use { anterior ->
            anterior.execSQL("CREATE TABLE IF NOT EXISTS `reportes` (`id` TEXT NOT NULL, `tipo` TEXT NOT NULL, `categoria` TEXT NOT NULL, `descripcion` TEXT NOT NULL, `fechaEvento` TEXT NOT NULL, `zona` TEXT NOT NULL, `referencia` TEXT NOT NULL, `marca` TEXT NOT NULL, `modelo` TEXT NOT NULL, `color` TEXT NOT NULL, `areaEntrega` TEXT, `estado` TEXT NOT NULL, `version` INTEGER NOT NULL, `creadoEn` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            anterior.execSQL("CREATE TABLE IF NOT EXISTS `pendientes` (`reporteId` TEXT NOT NULL, `version` INTEGER NOT NULL, `payloadJson` TEXT NOT NULL, `creadoEn` INTEGER NOT NULL, PRIMARY KEY(`reporteId`), FOREIGN KEY(`reporteId`) REFERENCES `reportes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
            anterior.execSQL("INSERT INTO reportes VALUES ('demo', 'PERDIDO', 'Celular', 'Celular negro encontrado', '2026-01-01', 'Campus', 'Patio', '', '', '', NULL, 'ACTIVO', 1, 1)")
            anterior.execSQL("INSERT INTO pendientes VALUES ('demo', 1, '{}', 1)")
            anterior.version = 1
        }
        val actual = Room.databaseBuilder(context, AppDatabase::class.java, nombre)
            .addMigrations(Migraciones.VERSION_1_2, Migraciones.VERSION_2_3).build()
        try {
            actual.openHelper.writableDatabase.query("SELECT cuentaId FROM reportes WHERE id = 'demo'").use {
                assertTrue(it.moveToFirst())
                assertEquals("", it.getString(0))
            }
            actual.openHelper.writableDatabase.query("SELECT cuentaId FROM pendientes WHERE reporteId = 'demo'").use {
                assertTrue(it.moveToFirst())
                assertEquals("", it.getString(0))
            }
        } finally { actual.close(); context.deleteDatabase(nombre) }
    }
}
