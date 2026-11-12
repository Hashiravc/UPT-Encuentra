package com.example.uptencuentra.data.local.dao

import androidx.room.*
import com.example.uptencuentra.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CoincidenciaDao {
    @Query("SELECT * FROM coincidencias WHERE perdidaId=:perdida AND cuentaId=:cuenta ORDER BY puntaje DESC, id")
    fun observar(perdida: String, cuenta: String): Flow<List<CoincidenciaEntity>>
    @Query("SELECT * FROM coincidencias WHERE id=:id AND cuentaId=:cuenta")
    suspend fun obtener(id: String, cuenta: String): CoincidenciaEntity?
    @Query("DELETE FROM coincidencias WHERE perdidaId=:perdida AND cuentaId=:cuenta")
    suspend fun limpiar(perdida: String, cuenta: String)
    @Upsert suspend fun guardar(lista: List<CoincidenciaEntity>)
}

@Dao
interface SolicitudDao {
    @Query("SELECT * FROM solicitudes WHERE cuentaId=:cuenta ORDER BY id DESC")
    fun observar(cuenta: String): Flow<List<SolicitudEntity>>
    @Query("SELECT * FROM solicitudes WHERE cuentaId=:cuenta AND estado='PENDIENTE_ENVIO'")
    suspend fun pendientes(cuenta: String): List<SolicitudEntity>
    @Query("SELECT * FROM solicitudes WHERE cuentaId=:cuenta AND coincidenciaId=:coincidencia AND estado NOT IN ('RECHAZADA','ERROR_ENVIO') LIMIT 1")
    suspend fun activa(cuenta: String, coincidencia: String): SolicitudEntity?
    @Upsert suspend fun guardar(solicitud: SolicitudEntity)
}
