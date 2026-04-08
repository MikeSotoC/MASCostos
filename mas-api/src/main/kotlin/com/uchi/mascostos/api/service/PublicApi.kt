package com.uchi.mascostos.api.service

import com.uchi.mascostos.api.model.BudgetCatalogItem
import com.uchi.mascostos.api.model.BudgetOption
import com.uchi.mascostos.api.model.BudgetResult
import com.uchi.mascostos.api.model.ProjectCostItem
import com.uchi.mascostos.api.model.ProjectRef
import java.nio.file.Path

interface ProjectGateway {
    fun createProject(name: String, location: String?): ProjectRef
    fun listProjects(): List<ProjectRef>
    fun addProjectItem(projectId: String, costCode: String, quantity: Double)
    fun listProjectItems(projectId: String): List<ProjectCostItem>
}

interface BudgetGateway {
    fun estimate(projectId: String): BudgetResult
}

interface BudgetCatalogGateway {
    fun listBudgetsForProject(projectId: String): List<BudgetOption>
    fun listCatalogForProject(projectId: String, budgetId: String? = null): List<BudgetCatalogItem>
}

interface ReportGateway {
    fun exportProjectCsv(projectId: String, outputPath: Path): Path
}
