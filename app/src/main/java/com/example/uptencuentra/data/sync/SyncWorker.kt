package com.example.uptencuentra.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.uptencuentra.domain.repository.SesionExpiradaException
import com.example.uptencuentra.domain.repository.SesionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted parametros: WorkerParameters,
    private val sincronizador: SincronizadorReportes,
    private val sesion: SesionRepository
) : CoroutineWorker(context, parametros) {
    override suspend fun doWork(): Result {
        val cuenta = inputData.getString("cuentaId") ?: return Result.failure()
        if (sesion.usuario.value?.id != cuenta) return Result.success()
        return try {
            sincronizador.sincronizar(cuenta)
            Result.success()
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (_: SesionExpiradaException) {
            sincronizador.informar(EstadoSincronizacion.SESION_VENCIDA)
            Result.failure()
        } catch (_: IOException) {
            sincronizador.informar(EstadoSincronizacion.SIN_CONEXION)
            Result.retry()
        } catch (error: HttpException) {
            sincronizador.informar(if (error.code() == 401) EstadoSincronizacion.SESION_VENCIDA else EstadoSincronizacion.ERROR)
            if (error.code() == 429 || error.code() >= 500) Result.retry() else Result.failure()
        } catch (_: Exception) {
            sincronizador.informar(EstadoSincronizacion.ERROR)
            Result.failure()
        }
    }
}
