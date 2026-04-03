package com.uchi.mascostos.core.model

data class ProyectoPartida(
    val id: Long,
    val proyectoId: Long,
    val subpresupuestoId: Long?,
    val codPartidaBase: String?,
    val descripcion: String,
    val unidad: String?,
    val metrado: Double,
    val precioUnitario: Double,
    val parcial: Double,
    val rendimientoMo: Double?,
    val rendimientoEq: Double?,
    val horasHombre: Double?,
    val horasMaquina: Double?,
    val origen: String,
    val orden: Int,
    val activo: Boolean
)
