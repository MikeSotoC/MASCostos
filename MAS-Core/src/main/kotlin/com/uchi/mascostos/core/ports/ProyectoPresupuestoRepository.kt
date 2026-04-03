package com.uchi.mascostos.core.ports

import com.uchi.mascostos.core.model.ProyectoPartida
import com.uchi.mascostos.core.model.ProyectoPartidaDetalle
import com.uchi.mascostos.core.model.ProyectoSubpresupuesto

interface ProyectoPresupuestoRepository {


    fun listarSubpresupuestosProyecto(proyectoId: Long): List<ProyectoSubpresupuesto>

    fun crearSubpresupuestoProyecto(
        proyectoId: Long,
        codSubpresupuesto: String?,
        nombre: String,
        orden: Int
    ): ProyectoSubpresupuesto

    fun listarPartidasProyecto(
        proyectoId: Long,
        subpresupuestoId: Long
    ): List<ProyectoPartida>

    fun crearProyectoPartida(
        proyectoId: Long,
        subpresupuestoId: Long?,
        codPartidaBase: String?,
        descripcion: String,
        unidad: String?,
        precioUnitario: Double,
        rendimientoMo: Double?,
        rendimientoEq: Double?,
        horasHombre: Double?,
        horasMaquina: Double?,
        origen: String,
        orden: Int
    ): ProyectoPartida

    fun obtenerProyectoPartida(
        proyectoId: Long,
        codPartidaBase: String
    ): ProyectoPartida?

    fun obtenerProyectoPartidaPorId(id: Long): ProyectoPartida?

    fun actualizarProyectoPartidaMetrado(
        proyectoPartidaId: Long,
        metrado: Double,
        parcial: Double
    )

    fun actualizarProyectoPartidaPrecioUnitario(
        proyectoPartidaId: Long,
        precioUnitario: Double
    )

    fun listarDetalleProyectoPartida(proyectoPartidaId: Long): List<ProyectoPartidaDetalle>

    fun crearDetalleProyectoPartida(
        proyectoPartidaId: Long,
        codInsumoBase: String?,
        descripcion: String,
        unidad: String?,
        tipo: Int?,
        cuadrilla: Double?,
        cantidad: Double,
        precioUnitario: Double,
        parcial: Double,
        origen: String
    ): ProyectoPartidaDetalle

    fun actualizarDetalleProyectoPartidaPrecio(
        detalleId: Long,
        precioUnitario: Double,
        parcial: Double
    )

    fun eliminarDetalleProyectoPartida(proyectoPartidaId: Long)
}