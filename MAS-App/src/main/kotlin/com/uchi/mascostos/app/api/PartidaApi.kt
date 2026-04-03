package com.uchi.mascostos.app.api

import com.uchi.mascostos.app.command.ProcesarPartidaProyectoCommand
import com.uchi.mascostos.app.dto.ProyectoPartidaDetalleDto
import com.uchi.mascostos.app.dto.ProyectoPartidaDto

interface PartidaApi {
    fun procesarPartidaProyecto(command: ProcesarPartidaProyectoCommand): ProyectoPartidaDto
    fun listarDetallePartidaProyecto(proyectoPartidaId: Long): List<ProyectoPartidaDetalleDto>
    fun actualizarMetrado(proyectoPartidaId: Long, metrado: Double): ProyectoPartidaDto
}