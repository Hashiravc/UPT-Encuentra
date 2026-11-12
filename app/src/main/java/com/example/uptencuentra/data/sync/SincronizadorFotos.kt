package com.example.uptencuentra.data.sync

import com.example.uptencuentra.data.api.SupabaseApi
import com.example.uptencuentra.data.model.*
import com.example.uptencuentra.data.photos.AlmacenFotos
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import javax.inject.Inject

class SincronizadorFotos @Inject constructor(private val api: SupabaseApi, private val fotos: AlmacenFotos) {
    // La ruta por hash evita duplicar archivos cuando se reintenta el mismo pendiente.
    suspend fun preparar(datos: ReporteDto, token: String, cuenta: String): ReporteDto = withContext(Dispatchers.IO) {
        val local = datos.fotoLocal ?: return@withContext datos
        val ruta = "$cuenta/${datos.id}/${fotos.huella(local)}.jpg"
        try { api.subirFoto("Bearer $token", ruta, fotos.archivo(local).asRequestBody("image/jpeg".toMediaType())).close()
        } catch (error: HttpException) {
            val cuerpo = runCatching { Gson().fromJson(error.response()?.errorBody()?.string(), Map::class.java) }.getOrNull()
            val repetida = error.code() == 409 || (cuerpo?.get("statusCode")?.toString() == "409" && cuerpo["error"] == "Duplicate")
            if (!repetida) throw error
        }
        datos.copy(fotoRuta = ruta)
    }

    suspend fun localizar(remoto: ReporteRemotoDto, token: String, rutaAnterior: String?): String? = withContext(Dispatchers.IO) {
        val ruta = remoto.fotoRuta ?: return@withContext null
        require(ruta.matches(Regex("${remoto.autorId}/${remoto.id}/[a-f0-9]{64}\\.jpg")))
        if (fotos.existe(rutaAnterior) && fotos.huella(rutaAnterior!!) == ruta.substringAfterLast('/').removeSuffix(".jpg")) return@withContext rutaAnterior
        val destino = fotos.destinoDescarga()
        try {
            api.descargarFoto("Bearer $token", ruta).use { respuesta ->
                respuesta.byteStream().use { entrada -> destino.outputStream().use { salida ->
                    val buffer = ByteArray(8192); var total = 0L
                    while (true) {
                        val n = entrada.read(buffer); if (n < 0) break
                        total += n; require(total <= 5_242_880); salida.write(buffer, 0, n)
                    }
                    require(total > 0)
                } }
            }
            require(fotos.huella(destino.absolutePath) == ruta.substringAfterLast('/').removeSuffix(".jpg"))
            destino.absolutePath
        } catch (error: Exception) { destino.delete(); throw error }
    }
}
