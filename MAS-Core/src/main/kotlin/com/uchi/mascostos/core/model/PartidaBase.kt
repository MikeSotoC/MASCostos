package com.uchi.mascostos.core.model

data class PartidaBase(
    val codPartida: String,
    val descripcion: String,
    val unidad: String?,
    val precioUnitario: Double?,
    val horasHombre: Double?,
    val horasMaquina: Double?,
    val rendimientoMo: Double?,
    val rendimientoEq: Double?
)
