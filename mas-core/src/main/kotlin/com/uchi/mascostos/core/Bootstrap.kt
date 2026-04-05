package com.uchi.mascostos.core

import com.uchi.mascostos.api.service.BudgetGateway
import com.uchi.mascostos.api.service.ProjectGateway
import com.uchi.mascostos.core.plugin.PluginRegistry
import com.uchi.mascostos.core.sqlite.SqlScriptRunner
import com.uchi.mascostos.core.sqlite.SqliteCostCatalogRepository
import com.uchi.mascostos.core.sqlite.SqliteProjectItemRepository
import com.uchi.mascostos.core.sqlite.SqliteProjectRepository
import java.nio.file.Path

data class AppServices(
    val projectGateway: ProjectGateway,
    val budgetGateway: BudgetGateway,
)

object Bootstrap {
    /**
     * Levanta servicios base usando un archivo SQLite.
     */
    fun bootWithSqlite(
        sqliteFile: Path,
        schemaScript: Path? = null,
    ): AppServices {
        val engine = BudgetEngine(databasePath = sqliteFile)
        val connection = engine.openConnection()

        if (schemaScript != null) {
            SqlScriptRunner.ensureSqliteInitialized(
                connection = connection,
                sqliteFile = sqliteFile,
                scriptPath = schemaScript,
            )
        }

        val projectRepo = SqliteProjectRepository(connection)
        val itemRepo = SqliteProjectItemRepository(connection)
        val costRepo = SqliteCostCatalogRepository(connection)

        PluginRegistry(engine).loadFromClasspath()

        return AppServices(
            projectGateway = engine.createProjectGateway(projectRepo, itemRepo, costRepo),
            budgetGateway = engine.createBudgetGateway(projectRepo, itemRepo, costRepo),
        )
    }
}
