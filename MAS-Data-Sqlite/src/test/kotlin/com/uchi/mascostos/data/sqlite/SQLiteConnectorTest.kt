package com.uchi.mascostos.data.sqlite

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SQLiteConnectorTest {

    @Test
    fun `crea directorio padre cuando no existe`() {
        val root = Files.createTempDirectory("mascostos-sqlite-test")
        val targetDir = root.resolve("nested").resolve("db")
        val jdbcUrl = "jdbc:sqlite:${targetDir.resolve("SQLDelphin_basica.sqlite")}" 

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

    @Test
    fun `usa SQLDelphin_basica sql local para bootstrap cuando existe`() {
        val originalUserDir = System.getProperty("user.dir")
        val workDir = Files.createTempDirectory("mascostos-bootstrap-sql")
        val sqlFile = workDir.resolve("SQLDelphin_basica.sql")
        Files.writeString(
            sqlFile,
            """
            CREATE TABLE tabla_local (id INTEGER PRIMARY KEY, nombre TEXT);
            INSERT INTO tabla_local(id, nombre) VALUES (1, 'ok');
            """.trimIndent()
        )

        val dbPath = workDir.resolve("data").resolve("SQLDelphin_basica.sqlite")
        val jdbcUrl = "jdbc:sqlite:$dbPath"

        try {
            System.setProperty("user.dir", workDir.toString())

            val connector = SQLiteConnector()
            connector.openProyectos(jdbcUrl).use { connection ->
                val count = connection.createStatement().use { st ->
                    st.executeQuery("SELECT COUNT(*) FROM tabla_local").use { rs ->
                        rs.next()
                        rs.getInt(1)
                    }
                }
                assertEquals(1, count)
            }
        } finally {
            if (originalUserDir == null) {
                System.clearProperty("user.dir")
            } else {
                System.setProperty("user.dir", originalUserDir)
            }
        }
    }
}
