package com.uchi.mascostos.app.service

import kotlin.test.Test
import kotlin.test.assertTrue

class SqliteQueryPromptBuilderTest {

    @Test
    fun `inyecta la entrada del usuario y usa schema SQLDelphin basica`() {
        val prompt = SqliteQueryPromptBuilder.build("partidas por presupuesto")

        assertTrue(prompt.contains("partidas por presupuesto"))
        assertTrue(prompt.contains("SCHEMA_SQLDELPHIN_BASICA"))
        assertTrue(prompt.contains("costo_unitario("))
        assertTrue(prompt.contains("LIMIT 50"))
    }

    @Test
    fun `permite inyectar schema completo en runtime`() {
        val customSchema = "tabla_demo(id, descripcion)"
        val prompt = SqliteQueryPromptBuilder.build("listar demo", customSchema)

        assertTrue(prompt.contains("listar demo"))
        assertTrue(prompt.contains(customSchema))
    }

    @Test
    fun `aplica contexto de proyecto cuando se envía id_proyecto`() {
        val prompt = SqliteQueryPromptBuilder.buildForProyecto(
            userInput = "mostrar costos",
            proyectoId = "PR-001"
        )

        assertTrue(prompt.contains("mostrar costos"))
        assertTrue(prompt.contains("id_proyecto = 'PR-001'"))
    }
}
