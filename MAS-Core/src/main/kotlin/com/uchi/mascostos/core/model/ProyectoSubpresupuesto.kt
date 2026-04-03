package com.uchi.mascostos.core.model

data class ProyectoSubpresupuesto(
    val id: Long,
    val proyectoId: Long,
    val codSubpresupuesto: String?,
    val nombre: String,
    val orden: Int,
    val activo: Boolean
)
