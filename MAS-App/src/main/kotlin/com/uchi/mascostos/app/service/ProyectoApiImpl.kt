package com.uchi.mascostos.app.service

import com.uchi.mascostos.app.api.ProyectoApi
import com.uchi.mascostos.app.command.CrearProyectoCommand
import com.uchi.mascostos.app.dto.ProyectoDto
import com.uchi.mascostos.core.model.Proyecto
import com.uchi.mascostos.core.ports.ProyectoRepository

class ProyectoApiImpl(
    private val proyectoRepository: ProyectoRepository
) : ProyectoApi {

    override fun listarProyectos(): List<ProyectoDto> =
        proyectoRepository.listar().map { it.toDto() }

    override fun crearProyecto(command: CrearProyectoCommand): ProyectoDto =
        proyectoRepository.crear(
            codigo = command.codigo,
            nombre = command.nombre,
            cliente = command.cliente,
            ubicacion = command.ubicacion,
            moneda = command.moneda,
            observaciones = command.observaciones
        ).toDto()

    override fun obtenerProyecto(id: Long): ProyectoDto? =
        proyectoRepository.obtenerPorId(id)?.toDto()

    private fun Proyecto.toDto() = ProyectoDto(
        id = id,
        codigo = codigo,
        nombre = nombre,
        cliente = cliente,
        ubicacion = ubicacion,
        moneda = moneda,
        estado = estado
    )
}