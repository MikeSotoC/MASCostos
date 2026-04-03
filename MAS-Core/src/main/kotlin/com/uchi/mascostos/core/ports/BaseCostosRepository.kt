package com.uchi.mascostos.core.ports

import com.uchi.mascostos.core.model.PartidaBase
import com.uchi.mascostos.core.model.Subpresupuesto

interface BaseCostosRepository {
    fun listarSubpresupuestos(codPresupuesto: String): List<Subpresupuesto>

    fun listarPartidasPresupuesto(
        codPresupuesto: String,
        codSubpresupuesto: String
    ): List<PartidaBase>

    fun listarDetallePartida(codPartida: String): List<DetallePartidaBaseRow>

    fun obtenerPrecioInsumo(
        codPresupuesto: String,
        codSubpresupuesto: String,
        codInsumo: String
    ): Double?

    fun listarAncestrosPartida(codPartida: String): List<PartidaJerarquiaNode>
}