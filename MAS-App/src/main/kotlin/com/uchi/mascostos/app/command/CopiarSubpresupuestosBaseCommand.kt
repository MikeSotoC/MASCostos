package com.uchi.mascostos.app.command

data class CopiarSubpresupuestosBaseCommand(
    val proyectoId: Long,
    val codPresupuestoBase: String
)
