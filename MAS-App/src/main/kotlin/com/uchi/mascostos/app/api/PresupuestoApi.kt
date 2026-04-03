package com.uchi.mascostos.app.api

import com.uchi.mascostos.app.command.CopiarPartidasBaseCommand
import com.uchi.mascostos.app.command.CopiarSubpresupuestosBaseCommand
import com.uchi.mascostos.app.dto.ProyectoPartidaDto
import com.uchi.mascostos.app.dto.SubpresupuestoDto

interface PresupuestoApi {
    fun copiarSubpresupuestosBase(command: CopiarSubpresupuestosBaseCommand): List<SubpresupuestoDto>
    fun listarSubpresupuestosProyecto(proyectoId: Long): List<SubpresupuestoDto>
    fun copiarPartidasBase(command: CopiarPartidasBaseCommand): List<ProyectoPartidaDto>
    fun listarPartidasProyecto(proyectoId: Long, subpresupuestoId: Long): List<ProyectoPartidaDto>
}