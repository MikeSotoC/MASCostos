package com.uchi.mascostos.app.service

import com.uchi.mascostos.app.api.ProyectoApi
import com.uchi.mascostos.app.command.CrearProyectoCommand
import com.uchi.mascostos.app.dto.ProyectoDto
import com.uchi.mascostos.app.errors.ValidationException
import com.uchi.mascostos.core.model.Proyecto
import com.uchi.mascostos.core.ports.ProyectoRepository

class ProyectoApiImpl(
    private val proyectoRepository: ProyectoRepository
) : ProyectoApi {

    override fun listarProyectos(): List<ProyectoDto> =
        proyectoRepository.listar().map { it.toDto() }

    override fun crearProyecto(command: CrearProyectoCommand): ProyectoDto {
        val codigo = command.codigo.trim()
        val nombre = command.nombre.trim()
        val moneda = command.moneda.trim().uppercase()

        requireField(codigo.isNotEmpty(), "El código del proyecto es obligatorio")
        requireField(nombre.isNotEmpty(), "El nombre del proyecto es obligatorio")
        requireField(moneda.matches(Regex("[A-Z]{3}")), "La moneda debe usar formato ISO de 3 letras")

        val existente = proyectoRepository.obtenerPorCodigo(codigo)
        requireField(existente == null, "Ya existe un proyecto con código $codigo")

        return proyectoRepository.crear(
            codigo = codigo,
            nombre = nombre,
            cliente = command.cliente?.trim()?.ifBlank { null },
            ubicacion = command.ubicacion?.trim()?.ifBlank { null },
            moneda = moneda,
            observaciones = command.observaciones?.trim()?.ifBlank { null }
        ).toDto()
    }

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

    private fun requireField(isValid: Boolean, message: String) {
        if (!isValid) {
            throw ValidationException(message)
        }
    }
}
