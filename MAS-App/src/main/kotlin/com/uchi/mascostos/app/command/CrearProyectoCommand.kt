package com.uchi.mascostos.app.command

data class CrearProyectoCommand(
    val codigo: String,
    val nombre: String,
    val cliente: String?,
    val ubicacion: String?,
    val moneda: String = "PEN",
    val observaciones: String? = null
)
