package com.uchi.mascostos.core.service

import com.uchi.mascostos.api.model.BudgetCatalogItem
import com.uchi.mascostos.api.model.BudgetLine
import com.uchi.mascostos.api.model.BudgetResult
import com.uchi.mascostos.api.model.ProjectRef
import com.uchi.mascostos.api.service.BudgetCatalogGateway
import com.uchi.mascostos.api.service.BudgetGateway
import com.uchi.mascostos.api.service.ProjectGateway
import com.uchi.mascostos.core.ports.BudgetStructureRepository
import com.uchi.mascostos.core.ports.CostCatalogRepository
import com.uchi.mascostos.core.ports.ProjectItemRepository
import com.uchi.mascostos.core.ports.ProjectRepository

class BudgetService(
    private val projectRepository: ProjectRepository,
    private val projectItemRepository: ProjectItemRepository,
    private val costCatalogRepository: CostCatalogRepository,
    private val budgetStructureRepository: BudgetStructureRepository,
) : ProjectGateway, BudgetGateway, BudgetCatalogGateway {

    override fun createProject(name: String, location: String?): ProjectRef {
        val created = projectRepository.create(name = name, location = location)
        return ProjectRef(id = created.id, name = created.name)
    }

    override fun listProjects(): List<ProjectRef> {
        return projectRepository.list().map { ProjectRef(id = it.id, name = it.name) }
    }

    override fun addProjectItem(projectId: String, costCode: String, quantity: Double) {
        require(costCode.isNotBlank()) { "El código de costo no puede estar vacío" }
        require(quantity > 0) { "La cantidad debe ser mayor a cero" }
        projectItemRepository.addItem(projectId = projectId, code = costCode, quantity = quantity)
    }

    override fun listCatalogForProject(projectId: String): List<BudgetCatalogItem> {
        return budgetStructureRepository.listCatalogByProject(projectId)
    }

    override fun estimate(projectId: String): BudgetResult {
        val project = projectRepository.list().firstOrNull { it.id == projectId }
            ?: error("Proyecto no encontrado: $projectId")

        val projectItems = projectItemRepository.findByProject(projectId)
        val costsByCode = costCatalogRepository
            .findByCodes(projectItems.map { it.code }.toSet())
            .associateBy { it.code }

        val lines = projectItems.mapNotNull { item ->
            val cost = costsByCode[item.code] ?: return@mapNotNull null
            BudgetLine(
                code = item.code,
                description = cost.description,
                unit = cost.unit,
                quantity = item.quantity,
                unitCost = cost.unitCost,
            )
        }

        return BudgetResult(
            project = ProjectRef(id = project.id, name = project.name),
            lines = lines,
        )
    }
}
