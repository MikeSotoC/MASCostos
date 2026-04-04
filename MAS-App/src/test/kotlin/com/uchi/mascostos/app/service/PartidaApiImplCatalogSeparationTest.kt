package com.uchi.mascostos.app.service

import com.uchi.mascostos.core.model.InsumoBase
import com.uchi.mascostos.core.model.PartidaBase
import com.uchi.mascostos.core.model.ProyectoPartida
import com.uchi.mascostos.core.model.ProyectoPartidaDetalle
import com.uchi.mascostos.core.model.ProyectoSubpresupuesto
import com.uchi.mascostos.core.model.Subpresupuesto
import com.uchi.mascostos.core.ports.BaseCostosRepository
import com.uchi.mascostos.core.ports.DetallePartidaBaseRow
import com.uchi.mascostos.core.ports.PartidaJerarquiaNode
import com.uchi.mascostos.core.ports.ProyectoPresupuestoRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class PartidaApiImplCatalogSeparationTest {

    @Test
    fun `listar detalle usa descripcion y unidad del catalogo por codigo insumo`() {
        val baseRepo = FakeBaseCostosRepository()
        val proyectoRepo = FakeProyectoPresupuestoRepository()
        val api = PartidaApiImpl(baseRepo, proyectoRepo)

        val detalles = api.listarDetallePartidaProyecto(10L)

        assertEquals(1, detalles.size)
        assertEquals("Cemento Portland Tipo I", detalles.first().descripcion)
        assertEquals("bls", detalles.first().unidad)
    }

    private class FakeBaseCostosRepository : BaseCostosRepository {
        override fun listarSubpresupuestos(codPresupuesto: String): List<Subpresupuesto> = emptyList()
        override fun listarPartidasPresupuesto(codPresupuesto: String, codSubpresupuesto: String): List<PartidaBase> = emptyList()
        override fun obtenerPartida(codPartida: String): PartidaBase? = null

        override fun obtenerInsumo(codInsumo: String): InsumoBase? {
            return InsumoBase(
                codInsumo = codInsumo,
                descripcion = "Cemento Portland Tipo I",
                unidad = "bls"
            )
        }

        override fun listarDetallePartida(codPartida: String): List<DetallePartidaBaseRow> = emptyList()
        override fun obtenerPrecioInsumo(codPresupuesto: String, codSubpresupuesto: String, codInsumo: String): Double? = null
        override fun listarAncestrosPartida(codPartida: String): List<PartidaJerarquiaNode> = emptyList()
    }

    private class FakeProyectoPresupuestoRepository : ProyectoPresupuestoRepository {
        override fun listarSubpresupuestosProyecto(proyectoId: Long): List<ProyectoSubpresupuesto> = emptyList()
        override fun crearSubpresupuestoProyecto(
            proyectoId: Long,
            codSubpresupuesto: String?,
            nombre: String,
            orden: Int
        ): ProyectoSubpresupuesto = throw UnsupportedOperationException()

        override fun listarPartidasProyecto(proyectoId: Long, subpresupuestoId: Long): List<ProyectoPartida> = emptyList()

        override fun crearProyectoPartida(
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
        ): ProyectoPartida = throw UnsupportedOperationException()

        override fun obtenerProyectoPartida(proyectoId: Long, codPartidaBase: String): ProyectoPartida? = null
        override fun obtenerProyectoPartidaPorId(id: Long): ProyectoPartida? = null
        override fun actualizarProyectoPartidaMetrado(proyectoPartidaId: Long, metrado: Double, parcial: Double) = Unit
        override fun actualizarProyectoPartidaPrecioUnitario(proyectoPartidaId: Long, precioUnitario: Double) = Unit

        override fun listarDetalleProyectoPartida(proyectoPartidaId: Long): List<ProyectoPartidaDetalle> {
            return listOf(
                ProyectoPartidaDetalle(
                    id = 1,
                    proyectoPartidaId = proyectoPartidaId,
                    codInsumoBase = "020101",
                    descripcion = "020101",
                    unidad = null,
                    tipo = 1,
                    cuadrilla = null,
                    cantidad = 2.0,
                    precioUnitario = 35.0,
                    parcial = 70.0,
                    origen = "BASE",
                    activo = true
                )
            )
        }

        override fun crearDetalleProyectoPartida(
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
        ): ProyectoPartidaDetalle = throw UnsupportedOperationException()

        override fun actualizarDetalleProyectoPartidaPrecio(detalleId: Long, precioUnitario: Double, parcial: Double) = Unit
        override fun eliminarDetalleProyectoPartida(proyectoPartidaId: Long) = Unit
    }
}
