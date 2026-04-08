package com.uchi.mascostos.core.sqlite

import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection

object SqlScriptRunner {
    fun ensureSqliteInitialized(
        connection: Connection,
        sqliteFile: Path,
        scriptPath: Path,
    ) {
        val shouldRun = !Files.exists(sqliteFile) || !hasCoreTables(connection)
        if (!shouldRun) return

        run(connection, scriptPath)
    }

    fun run(connection: Connection, scriptPath: Path) {
        val raw = scriptPath.toFile().readText(Charsets.UTF_8)
        val statements = raw
            .lineSequence()
            .filterNot { it.trimStart().startsWith("--") }
            .joinToString("\n")
            .split(';')
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        connection.autoCommit = false
        connection.createStatement().use { st ->
            statements.forEach { st.execute(it) }
        }
        connection.commit()
    }

    private fun hasCoreTables(connection: Connection): Boolean {
        val knownTables = setOf("proyecto", "presupuesto", "costo_unitario")
        val found = mutableSetOf<String>()

        connection.createStatement().use { st ->
            st.executeQuery("SELECT name FROM sqlite_master WHERE type='table'").use { rs ->
                while (rs.next()) {
                    found += rs.getString("name").lowercase()
                }
            }
        }

        return knownTables.all { it in found }
    }
}
