package com.uchi.mascostos.core.app

import com.uchi.mascostos.api.model.BudgetCatalogItem
import com.uchi.mascostos.api.model.BudgetOption
import com.uchi.mascostos.api.model.ProjectCostItem
import com.uchi.mascostos.api.model.ProjectRef
import com.uchi.mascostos.api.service.BudgetAppService
import com.uchi.mascostos.core.AppServices
import java.nio.file.Path

class CoreBudgetAppService(
    private val services: AppServices,
) : BudgetAppService {

    override fun listProjects(): List<ProjectRef> = services.projectGateway.listProjects()

    override fun createProject(name: String, location: String?): ProjectRef {
        return services.projectGateway.createProject(name, location)
    }

    override fun listBudgetsForProject(projectId: String): List<BudgetOption> {
        return services.budgetCatalogGateway.listBudgetsForProject(projectId)
    }

    override fun listCatalogForProject(projectId: String, budgetId: String?): List<BudgetCatalogItem> {
        return services.budgetCatalogGateway.listCatalogForProject(projectId, budgetId)
    }

    override fun listProjectItems(projectId: String): List<ProjectCostItem> {
        return services.projectGateway.listProjectItems(projectId)
    }

    override fun upsertProjectItem(projectId: String, costCode: String, quantity: Double) {
        services.projectGateway.addProjectItem(projectId, costCode, quantity)
    }

    override fun exportProjectCsv(projectId: String, outputPath: Path): Path {
        return services.reportGateway.exportProjectCsv(projectId, outputPath)
    }

    override fun exportProjectPdf(projectId: String, outputPath: Path): Path {
        return services.reportGateway.exportProjectPdf(projectId, outputPath)
    }
}
