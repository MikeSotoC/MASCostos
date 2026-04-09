package com.uchi.mascostos.core.service

import com.uchi.mascostos.api.model.BudgetCatalogItem
import com.uchi.mascostos.api.model.BudgetLine
import com.uchi.mascostos.api.model.BudgetOption
import com.uchi.mascostos.api.model.BudgetResult
import com.uchi.mascostos.api.model.ProjectCostItem
import com.uchi.mascostos.api.model.ProjectRef
import com.uchi.mascostos.api.service.BudgetCatalogGateway
import com.uchi.mascostos.api.service.BudgetGateway
import com.uchi.mascostos.api.service.ProjectGateway
import com.uchi.mascostos.api.service.ReportGateway
import com.uchi.mascostos.core.ports.BudgetStructureRepository
import com.uchi.mascostos.core.ports.CostCatalogRepository
import com.uchi.mascostos.core.ports.ProjectItemRepository
import com.uchi.mascostos.core.ports.ProjectRepository
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import java.nio.file.Path

class BudgetService(
    private val projectRepository: ProjectRepository,
    private val projectItemRepository: ProjectItemRepository,
    private val costCatalogRepository: CostCatalogRepository,
    private val budgetStructureRepository: BudgetStructureRepository,
) : ProjectGateway, BudgetGateway, BudgetCatalogGateway, ReportGateway {

    override fun createProject(name: String, location: String?): ProjectRef {
        val created = projectRepository.create(name = name, location = location)
        return ProjectRef(id = created.id, name = created.name)
    }

    override fun listProjects(): List<ProjectRef> {
        return projectRepository.list().map { ProjectRef(id = it.id, name = it.name) }
    }

    override fun addProjectItem(projectId: String, costCode: String, quantity: Double) {
        require(projectRepository.list().any { it.id == projectId }) { "Proyecto no encontrado: $projectId" }
        require(costCode.isNotBlank()) { "El código de costo no puede estar vacío" }
        require(quantity > 0) { "La cantidad debe ser mayor a cero" }
        require(quantity <= 1_000_000) { "La cantidad supera el límite permitido" }
        require(costCatalogRepository.findByCodes(setOf(costCode)).isNotEmpty()) { "Código de costo inválido: $costCode" }
        val catalogItem = budgetStructureRepository.listCatalogByProject(projectId).firstOrNull { it.costCode == costCode }
            ?: error("No se encontró la partida $costCode en la estructura del proyecto.")
        val blockedCodes = System.getenv("MASCOSTOS_BLOCKED_CODES")
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?.toSet()
            ?: emptySet()
        val issues = DomainValidationEngine.validateItemInput(projectId, quantity, catalogItem, blockedCodes)
        require(issues.isEmpty()) { issues.joinToString(" ") { "[${it.code}] ${it.message}" } }
        projectItemRepository.addItem(projectId = projectId, code = costCode, quantity = quantity)
    }

    override fun listProjectItems(projectId: String): List<ProjectCostItem> {
        val projectItems = projectItemRepository.findByProject(projectId)
        val costs = costCatalogRepository.findByCodes(projectItems.map { it.code }.toSet()).associateBy { it.code }
        val titleByCode = budgetStructureRepository
            .listCatalogByProject(projectId)
            .associateBy({ it.costCode }, { it.titleName })

        return projectItems.mapNotNull { row ->
            val cost = costs[row.code] ?: return@mapNotNull null
            ProjectCostItem(
                titleName = titleByCode[row.code] ?: "SIN TITULO",
                costCode = row.code,
                description = cost.description,
                unit = cost.unit,
                quantity = row.quantity,
                unitCost = cost.unitCost,
            )
        }
    }

    override fun listBudgetsForProject(projectId: String): List<BudgetOption> {
        return budgetStructureRepository.listBudgetsByProject(projectId)
    }

    override fun listCatalogForProject(projectId: String, budgetId: String?): List<BudgetCatalogItem> {
        return budgetStructureRepository.listCatalogByProject(projectId, budgetId)
    }

    override fun exportProjectCsv(projectId: String, outputPath: Path): Path {
        val items = listProjectItems(projectId)
        val header = "titulo,codigo,descripcion,unidad,cantidad,costo_unitario,subtotal"
        val rows = items.joinToString("\n") { item ->
            listOf(
                item.titleName,
                item.costCode,
                item.description,
                item.unit,
                item.quantity.toString(),
                item.unitCost.toString(),
                item.subtotal.toString(),
            ).joinToString(",") { value ->
                "\"${value.replace("\"", "''")}\""
            }
        }

        outputPath.toFile().writeText("$header\n$rows")
        return outputPath
    }

    override fun exportProjectPdf(projectId: String, outputPath: Path): Path {
        val items = listProjectItems(projectId)
        val total = items.sumOf { it.subtotal }

        PDDocument().use { document ->
            val page = PDPage(PDRectangle.A4)
            document.addPage(page)

            PDPageContentStream(document, page).use { content ->
                content.beginText()
                content.setFont(PDType1Font.HELVETICA_BOLD, 14f)
                content.newLineAtOffset(48f, 800f)
                content.showText("MASCostos - Reporte de Proyecto $projectId")
                content.endText()

                var y = 775f
                content.setFont(PDType1Font.HELVETICA, 10f)
                items.take(45).forEach { item ->
                    content.beginText()
                    content.newLineAtOffset(48f, y)
                    content.showText("${item.costCode} | ${item.description.take(56)} | qty ${item.quantity} | sub ${"%.2f".format(item.subtotal)}")
                    content.endText()
                    y -= 14f
                }

                content.beginText()
                content.setFont(PDType1Font.HELVETICA_BOLD, 12f)
                content.newLineAtOffset(48f, 80f)
                content.showText("TOTAL: ${"%.2f".format(total)}")
                content.endText()
            }

            outputPath.toFile().parentFile?.mkdirs()
            document.save(outputPath.toFile())
        }
        return outputPath
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
