package com.example.uptencuentra.data.auth

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlmacenSesion @Inject constructor(@ApplicationContext context: Context) {
    private val preferencias = context.getSharedPreferences("sesion_segura", Context.MODE_PRIVATE)
    private val alias = "upt_encuentra_sesion"

    @Synchronized
    fun guardar(nombre: String, texto: String) {
        val cifrador = Cipher.getInstance("AES/GCM/NoPadding")
        cifrador.init(Cipher.ENCRYPT_MODE, clave())
        val contenido = cifrador.iv + cifrador.doFinal(texto.toByteArray(Charsets.UTF_8))
        check(preferencias.edit().putString(nombre, Base64.encodeToString(contenido, Base64.NO_WRAP)).commit())
    }

    @Synchronized
    fun leer(nombre: String): String? {
        val guardado = preferencias.getString(nombre, null) ?: return null
        return try {
            val bytes = Base64.decode(guardado, Base64.NO_WRAP)
            val cifrador = Cipher.getInstance("AES/GCM/NoPadding")
            cifrador.init(Cipher.DECRYPT_MODE, clave(), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
            String(cifrador.doFinal(bytes.copyOfRange(12, bytes.size)), Charsets.UTF_8)
        } catch (_: Exception) {
            borrar(nombre)
            null
        }
    }

    @Synchronized
    fun borrar(nombre: String) { check(preferencias.edit().remove(nombre).commit()) }

    private fun clave(): SecretKey {
        val almacen = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (almacen.getKey(alias, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build())
        }.generateKey()
    }
}
