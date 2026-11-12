package com.example.uptencuentra.data.photos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlmacenFotos @Inject constructor(@ApplicationContext private val context: Context) {
    private val carpeta get() = File(context.filesDir, "fotos").apply { mkdirs() }

    // Copia una imagen como JPEG reducido y sin los metadatos de la galería original.
    suspend fun importar(uri: Uri): String = withContext(Dispatchers.IO) {
        require(context.contentResolver.getType(uri)?.startsWith("image/") == true)
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, limites) }
        require(limites.outWidth in 1..100000 && limites.outHeight in 1..100000)
        var muestra = 1
        while (maxOf(limites.outWidth, limites.outHeight) / muestra > 1600) muestra *= 2
        val opciones = BitmapFactory.Options().apply { inSampleSize = muestra }
        val bitmap = context.contentResolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, opciones) }
        requireNotNull(bitmap)
        val destino = File(carpeta, "${UUID.randomUUID()}.jpg")
        try {
            destino.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it)) }
            require(destino.length() in 1..5_242_880)
            destino.absolutePath
        } catch (error: Exception) { destino.delete(); throw error
        } finally { bitmap.recycle() }
    }

    fun archivo(ruta: String): File {
        val file = File(ruta).canonicalFile
        require(file.parentFile == carpeta.canonicalFile && file.isFile)
        return file
    }

    fun destinoDescarga(): File = File(carpeta, "${UUID.randomUUID()}.jpg")
    fun existe(ruta: String?): Boolean = ruta != null && runCatching { archivo(ruta) }.isSuccess
    fun huella(ruta: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        archivo(ruta).inputStream().use { input ->
            val buffer = ByteArray(8192)
            while (true) { val n = input.read(buffer); if (n < 0) break; digest.update(buffer, 0, n) }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
