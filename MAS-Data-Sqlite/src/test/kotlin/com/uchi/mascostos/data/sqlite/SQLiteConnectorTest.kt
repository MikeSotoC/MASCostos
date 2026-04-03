package com.uchi.mascostos.data.sqlite

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertTrue

class SQLiteConnectorTest {

    @Test
    fun `crea directorio padre cuando no existe`() {
        val root = Files.createTempDirectory("mascostos-sqlite-test")
        val targetDir = root.resolve("nested").resolve("db")
        val jdbcUrl = "jdbc:sqlite:${targetDir.resolve("proyectos.db")}" 

        val connector = SQLiteConnector()
        connector.ensureDirectoryFor(jdbcUrl)

        assertTrue(Files.exists(targetDir))
    }

    @Test
    fun `ignora base en memoria`() {
        val connector = SQLiteConnector()
        connector.ensureDirectoryFor("jdbc:sqlite::memory:")

        assertTrue(true)
    }
}
