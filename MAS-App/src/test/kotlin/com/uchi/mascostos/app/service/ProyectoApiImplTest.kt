package com.uchi.mascostos.app.service

import com.uchi.mascostos.app.command.CrearProyectoCommand
import com.uchi.mascostos.app.errors.ValidationException
import com.uchi.mascostos.core.model.Proyecto
import com.uchi.mascostos.core.ports.ProyectoRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ProyectoApiImplTest {

    @Test
    fun `lanza error si codigo esta vacio`() {
        val repo = FakeProyectoRepository()
        val api = ProyectoApiImpl(repo)

        assertFailsWith<ValidationException> {
            api.crearProyecto(
                CrearProyectoCommand(
                    codigo = "   ",
                    nombre = "Proyecto Uno",
                    cliente = null,
                    ubicacion = null
                )
            )
        }
    }

    @Test
    fun `lanza error si codigo ya existe`() {
        val repo = FakeProyectoRepository(existingCodes = mutableSetOf("PRJ-001"))
        val api = ProyectoApiImpl(repo)

        assertFailsWith<ValidationException> {
            api.crearProyecto(
                CrearProyectoCommand(
                    codigo = "PRJ-001",
                    nombre = "Proyecto Duplicado",
                    cliente = null,
                    ubicacion = null
                )
            )
        }
    }

    @Test
    fun `normaliza moneda y espacios al crear proyecto`() {
        val repo = FakeProyectoRepository()
        val api = ProyectoApiImpl(repo)

        val created = api.crearProyecto(
            CrearProyectoCommand(
                codigo = " PRJ-010 ",
                nombre = "  Proyecto Centro  ",
                cliente = "  Cliente SAC ",
                ubicacion = " Lima ",
                moneda = "pen",
                observaciones = "  Observacion inicial  "
            )
        )

        assertEquals("PRJ-010", created.codigo)
        assertEquals("Proyecto Centro", created.nombre)
        assertEquals("PEN", created.moneda)
        assertEquals(1, repo.created.size)
    }

    private class FakeProyectoRepository(
        private val existingCodes: MutableSet<String> = mutableSetOf()
    ) : ProyectoRepository {

        val created = mutableListOf<Proyecto>()
        private var nextId = 1L

        override fun listar(): List<Proyecto> = created.toList()

        override fun obtenerPorId(id: Long): Proyecto? = created.firstOrNull { it.id == id }

        override fun obtenerPorCodigo(codigo: String): Proyecto? {
            if (codigo in existingCodes) {
                return Proyecto(
                    id = 99,
                    codigo = codigo,
                    nombre = "Existente",
                    cliente = null,
                    ubicacion = null,
                    moneda = "PEN",
                    estado = "ACTIVO",
                    observaciones = null
                )
            }
            return created.firstOrNull { it.codigo == codigo }
        }

        override fun crear(
            codigo: String,
            nombre: String,
            cliente: String?,
            ubicacion: String?,
            moneda: String,
            observaciones: String?
        ): Proyecto {
            val proyecto = Proyecto(
                id = nextId++,
                codigo = codigo,
                nombre = nombre,
                cliente = cliente,
                ubicacion = ubicacion,
                moneda = moneda,
                estado = "ACTIVO",
                observaciones = observaciones
            )
            created += proyecto
            return proyecto
        }
    }
}
