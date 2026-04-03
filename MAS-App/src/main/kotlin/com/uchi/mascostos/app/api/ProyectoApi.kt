package com.uchi.mascostos.app.api

import com.uchi.mascostos.app.command.CrearProyectoCommand
import com.uchi.mascostos.app.dto.ProyectoDto

interface ProyectoApi {
    fun listarProyectos(): List<ProyectoDto>
    fun crearProyecto(command: CrearProyectoCommand): ProyectoDto
    fun obtenerProyecto(id: Long): ProyectoDto?
}