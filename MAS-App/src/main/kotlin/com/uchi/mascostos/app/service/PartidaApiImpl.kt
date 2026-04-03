package com.uchi.mascostos.app.service

import com.uchi.mascostos.app.api.PartidaApi
import com.uchi.mascostos.app.command.ProcesarPartidaProyectoCommand
import com.uchi.mascostos.app.dto.ProyectoPartidaDetalleDto
import com.uchi.mascostos.app.dto.ProyectoPartidaDto
import com.uchi.mascostos.core.model.ProyectoPartida
import com.uchi.mascostos.core.model.ProyectoPartidaDetalle
import com.uchi.mascostos.core.ports.BaseCostosRepository
import com.uchi.mascostos.core.ports.ProyectoPresupuestoRepository

class PartidaApiImpl(
    private val baseCostosRepository: BaseCostosRepository,
    private val proyectoPresupuestoRepository: ProyectoPresupuestoRepository
) : PartidaApi {

    override fun procesarPartidaProyecto(command: ProcesarPartidaProyectoCommand): ProyectoPartidaDto {
        val partida = proyectoPresupuestoRepository.obtenerProyectoPartida(
            proyectoId = command.proyectoId,
            codPartidaBase = command.codPartidaBase
        ) ?: error("No existe la partida en el proyecto")

        proyectoPresupuestoRepository.eliminarDetalleProyectoPartida(partida.id)

        val detalleBase = baseCostosRepository.listarDetallePartida(command.codPartidaBase)

        detalleBase.forEach { row ->
            val precio = row.codInsumo?.let {
                baseCostosRepository.obtenerPrecioInsumo(
                    codPresupuesto = command.codPresupuestoBase,
                    codSubpresupuesto = command.codSubpresupuestoBase,
                    codInsumo = it
                )
            } ?: 0.0

            val parcial = row.cantidad * (precio ?: 0.0)

            proyectoPresupuestoRepository.crearDetalleProyectoPartida(
                proyectoPartidaId = partida.id,
                codInsumoBase = row.codInsumo,
                descripcion = row.descripcion,
                unidad = row.unidad,
                tipo = row.tipo,
                cuadrilla = row.cuadrilla,
                cantidad = row.cantidad,
                precioUnitario = precio ?: 0.0,
                parcial = parcial,
                origen = "BASE"
            )
        }

        val total = proyectoPresupuestoRepository
            .listarDetalleProyectoPartida(partida.id)
            .sumOf { it.parcial }

        proyectoPresupuestoRepository.actualizarProyectoPartidaPrecioUnitario(partida.id, total)

        val parcialPartida = command.metrado * total
        proyectoPresupuestoRepository.actualizarProyectoPartidaMetrado(partida.id, command.metrado, parcialPartida)

        val actualizada = proyectoPresupuestoRepository.obtenerProyectoPartida(
            proyectoId = command.proyectoId,
            codPartidaBase = command.codPartidaBase
        ) ?: error("No se pudo recargar la partida")

        return actualizada.toDto()
    }

    override fun listarDetallePartidaProyecto(proyectoPartidaId: Long): List<ProyectoPartidaDetalleDto> =
        proyectoPresupuestoRepository.listarDetalleProyectoPartida(proyectoPartidaId).map { it.toDto() }

    override fun actualizarMetrado(proyectoPartidaId: Long, metrado: Double): ProyectoPartidaDto {
        val detalles = proyectoPresupuestoRepository.listarDetalleProyectoPartida(proyectoPartidaId)
        val precioUnitario = detalles.sumOf { it.parcial }
        val parcial = metrado * precioUnitario

        proyectoPresupuestoRepository.actualizarProyectoPartidaMetrado(proyectoPartidaId, metrado, parcial)

        val partida = proyectoPresupuestoRepository.obtenerProyectoPartidaPorId(proyectoPartidaId)
            ?: error("No existe la partida")

        return partida.toDto()
    }

    private fun ProyectoPartida.toDto() = ProyectoPartidaDto(
        id = id,
        codPartidaBase = codPartidaBase,
        descripcion = descripcion,
        unidad = unidad,
        metrado = metrado,
        precioUnitario = precioUnitario,
        parcial = parcial,
        orden = orden
    )

    private fun ProyectoPartidaDetalle.toDto() = ProyectoPartidaDetalleDto(
        id = id,
        codInsumoBase = codInsumoBase,
        descripcion = descripcion,
        unidad = unidad,
        cantidad = cantidad,
        precioUnitario = precioUnitario,
        parcial = parcial
    )
}