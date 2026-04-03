package com.uchi.mascostos.app.dto

data class PresupuestoSheetRowDto(
    val tipo: PresupuestoSheetRowType,
    val itemVisual: String,
    val descripcion: String,
    val unidad: String?,
    val metrado: Double?,
    val precioUnitario: Double?,
    val parcial: Double?,
    val nivel: Int,
    val codPartidaBase: String? = null,
    val proyectoPartidaId: Long? = null
)