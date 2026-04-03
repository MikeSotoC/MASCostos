package com.uchi.mascostos.app.service

import com.uchi.mascostos.app.command.CopiarPartidasBaseCommand
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

class PresupuestoApiImplCatalogSeparationTest {

    @Test
    fun `al copiar partidas base el proyecto persiste codigo como descripcion tecnica`() {
        val baseRepo = FakeBaseCostosRepository()
        val proyectoRepo = FakeProyectoPresupuestoRepository()
        val api = PresupuestoApiImpl(baseRepo, proyectoRepo)

        val result = api.copiarPartidasBase(
            CopiarPartidasBaseCommand(
                proyectoId = 1L,
                codPresupuestoBase = "0401001",
                codSubpresupuestoBase = "01"
            )
        )

        assertEquals(1, result.size)
        assertEquals("010101", proyectoRepo.lastCreatedPartida?.codPartidaBase)
        assertEquals("010101", proyectoRepo.lastCreatedPartida?.descripcion)
        assertEquals(null, proyectoRepo.lastCreatedPartida?.unidad)
    }

    private class FakeBaseCostosRepository : BaseCostosRepository {
        override fun listarSubpresupuestos(codPresupuesto: String): List<Subpresupuesto> = emptyList()

        override fun listarPartidasPresupuesto(codPresupuesto: String, codSubpresupuesto: String): List<PartidaBase> {
            return listOf(
                PartidaBase(
                    codPartida = "010101",
                    descripcion = "Excavación",
                    unidad = "m3",
                    precioUnitario = 120.0,
                    horasHombre = null,
                    horasMaquina = null,
                    rendimientoMo = null,
                    rendimientoEq = null
                )
            )
        }

        override fun obtenerPartida(codPartida: String): PartidaBase? = null

        override fun listarDetallePartida(codPartida: String): List<DetallePartidaBaseRow> = emptyList()

        override fun obtenerPrecioInsumo(codPresupuesto: String, codSubpresupuesto: String, codInsumo: String): Double? = null

        override fun listarAncestrosPartida(codPartida: String): List<PartidaJerarquiaNode> = emptyList()
    }

    private class FakeProyectoPresupuestoRepository : ProyectoPresupuestoRepository {

        var lastCreatedPartida: ProyectoPartida? = null

        override fun listarSubpresupuestosProyecto(proyectoId: Long): List<ProyectoSubpresupuesto> {
            return listOf(
                ProyectoSubpresupuesto(
                    id = 10L,
                    proyectoId = proyectoId,
                    codSubpresupuesto = "01",
                    nombre = "Obras preliminares",
                    orden = 1,
                    activo = true
                )
            )
        }

        override fun crearSubpresupuestoProyecto(
            proyectoId: Long,
            codSubpresupuesto: String?,
            nombre: String,
            orden: Int
        ): ProyectoSubpresupuesto = throw UnsupportedOperationException()

        override fun listarPartidasProyecto(proyectoId: Long, subpresupuestoId: Long): List<ProyectoPartida> {
            return listOfNotNull(lastCreatedPartida)
        }

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
        ): ProyectoPartida {
            val created = ProyectoPartida(
                id = 200,
                proyectoId = proyectoId,
                subpresupuestoId = subpresupuestoId,
                codPartidaBase = codPartidaBase,
                descripcion = descripcion,
                unidad = unidad,
                metrado = 0.0,
                precioUnitario = precioUnitario,
                parcial = 0.0,
                rendimientoMo = rendimientoMo,
                rendimientoEq = rendimientoEq,
                horasHombre = horasHombre,
                horasMaquina = horasMaquina,
                origen = origen,
                orden = orden,
                activo = true
            )
            lastCreatedPartida = created
            return created
        }

        override fun obtenerProyectoPartida(proyectoId: Long, codPartidaBase: String): ProyectoPartida? = lastCreatedPartida

        override fun obtenerProyectoPartidaPorId(id: Long): ProyectoPartida? = lastCreatedPartida

        override fun actualizarProyectoPartidaMetrado(proyectoPartidaId: Long, metrado: Double, parcial: Double) = Unit

        override fun actualizarProyectoPartidaPrecioUnitario(proyectoPartidaId: Long, precioUnitario: Double) = Unit

        override fun listarDetalleProyectoPartida(proyectoPartidaId: Long): List<ProyectoPartidaDetalle> = emptyList()

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
