package com.example.uptencuentra.domain.model

data class CaracteristicasParticulares(val senal: String = "", val accesorios: String = "", val inscripcion: String = "") {
    init { require(listOf(senal, accesorios, inscripcion).all { it.length <= 160 }) }
    val estanVacias: Boolean get() = listOf(senal, accesorios, inscripcion).all { it.isBlank() }
}
