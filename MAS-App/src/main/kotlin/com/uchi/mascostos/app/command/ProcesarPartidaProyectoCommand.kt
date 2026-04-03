package com.uchi.mascostos.app.command

data class ProcesarPartidaProyectoCommand(
    val proyectoId: Long,
    val codPresupuestoBase: String,
    val codSubpresupuestoBase: String,
    val codPartidaBase: String,
    val metrado: Double
)
