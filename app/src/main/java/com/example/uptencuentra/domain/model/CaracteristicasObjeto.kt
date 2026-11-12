package com.example.uptencuentra.domain.model

data class CaracteristicasObjeto(val marca: String, val modelo: String, val color: String) {
    init {
        require(listOf(marca, modelo, color).all { it.length <= 80 })
    }
}
