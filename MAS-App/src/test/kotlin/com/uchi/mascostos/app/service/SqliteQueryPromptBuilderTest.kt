package com.uchi.mascostos.app.service

import kotlin.test.Test
import kotlin.test.assertTrue

class SqliteQueryPromptBuilderTest {

    @Test
    fun `inyecta la entrada del usuario y usa tablas DataSQL reales`() {
        val prompt = SqliteQueryPromptBuilder.build("partidas por subpresupuesto")

        assertTrue(prompt.contains("partidas por subpresupuesto"))
        assertTrue(prompt.contains("presupuesto_partida("))
        assertTrue(prompt.contains("proyecto_partidas("))
        assertTrue(prompt.contains("LIMIT 50"))
    }
}
