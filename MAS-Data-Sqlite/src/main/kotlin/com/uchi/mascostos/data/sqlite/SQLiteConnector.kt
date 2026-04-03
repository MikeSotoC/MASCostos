package com.uchi.mascostos.data.sqlite

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager

class SQLiteConnector {

    fun openBaseCostos(): Connection {
        return openAndBootstrap(
            jdbcUrl = DbPaths.BASE_COSTOS,
            seedResource = "/com/uchi/mascostos/data/sqlite/seed/base_catalogo.sql"
        )
    }

    fun openProyectos(): Connection {
        return openAndBootstrap(
            jdbcUrl = DbPaths.PROYECTOS,
            seedResource = "/com/uchi/mascostos/data/sqlite/seed/proyecto_ejemplo.sql"
        )
    }

    private fun openAndBootstrap(jdbcUrl: String, seedResource: String): Connection {
        ensureDirectoryFor(jdbcUrl)

        val dbPath = sqlitePathFromJdbc(jdbcUrl)
        val shouldSeed = dbPath != null && Files.notExists(dbPath)

        val connection = DriverManager.getConnection(jdbcUrl)

        if (shouldSeed) {
            executeSqlScript(connection, seedResource)
        }

        return connection
    }

    internal fun ensureDirectoryFor(jdbcUrl: String) {
        val path = sqlitePathFromJdbc(jdbcUrl) ?: return
        val parent = path.parent ?: return

        if (Files.notExists(parent)) {
            Files.createDirectories(parent)
        }
    }

    private fun executeSqlScript(connection: Connection, resourcePath: String) {
        val sql = SQLiteConnector::class.java.getResource(resourcePath)?.readText()
            ?: error("No se encontró el script de seed: $resourcePath")

        val statements = sql
            .lineSequence()
            .filterNot { it.trimStart().startsWith("--") }
            .joinToString("\n")
            .split(";")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        connection.autoCommit = false
        try {
            statements.forEach { stmt ->
                connection.createStatement().use { st ->
                    st.execute(stmt)
                }
            }
            connection.commit()
        } catch (ex: Exception) {
            connection.rollback()
            throw ex
        } finally {
            connection.autoCommit = true
        }
    }

    private fun sqlitePathFromJdbc(jdbcUrl: String): Path? {
        if (!jdbcUrl.startsWith("jdbc:sqlite:")) return null

        val rawPath = jdbcUrl.removePrefix("jdbc:sqlite:").trim()
        if (rawPath.isBlank()) return null

        // in-memory DBs or URI modes that do not represent filesystem paths
        if (rawPath == ":memory:" || rawPath.startsWith("file::memory:")) return null

        return Paths.get(rawPath)
    }
}
