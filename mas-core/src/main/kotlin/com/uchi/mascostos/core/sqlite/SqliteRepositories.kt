package com.uchi.mascostos.core.sqlite

import com.uchi.mascostos.core.domain.CostItem
import com.uchi.mascostos.core.domain.Project
import com.uchi.mascostos.core.domain.ProjectItem
import com.uchi.mascostos.core.ports.CostCatalogRepository
import com.uchi.mascostos.core.ports.ProjectItemRepository
import com.uchi.mascostos.core.ports.ProjectRepository
import java.sql.Connection
import java.util.UUID

class SqliteProjectRepository(private val connection: Connection) : ProjectRepository {
    init {
        connection.createStatement().use {
            it.execute(
                """
                CREATE TABLE IF NOT EXISTS mas_project (
                    id TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    location TEXT
                )
                """.trimIndent()
            )
        }
    }

    override fun create(name: String, location: String?): Project {
        val id = UUID.randomUUID().toString()
        connection.prepareStatement("INSERT INTO mas_project(id, name, location) VALUES (?, ?, ?)").use { ps ->
            ps.setString(1, id)
            ps.setString(2, name)
            ps.setString(3, location)
            ps.executeUpdate()
        }
        return Project(id = id, name = name, location = location)
    }

    override fun list(): List<Project> {
        return connection.createStatement().use { st ->
            st.executeQuery("SELECT id, name, location FROM mas_project ORDER BY name").use { rs ->
                buildList {
                    while (rs.next()) {
                        add(Project(rs.getString("id"), rs.getString("name"), rs.getString("location")))
                    }
                }
            }
        }
    }
}

class SqliteProjectItemRepository(private val connection: Connection) : ProjectItemRepository {
    init {
        connection.createStatement().use {
            it.execute(
                """
                CREATE TABLE IF NOT EXISTS mas_project_item (
                    project_id TEXT NOT NULL,
                    code TEXT NOT NULL,
                    quantity REAL NOT NULL,
                    PRIMARY KEY(project_id, code)
                )
                """.trimIndent()
            )
        }
    }

    override fun findByProject(projectId: String): List<ProjectItem> {
        return connection.prepareStatement(
            "SELECT project_id, code, quantity FROM mas_project_item WHERE project_id = ? ORDER BY code"
        ).use { ps ->
            ps.setString(1, projectId)
            ps.executeQuery().use { rs ->
                buildList {
                    while (rs.next()) {
                        add(ProjectItem(rs.getString("project_id"), rs.getString("code"), rs.getDouble("quantity")))
                    }
                }
            }
        }
    }

    override fun addItem(projectId: String, code: String, quantity: Double) {
        connection.prepareStatement(
            "INSERT OR REPLACE INTO mas_project_item(project_id, code, quantity) VALUES (?, ?, ?)"
        ).use { ps ->
            ps.setString(1, projectId)
            ps.setString(2, code)
            ps.setDouble(3, quantity)
            ps.executeUpdate()
        }
    }
}

class SqliteCostCatalogRepository(private val connection: Connection) : CostCatalogRepository {
    override fun findByCodes(codes: Set<String>): List<CostItem> {
        if (codes.isEmpty()) return emptyList()

        val placeholders = codes.joinToString(",") { "?" }
        val sql =
            "SELECT id_partida AS code, descripcion AS description, unidad, costo_unitario AS unit_cost " +
                "FROM partida_base WHERE id_partida IN ($placeholders)"

        return connection.prepareStatement(sql).use { ps ->
            codes.forEachIndexed { idx, code -> ps.setString(idx + 1, code) }
            ps.executeQuery().use { rs ->
                buildList {
                    while (rs.next()) {
                        add(
                            CostItem(
                                code = rs.getString("code"),
                                description = rs.getString("description"),
                                unit = rs.getString("unidad") ?: "und",
                                unitCost = rs.getDouble("unit_cost"),
                            )
                        )
                    }
                }
            }
        }
    }
}
