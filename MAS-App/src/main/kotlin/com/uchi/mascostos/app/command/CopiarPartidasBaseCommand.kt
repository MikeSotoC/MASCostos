package com.uchi.mascostos.app.command

data class CopiarPartidasBaseCommand(
    val proyectoId: Long,
    val codPresupuestoBase: String,
    val codSubpresupuestoBase: String
)
