package com.uchi.mascostos.core.sqlite

import java.nio.file.Path
import java.sql.Connection

object SqlScriptRunner {
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
}
