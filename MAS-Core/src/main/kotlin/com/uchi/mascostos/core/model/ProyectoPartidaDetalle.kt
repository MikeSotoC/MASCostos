package com.uchi.mascostos.core.model

data class ProyectoPartidaDetalle (
    val id: Long,
    val proyectoPartidaId: Long,
    val codInsumoBase: String?,
    val descripcion: String,
    val unidad: String?,
    val tipo: Int?,
    val cuadrilla: Double?,
    val cantidad: Double,
    val precioUnitario: Double,
    val parcial: Double,
    val origen: String,
    val activo: Boolean
)