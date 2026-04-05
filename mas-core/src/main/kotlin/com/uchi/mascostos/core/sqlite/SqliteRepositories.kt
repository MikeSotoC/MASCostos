package com.uchi.mascostos.core.sqlite

import com.uchi.mascostos.api.model.BudgetCatalogItem
import com.uchi.mascostos.core.domain.CostItem
import com.uchi.mascostos.core.domain.Project
import com.uchi.mascostos.core.domain.ProjectItem
import com.uchi.mascostos.core.ports.BudgetStructureRepository
import com.uchi.mascostos.core.ports.CostCatalogRepository
import com.uchi.mascostos.core.ports.ProjectItemRepository
import com.uchi.mascostos.core.ports.ProjectRepository
import java.sql.Connection
import java.util.UUID

private fun nextId12(): String = UUID.randomUUID().toString().replace("-", "").take(12)

class SqliteProjectRepository(private val connection: Connection) : ProjectRepository {
    override fun create(name: String, location: String?): Project {
        val id = nextId12()
        connection.prepareStatement(
            "INSERT INTO proyecto(id_proyecto, nombre_proyecto, lugar_proyecto) VALUES (?, ?, ?)"
        ).use { ps ->
            ps.setString(1, id)
            ps.setString(2, name)
            ps.setString(3, location)
            ps.executeUpdate()
        }
        return Project(id = id, name = name, location = location)
    }

    override fun list(): List<Project> {
        return connection.createStatement().use { st ->
            st.executeQuery(
                "SELECT id_proyecto, nombre_proyecto, lugar_proyecto FROM proyecto ORDER BY nombre_proyecto"
            ).use { rs ->
                buildList {
                    while (rs.next()) {
                        add(
                            Project(
                                id = rs.getString("id_proyecto"),
                                name = rs.getString("nombre_proyecto"),
                                location = rs.getString("lugar_proyecto"),
                            )
                        )
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
            "SELECT cu.id_costounitario AS code, cu.descripcion_costo AS description, " +
                "COALESCE(u.nombre_unidad, 'und') AS unidad, cu.costo_unitario AS unit_cost " +
                "FROM costo_unitario cu LEFT JOIN unidad u ON u.id_unidad = cu.id_unidad " +
                "WHERE cu.id_costounitario IN ($placeholders)"

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

class SqliteBudgetStructureRepository(private val connection: Connection) : BudgetStructureRepository {
    override fun listCatalogByProject(projectId: String): List<BudgetCatalogItem> {
        val sql =
            """
            SELECT
                t.id_titulo AS title_id,
                COALESCE(t.descripcion_titulo, 'SIN TITULO') AS title_name,
                cu.id_costounitario AS cost_code,
                COALESCE(cu.descripcion_costo, '') AS description,
                COALESCE(u.nombre_unidad, 'und') AS unit,
                COALESCE(cu.costo_unitario, 0) AS unit_cost
            FROM presupuesto p
            JOIN titulo t ON t.id_presupuesto = p.id_presupuesto
            JOIN costo_unitario cu ON cu.id_titulo = t.id_titulo
            LEFT JOIN unidad u ON u.id_unidad = cu.id_unidad
            WHERE p.id_proyecto = ?
            ORDER BY t.numeracion_titulo, cu.numeracion_costo
            """.trimIndent()

        return connection.prepareStatement(sql).use { ps ->
            ps.setString(1, projectId)
            ps.executeQuery().use { rs ->
                buildList {
                    while (rs.next()) {
                        add(
                            BudgetCatalogItem(
                                titleId = rs.getString("title_id"),
                                titleName = rs.getString("title_name"),
                                costCode = rs.getString("cost_code"),
                                description = rs.getString("description"),
                                unit = rs.getString("unit"),
                                unitCost = rs.getDouble("unit_cost"),
                            )
                        )
                    }
                }
            }
        }
    }
}
