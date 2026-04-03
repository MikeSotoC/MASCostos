package com.uchi.mascostos.core.model

data class Proyecto(
    val id: Long,
    val codigo: String,
    val nombre: String,
    val cliente: String?,
    val ubicacion: String?,
    val moneda: String,
    val estado: String,
    val observaciones: String?
)
