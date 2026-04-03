package com.uchi.mascostos.app.dto

data class ProyectoDto(
    val id: Long,
    val codigo: String,
    val nombre: String,
    val cliente: String?,
    val ubicacion: String?,
    val moneda: String,
    val estado: String
)
