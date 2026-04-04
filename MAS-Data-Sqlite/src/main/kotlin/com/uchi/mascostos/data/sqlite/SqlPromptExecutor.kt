package com.uchi.mascostos.data.sqlite

import java.sql.DriverManager

class SqlPromptExecutor(
    private val jdbcUrl: String = DbPaths.PROYECTOS,
    private val maxRows: Int = 50
) {

    fun executeSelect(sql: String): List<Map<String, Any?>> {
        val normalized = sql.trim().trimEnd(';')
        require(normalized.startsWith("select", ignoreCase = true)) {
            "Solo se permiten consultas SELECT"
        }
        require(!normalized.contains("--")) { "No se permiten comentarios SQL" }
        require(!normalized.contains(';')) { "No se permiten múltiples sentencias" }

        val limitedSql = enforceLimit(normalized)

        DriverManager.getConnection(jdbcUrl).use { connection ->
            connection.prepareStatement(limitedSql).use { statement ->
                statement.maxRows = maxRows
                statement.executeQuery().use { rs ->
                    val meta = rs.metaData
                    val cols = (1..meta.columnCount).map { idx -> meta.getColumnLabel(idx) }
                    val rows = mutableListOf<Map<String, Any?>>()
                    while (rs.next()) {
                        rows += cols.associateWith { col -> rs.getObject(col) }
                    }
                    return rows
                }
            }
        }
    }

    private fun enforceLimit(sql: String): String {
        return if (Regex("\\blimit\\s+\\d+", RegexOption.IGNORE_CASE).containsMatchIn(sql)) {
            sql
        } else {
            "$sql LIMIT $maxRows"
        }
    }
}
