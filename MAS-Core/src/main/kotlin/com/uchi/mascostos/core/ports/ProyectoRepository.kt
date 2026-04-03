package com.uchi.mascostos.core.ports

import com.uchi.mascostos.core.model.Proyecto

interface ProyectoRepository {
    fun listar(): List<Proyecto>
    fun obtenerPorId(id: Long): Proyecto?
    fun obtenerPorCodigo(codigo: String): Proyecto?
    fun crear(
        codigo: String,
        nombre: String,
        cliente: String?,
        ubicacion: String?,
        moneda: String = "PEN",
        observaciones: String? = null
    ): Proyecto
}