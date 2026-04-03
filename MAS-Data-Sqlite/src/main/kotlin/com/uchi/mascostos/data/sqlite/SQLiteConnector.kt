package com.uchi.mascostos.data.sqlite

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager

class SQLiteConnector {

    fun openBaseCostos(): Connection {
        ensureDirectoryFor(DbPaths.BASE_COSTOS)
        return DriverManager.getConnection(DbPaths.BASE_COSTOS)
    }

    fun openProyectos(): Connection {
        ensureDirectoryFor(DbPaths.PROYECTOS)
        return DriverManager.getConnection(DbPaths.PROYECTOS)
    }

    internal fun ensureDirectoryFor(jdbcUrl: String) {
        val path = sqlitePathFromJdbc(jdbcUrl) ?: return
        val parent = path.parent ?: return

        if (Files.notExists(parent)) {
            Files.createDirectories(parent)
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
