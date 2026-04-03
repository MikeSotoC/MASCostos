package com.uchi.mascostos.app.service

import com.uchi.mascostos.app.api.PresupuestoApi
import com.uchi.mascostos.app.command.CopiarPartidasBaseCommand
import com.uchi.mascostos.app.command.CopiarSubpresupuestosBaseCommand
import com.uchi.mascostos.app.dto.ProyectoPartidaDto
import com.uchi.mascostos.app.dto.SubpresupuestoDto
import com.uchi.mascostos.core.model.ProyectoPartida
import com.uchi.mascostos.core.model.ProyectoSubpresupuesto
import com.uchi.mascostos.core.ports.BaseCostosRepository
import com.uchi.mascostos.core.ports.ProyectoPresupuestoRepository

class PresupuestoApiImpl(
    private val baseCostosRepository: BaseCostosRepository,
    private val proyectoPresupuestoRepository: ProyectoPresupuestoRepository
) : PresupuestoApi {

    override fun copiarSubpresupuestosBase(command: CopiarSubpresupuestosBaseCommand): List<SubpresupuestoDto> {
        val base = baseCostosRepository.listarSubpresupuestos(command.codPresupuestoBase)
        base.forEachIndexed { index, item ->
            proyectoPresupuestoRepository.crearSubpresupuestoProyecto(
                proyectoId = command.proyectoId,
                codSubpresupuesto = item.codSubpresupuesto,
                nombre = item.descripcion,
                orden = index + 1
            )
        }
        return listarSubpresupuestosProyecto(command.proyectoId)
    }

    override fun listarSubpresupuestosProyecto(proyectoId: Long): List<SubpresupuestoDto> =
        proyectoPresupuestoRepository.listarSubpresupuestosProyecto(proyectoId).map { it.toDto() }

    override fun copiarPartidasBase(command: CopiarPartidasBaseCommand): List<ProyectoPartidaDto> {
        val sub = proyectoPresupuestoRepository
            .listarSubpresupuestosProyecto(command.proyectoId)
            .firstOrNull { it.codSubpresupuesto == command.codSubpresupuestoBase }
            ?: error("No existe el subpresupuesto del proyecto")

        val partidas = baseCostosRepository.listarPartidasPresupuesto(
            codPresupuesto = command.codPresupuestoBase,
            codSubpresupuesto = command.codSubpresupuestoBase
        )

        partidas.forEachIndexed { index, item ->
            proyectoPresupuestoRepository.crearProyectoPartida(
                proyectoId = command.proyectoId,
                subpresupuestoId = sub.id,
                codPartidaBase = item.codPartida,
                descripcion = item.descripcion,
                unidad = item.unidad,
                precioUnitario = item.precioUnitario ?: 0.0,
                rendimientoMo = item.rendimientoMo,
                rendimientoEq = item.rendimientoEq,
                horasHombre = item.horasHombre,
                horasMaquina = item.horasMaquina,
                origen = "BASE",
                orden = index + 1
            )
        }

        return proyectoPresupuestoRepository
            .listarPartidasProyecto(command.proyectoId, sub.id)
            .map { it.toDto() }
    }

    override fun listarPartidasProyecto(proyectoId: Long, subpresupuestoId: Long): List<ProyectoPartidaDto> =
        proyectoPresupuestoRepository.listarPartidasProyecto(proyectoId, subpresupuestoId).map { it.toDto() }

    private fun ProyectoSubpresupuesto.toDto() = SubpresupuestoDto(
        id = id,
        codSubpresupuesto = codSubpresupuesto,
        nombre = nombre,
        orden = orden
    )

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
}