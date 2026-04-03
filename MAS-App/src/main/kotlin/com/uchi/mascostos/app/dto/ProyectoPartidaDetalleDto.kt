package com.uchi.mascostos.app.dto

data class ProyectoPartidaDetalleDto(
    val id: Long,
    val codInsumoBase: String?,
    val descripcion: String,
    val unidad: String?,
    val cantidad: Double,
    val precioUnitario: Double,
    val parcial: Double
)
