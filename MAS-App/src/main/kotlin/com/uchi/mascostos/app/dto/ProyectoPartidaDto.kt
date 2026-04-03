package com.uchi.mascostos.app.dto

data class ProyectoPartidaDto(
    val id: Long,
    val codPartidaBase: String?,
    val descripcion: String,
    val unidad: String?,
    val metrado: Double,
    val precioUnitario: Double,
    val parcial: Double,
    val orden: Int
)
