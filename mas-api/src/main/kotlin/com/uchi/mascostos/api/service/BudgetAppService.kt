package com.uchi.mascostos.api.service

import com.uchi.mascostos.api.model.BudgetCatalogItem
import com.uchi.mascostos.api.model.BudgetOption
import com.uchi.mascostos.api.model.ProjectCostItem
import com.uchi.mascostos.api.model.ProjectRef
import java.nio.file.Path

/**
 * Servicio de aplicación unificado para clientes (desktop/android/futuro remoto).
 */
interface BudgetAppService {
    fun listProjects(): List<ProjectRef>
    fun createProject(name: String, location: String?): ProjectRef
    fun listBudgetsForProject(projectId: String): List<BudgetOption>
    fun listCatalogForProject(projectId: String, budgetId: String?): List<BudgetCatalogItem>
    fun listProjectItems(projectId: String): List<ProjectCostItem>
    fun upsertProjectItem(projectId: String, costCode: String, quantity: Double)
    fun exportProjectCsv(projectId: String, outputPath: Path): Path
}
