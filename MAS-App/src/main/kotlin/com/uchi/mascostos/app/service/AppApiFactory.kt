package com.uchi.mascostos.app.service

import com.uchi.mascostos.app.api.AppApi
import com.uchi.mascostos.core.ports.BaseCostosRepository
import com.uchi.mascostos.core.ports.ProyectoPresupuestoRepository
import com.uchi.mascostos.core.ports.ProyectoRepository

object AppApiFactory {
    fun create(
        proyectoRepository: ProyectoRepository,
        baseCostosRepository: BaseCostosRepository,
        proyectoPresupuestoRepository: ProyectoPresupuestoRepository
    ): AppApi {
        return AppApi(
            proyectoApi = ProyectoApiImpl(proyectoRepository),
            presupuestoApi = PresupuestoApiImpl(baseCostosRepository, proyectoPresupuestoRepository),
            partidaApi = PartidaApiImpl(baseCostosRepository, proyectoPresupuestoRepository),
            presupuestoSheetApi = PresupuestoSheetApiImpl(
                proyectoPresupuestoRepository = proyectoPresupuestoRepository,
                baseCostosRepository = baseCostosRepository
            )
        )
    }
}