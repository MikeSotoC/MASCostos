package com.uchi.mascostos.core

import com.uchi.mascostos.api.service.BudgetCatalogGateway
import com.uchi.mascostos.api.service.BudgetGateway
import com.uchi.mascostos.api.service.ProjectGateway
import com.uchi.mascostos.api.service.ReportGateway
import com.uchi.mascostos.core.plugin.PluginRegistry
import com.uchi.mascostos.core.sqlite.SqlScriptRunner
import com.uchi.mascostos.core.sqlite.SqliteBudgetStructureRepository
import com.uchi.mascostos.core.sqlite.SqliteCostCatalogRepository
import com.uchi.mascostos.core.sqlite.SqliteProjectItemRepository
import com.uchi.mascostos.core.sqlite.SqliteProjectRepository
import com.uchi.mascostos.core.app.CoreBudgetAppService
import java.nio.file.Path

data class AppServices(
    val projectGateway: ProjectGateway,
    val budgetGateway: BudgetGateway,
    val budgetCatalogGateway: BudgetCatalogGateway,
    val reportGateway: ReportGateway,
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
        val structureRepo = SqliteBudgetStructureRepository(connection)

        PluginRegistry(engine).loadFromClasspath()

        return AppServices(
            projectGateway = engine.createProjectGateway(projectRepo, itemRepo, costRepo, structureRepo),
            budgetGateway = engine.createBudgetGateway(projectRepo, itemRepo, costRepo, structureRepo),
            budgetCatalogGateway = engine.createBudgetCatalogGateway(projectRepo, itemRepo, costRepo, structureRepo),
            reportGateway = engine.createReportGateway(projectRepo, itemRepo, costRepo, structureRepo),
        )
    }
}


fun AppServices.asBudgetAppService(): CoreBudgetAppService = CoreBudgetAppService(this)
