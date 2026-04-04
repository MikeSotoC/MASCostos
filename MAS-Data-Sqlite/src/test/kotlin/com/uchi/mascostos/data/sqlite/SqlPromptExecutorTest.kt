package com.uchi.mascostos.data.sqlite

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SqlPromptExecutorTest {

    @Test
    fun `ejecuta select simple y agrega limit por defecto`() {
        val dbPath = Files.createTempFile("prompt-exec", ".sqlite")
        val jdbcUrl = "jdbc:sqlite:${dbPath.toAbsolutePath()}"

        SQLiteConnector.openProyectos(jdbcUrl).use { connection ->
            connection.createStatement().use { st ->
                st.execute("CREATE TABLE demo (id INTEGER, nombre TEXT)")
                st.execute("INSERT INTO demo(id, nombre) VALUES (1, 'uno'), (2, 'dos')")
            }
        }

        val executor = SqlPromptExecutor(jdbcUrl = jdbcUrl, maxRows = 1)
        val rows = executor.executeSelect("SELECT id, nombre FROM demo ORDER BY id")

        assertEquals(1, rows.size)
        assertEquals(1L, rows.first()["id"])
        assertEquals("uno", rows.first()["nombre"])
    }

    @Test
    fun `rechaza sentencias no select`() {
        val executor = SqlPromptExecutor(jdbcUrl = "jdbc:sqlite::memory:")

        val ex = assertFailsWith<IllegalArgumentException> {
            executor.executeSelect("DELETE FROM demo")
        }

        assertTrue(ex.message!!.contains("SELECT"))
    }
}
