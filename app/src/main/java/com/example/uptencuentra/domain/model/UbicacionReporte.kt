package com.example.uptencuentra.domain.model

data class UbicacionReporte(val zona: String, val referencia: String) {
    init {
        require(zona.isNotBlank() && zona.length <= 80)
        require(referencia.isNotBlank() && referencia.length <= 160)
    }
}
