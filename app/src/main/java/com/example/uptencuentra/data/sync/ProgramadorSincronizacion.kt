package com.example.uptencuentra.data.sync

import android.content.Context
import androidx.work.*
import com.example.uptencuentra.domain.service.ProgramarSincronizacion
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgramadorSincronizacion @Inject constructor(@ApplicationContext private val context: Context) : ProgramarSincronizacion {
    // Programa el envío con conexión y una revisión periódica de los datos de la cuenta.
    override fun programar(cuentaId: String) {
        if (cuentaId.isBlank()) return
        val work = WorkManager.getInstance(context)
        val restricciones = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val datos = workDataOf("cuentaId" to cuentaId)
        val inmediato = OneTimeWorkRequestBuilder<SyncWorker>().setConstraints(restricciones)
            .setInputData(datos).addTag("sincronizacion").build()
        work.enqueueUniqueWork("envio-$cuentaId", ExistingWorkPolicy.APPEND_OR_REPLACE, inmediato)
        val periodico = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(restricciones).setInputData(datos).addTag("sincronizacion").build()
        work.enqueueUniquePeriodicWork("revision-$cuentaId", ExistingPeriodicWorkPolicy.KEEP, periodico)
    }

    fun detener() { WorkManager.getInstance(context).cancelAllWorkByTag("sincronizacion") }
}
