package com.uchi.mascostos.app.service

import kotlin.test.Test
import kotlin.test.assertTrue

class SqliteQueryPromptBuilderTest {

    @Test
    fun `inyecta la entrada del usuario en el template`() {
        val prompt = SqliteQueryPromptBuilder.build("productos con stock")

        assertTrue(prompt.contains("productos con stock"))
        assertTrue(prompt.contains("* SOLO generar consultas SELECT"))
        assertTrue(prompt.contains("LIMIT 50"))
    }
}
