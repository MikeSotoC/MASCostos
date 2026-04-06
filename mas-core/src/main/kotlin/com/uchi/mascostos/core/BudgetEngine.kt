package com.uchi.mascostos.core

import com.uchi.mascostos.api.BudgetProvider
import com.uchi.mascostos.api.PluginContext
import com.uchi.mascostos.api.service.BudgetCatalogGateway
import com.uchi.mascostos.api.service.BudgetGateway
import com.uchi.mascostos.api.service.ProjectGateway
import com.uchi.mascostos.api.service.ReportGateway
import com.uchi.mascostos.core.ports.BudgetStructureRepository
import com.uchi.mascostos.core.ports.CostCatalogRepository
import com.uchi.mascostos.core.ports.ProjectItemRepository
import com.uchi.mascostos.core.ports.ProjectRepository
import com.uchi.mascostos.core.service.BudgetService
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager

class BudgetEngine(
    private val databasePath: Path,
) : PluginContext {

    private val providers = mutableMapOf<String, BudgetProvider>()
    private val connection: Connection by lazy {
        DriverManager.getConnection("jdbc:sqlite:${databasePath.toAbsolutePath()}")
    }

    override fun registerBudgetProvider(provider: BudgetProvider) {
        providers[provider.id] = provider
    }

    fun listProviders(): List<BudgetProvider> = providers.values.toList()

    fun openConnection(): Connection = connection

    private fun createService(
        projectRepository: ProjectRepository,
        projectItemRepository: ProjectItemRepository,
        costCatalogRepository: CostCatalogRepository,
        budgetStructureRepository: BudgetStructureRepository,
    ): BudgetService {
        return BudgetService(projectRepository, projectItemRepository, costCatalogRepository, budgetStructureRepository)
    }

    fun createBudgetGateway(
        projectRepository: ProjectRepository,
        projectItemRepository: ProjectItemRepository,
        costCatalogRepository: CostCatalogRepository,
        budgetStructureRepository: BudgetStructureRepository,
    ): BudgetGateway = createService(projectRepository, projectItemRepository, costCatalogRepository, budgetStructureRepository)

    fun createProjectGateway(
        projectRepository: ProjectRepository,
        projectItemRepository: ProjectItemRepository,
        costCatalogRepository: CostCatalogRepository,
        budgetStructureRepository: BudgetStructureRepository,
    ): ProjectGateway = createService(projectRepository, projectItemRepository, costCatalogRepository, budgetStructureRepository)

    fun createBudgetCatalogGateway(
        projectRepository: ProjectRepository,
        projectItemRepository: ProjectItemRepository,
        costCatalogRepository: CostCatalogRepository,
        budgetStructureRepository: BudgetStructureRepository,
    ): BudgetCatalogGateway = createService(projectRepository, projectItemRepository, costCatalogRepository, budgetStructureRepository)

    fun createReportGateway(
        projectRepository: ProjectRepository,
        projectItemRepository: ProjectItemRepository,
        costCatalogRepository: CostCatalogRepository,
        budgetStructureRepository: BudgetStructureRepository,
    ): ReportGateway = createService(projectRepository, projectItemRepository, costCatalogRepository, budgetStructureRepository)
}
