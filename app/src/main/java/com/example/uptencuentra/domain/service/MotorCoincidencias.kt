package com.example.uptencuentra.domain.service

import com.example.uptencuentra.domain.model.*
import java.text.Normalizer
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToInt

object ParametrosCoincidencia {
    const val SENAL = 35
    const val ACCESORIOS = 25
    const val INSCRIPCION = 30
    const val UMBRAL = 35
    const val SIMILITUD_MINIMA = 0.6
}

class MotorCoincidencias {
    private val ignoradas = setOf("de", "la", "el", "en", "un", "una", "con", "y", "del", "los", "las", "al", "mi", "mis")

    // Compara datos particulares; las características generales por sí solas nunca generan un candidato.
    fun comparar(perdido: Reporte, hallado: Reporte): Coincidencia? {
        if (perdido.tipo != TipoReporte.PERDIDO || hallado.tipo != TipoReporte.ENCONTRADO) return null
        if (perdido.estado != EstadoReporte.ACTIVO || hallado.estado !in listOf(EstadoReporte.ACTIVO, EstadoReporte.EN_CUSTODIA)) return null
        if (normalizar(perdido.categoria) != normalizar(hallado.categoria)) return null
        val dias = ChronoUnit.DAYS.between(perdido.fechaEvento, hallado.fechaEvento)
        if (dias !in -1..30) return null
        val p = perdido.caracteristicas; val h = hallado.caracteristicas
        if (difieren(p.marca, h.marca) || difieren(p.modelo, h.modelo)) return null
        val pares = listOf(
            Triple(perdido.particulares.senal, hallado.particulares.senal, ParametrosCoincidencia.SENAL),
            Triple(perdido.particulares.accesorios, hallado.particulares.accesorios, ParametrosCoincidencia.ACCESORIOS),
            Triple(perdido.particulares.inscripcion, hallado.particulares.inscripcion, ParametrosCoincidencia.INSCRIPCION))
        val similares = pares.map { similitud(it.first, it.second) to it.third }
            .filter { it.first >= ParametrosCoincidencia.SIMILITUD_MINIMA }
        if (similares.isEmpty()) return null
        var puntos = similares.sumOf { it.first * it.second }
        if (iguales(p.marca, h.marca)) puntos += 5
        if (iguales(p.modelo, h.modelo)) puntos += 10
        if (iguales(p.color, h.color)) puntos += 5
        if (normalizar(perdido.ubicacion.zona) == normalizar(hallado.ubicacion.zona)) puntos += 5
        puntos += when { kotlin.math.abs(dias) <= 3 -> 5; kotlin.math.abs(dias) <= 7 -> 2; else -> 0 }
        if (puntos.roundToInt() < ParametrosCoincidencia.UMBRAL) return null
        return Coincidencia("${perdido.id}:${hallado.id}", perdido.id, hallado.categoria, hallado.ubicacion.zona,
            hallado.fechaEvento.toString(), puntos.roundToInt().coerceAtMost(100),
            listOf("Coincidencia en características particulares", "Categoría compatible", "Fecha dentro del intervalo de búsqueda"))
    }

    private fun iguales(a: String, b: String) = normalizar(a).isNotBlank() && normalizar(a) == normalizar(b)
    private fun difieren(a: String, b: String) = normalizar(a).isNotBlank() && normalizar(b).isNotBlank() && !iguales(a, b)
    private fun normalizar(t: String) = Normalizer.normalize(t.lowercase(Locale.ROOT), Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "").replace(Regex("[^a-z0-9]+"), " ").trim()
    private fun similitud(a: String, b: String): Double {
        val ta = normalizar(a).split(' ').filter { it.isNotBlank() && it !in ignoradas }.toSet()
        val tb = normalizar(b).split(' ').filter { it.isNotBlank() && it !in ignoradas }.toSet()
        val union = ta union tb
        return if (union.isEmpty()) 0.0 else (ta intersect tb).size.toDouble() / union.size
    }
}
