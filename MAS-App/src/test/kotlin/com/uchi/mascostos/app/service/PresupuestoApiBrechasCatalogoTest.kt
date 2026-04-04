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

class PresupuestoApiBrechasCatalogoTest {

    @Test
    fun `detecta brecha cuando descripcion y unidad difieren del catalogo`() {
        val baseRepo = FakeBaseCostosRepository()
        val proyectoRepo = FakeProyectoPresupuestoRepository()
        val api = PresupuestoApiImpl(baseRepo, proyectoRepo)

        val brechas = api.detectarBrechasCatalogo(proyectoId = 1L, subpresupuestoId = 10L)

        assertEquals(1, brechas.size)
        assertEquals(true, brechas.first().catalogoEncontrado)
        assertEquals(true, brechas.first().requiereNormalizacion)
        assertEquals("Excavación manual", brechas.first().descripcionCatalogo)
    }

    @Test
    fun `no detecta brecha cuando proyecto coincide con catalogo`() {
        val baseRepo = FakeBaseCostosRepository()
        val proyectoRepo = FakeProyectoPresupuestoRepository(descripcion = "Excavación manual", unidad = "m3")
        val api = PresupuestoApiImpl(baseRepo, proyectoRepo)

        val brechas = api.detectarBrechasCatalogo(proyectoId = 1L, subpresupuestoId = 10L)

        assertEquals(1, brechas.size)
        assertEquals(true, brechas.first().catalogoEncontrado)
        assertEquals(false, brechas.first().requiereNormalizacion)
    }

    private class FakeBaseCostosRepository : BaseCostosRepository {
        override fun listarSubpresupuestos(codPresupuesto: String): List<Subpresupuesto> = emptyList()
        override fun listarPartidasPresupuesto(codPresupuesto: String, codSubpresupuesto: String): List<PartidaBase> = emptyList()

        override fun obtenerPartida(codPartida: String): PartidaBase {
            return PartidaBase(
                codPartida = codPartida,
                descripcion = "Excavación manual",
                unidad = "m3",
                precioUnitario = 100.0,
                horasHombre = null,
                horasMaquina = null,
                rendimientoMo = null,
                rendimientoEq = null
            )
        }

        override fun obtenerInsumo(codInsumo: String): InsumoBase? = null
        override fun listarDetallePartida(codPartida: String): List<DetallePartidaBaseRow> = emptyList()
        override fun obtenerPrecioInsumo(codPresupuesto: String, codSubpresupuesto: String, codInsumo: String): Double? = null
        override fun listarAncestrosPartida(codPartida: String): List<PartidaJerarquiaNode> = emptyList()
    }

    private class FakeProyectoPresupuestoRepository(
        private val descripcion: String = "DESC PROYECTO",
        private val unidad: String? = "und"
    ) : ProyectoPresupuestoRepository {
        override fun listarSubpresupuestosProyecto(proyectoId: Long): List<ProyectoSubpresupuesto> = emptyList()

        override fun crearSubpresupuestoProyecto(
            proyectoId: Long,
            codSubpresupuesto: String?,
            nombre: String,
            orden: Int
        ): ProyectoSubpresupuesto = throw UnsupportedOperationException()

        override fun listarPartidasProyecto(proyectoId: Long, subpresupuestoId: Long): List<ProyectoPartida> {
            return listOf(
                ProyectoPartida(
                    id = 900,
                    proyectoId = proyectoId,
                    subpresupuestoId = subpresupuestoId,
                    codPartidaBase = "010101",
                    descripcion = descripcion,
                    unidad = unidad,
                    metrado = 10.0,
                    precioUnitario = 100.0,
                    parcial = 1000.0,
                    rendimientoMo = null,
                    rendimientoEq = null,
                    horasHombre = null,
                    horasMaquina = null,
                    origen = "BASE",
                    orden = 1,
                    activo = true
                )
            )
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
        ): ProyectoPartida = throw UnsupportedOperationException()

        override fun obtenerProyectoPartida(proyectoId: Long, codPartidaBase: String): ProyectoPartida? = null
        override fun obtenerProyectoPartidaPorId(id: Long): ProyectoPartida? = null
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
