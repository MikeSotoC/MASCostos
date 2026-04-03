package com.uchi.mascostos.core.ports

data class DetallePartidaBaseRow(
    val codInsumo: String?,
    val descripcion: String,
    val unidad: String?,
    val tipo: Int?,
    val cuadrilla: Double?,
    val cantidad: Double
)
