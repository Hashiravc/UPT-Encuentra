package com.example.uptencuentra.data.model

data class CaracteristicasDto(val marca: String, val modelo: String, val color: String)

// Instantánea local para la cola; el contrato remoto se definirá al conectar Supabase.
data class ReporteDto(
    val id: String,
    val tipo: String,
    val categoria: String,
    val descripcion: String,
    val fechaEvento: String,
    val zona: String,
    val referencia: String,
    val caracteristicas: CaracteristicasDto,
    val areaEntrega: String?,
    val estado: String,
    val version: Int,
    val particulares: com.example.uptencuentra.domain.model.CaracteristicasParticulares? = null,
    val fotoLocal: String? = null,
    val fotoRuta: String? = null
)
