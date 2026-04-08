package com.uchi.mascostos.desktop

import atlantafx.base.theme.PrimerLight
import com.uchi.mascostos.api.model.BudgetCatalogItem
import com.uchi.mascostos.api.model.BudgetOption
import com.uchi.mascostos.api.model.ProjectCostItem
import com.uchi.mascostos.api.model.ProjectRef
import com.uchi.mascostos.core.Bootstrap
import javafx.application.Application
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.SelectionMode
import javafx.scene.control.Separator
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Stage
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.uchi.mascostos.shared.ui.BudgetUiLogic
import com.uchi.mascostos.shared.ui.MasDesignSystem
import com.uchi.mascostos.shared.ui.UiCostItem

class DesktopMain : Application() {
    override fun start(stage: Stage) {
        Application.setUserAgentStylesheet(PrimerLight().userAgentStylesheet)

        val services = Bootstrap.bootWithSqlite(
            sqliteFile = Paths.get("mascostos.sqlite"),
            schemaScript = Paths.get("SQLDelphin_basica.sql"),
        )

        val projects = FXCollections.observableArrayList<ProjectRef>()
        val projectsView = javafx.scene.control.ListView(projects)

        val budgets = FXCollections.observableArrayList<BudgetOption>()
        val budgetSelector = ComboBox(budgets).apply { promptText = "Presupuesto activo" }

        val rootNode = TreeItem("Estructura presupuestal").apply { isExpanded = true }
        val structureView = TreeView(rootNode).apply {
            selectionModel.selectionMode = SelectionMode.MULTIPLE
            isShowRoot = true
        }

        val projectItems = FXCollections.observableArrayList<ProjectCostItem>()
        val projectItemsView = javafx.scene.control.ListView(projectItems).apply {
            setCellFactory {
                object : ListCell<ProjectCostItem>() {
                    override fun updateItem(item: ProjectCostItem?, empty: Boolean) {
                        super.updateItem(item, empty)
                        text = if (empty || item == null) {
                            null
                        } else {
                            "${item.titleName} | ${item.costCode} - ${item.description} | ${item.quantity} x ${item.unitCost} = ${"%.2f".format(item.subtotal)}"
                        }
                    }
                }
            }
        }

        val treeCostMap = mutableMapOf<TreeItem<String>, String>()

        val nameField = TextField().apply { promptText = "Nombre del proyecto" }
        val locationField = TextField().apply { promptText = "Ubicación" }
        val createButton = Button("Crear proyecto")
        val refreshButton = Button("Refrescar")

        val quantityField = TextField().apply { promptText = "Cantidad para agregar" }
        val editQuantityField = TextField().apply { promptText = "Nueva cantidad ítem seleccionado" }
        val loadCatalogButton = Button("Cargar estructura")
        val addSelectedButton = Button("Agregar selección")
        val updateSelectedItemButton = Button("Actualizar ítem")
        val estimateButton = Button("Calcular presupuesto")
        val exportCsvButton = Button("Exportar CSV")
        val exportPdfButton = Button("Exportar PDF")

        val resultLabel = Label("Total estimado: 0.00")
        val subtotalByTitleArea = TextArea().apply {
            isEditable = false
            promptText = "Subtotales por título"
            prefRowCount = 5
        }
        val parityLabel = Label("Paridad UI: ${MasDesignSystem.StatusText.parityAligned}")
        val pendingLabel = Label("Falta: ${MasDesignSystem.StatusText.pendingRoadmap}")
        val nextActionLabel = Label("Siguiente: unificar sistema de diseño entre plataformas")
        val exportStatusLabel = Label("Reporte: pendiente")

        fun selectedProjectId(): String? = projectsView.selectionModel.selectedItem?.id

        fun selectedBudgetId(): String? = budgetSelector.selectionModel.selectedItem?.id

        fun refreshProjectItems(projectId: String) {
            val items = services.projectGateway.listProjectItems(projectId)
            projectItems.setAll(items)

            val uiItems = items.map { UiCostItem(it.titleName, it.costCode, it.quantity, it.unitCost) }
            val subtotals = BudgetUiLogic.subtotalsByTitle(uiItems)
            subtotalByTitleArea.text = subtotals.entries.joinToString("\n") { (title, subtotal) ->
                "$title: ${"%.2f".format(subtotal)}"
            }
        }

        fun reloadProjects() {
            projects.setAll(services.projectGateway.listProjects())
        }

        fun reloadBudgets(projectId: String) {
            val list = services.budgetCatalogGateway.listBudgetsForProject(projectId)
            budgets.setAll(list)
            if (list.isNotEmpty()) {
                budgetSelector.selectionModel.selectFirst()
            }
        }

        fun buildStructureTree(items: List<BudgetCatalogItem>) {
            treeCostMap.clear()
            rootNode.children.clear()

            val grouped = items.groupBy { "${it.titleName} (${it.titleId ?: "N/A"})" }
            grouped.toSortedMap().forEach { (title, titleItems) ->
                val titleNode = TreeItem(title).apply { isExpanded = true }
                titleItems.forEach { item ->
                    val text = "${item.costCode} - ${item.description} [${item.unit}] S/ ${"%.2f".format(item.unitCost)}"
                    val leaf = TreeItem(text)
                    treeCostMap[leaf] = item.costCode
                    titleNode.children += leaf
                }
                rootNode.children += titleNode
            }
        }

        createButton.setOnAction {
            if (nameField.text.isNullOrBlank()) return@setOnAction
            services.projectGateway.createProject(
                name = nameField.text.trim(),
                location = locationField.text?.trim()?.ifBlank { null },
            )
            nameField.clear()
            locationField.clear()
            reloadProjects()
        }

        loadCatalogButton.setOnAction {
            val projectId = selectedProjectId() ?: return@setOnAction
            val budgetId = selectedBudgetId()
            val items = services.budgetCatalogGateway.listCatalogForProject(projectId, budgetId)
            buildStructureTree(items)
            refreshProjectItems(projectId)
        }

        addSelectedButton.setOnAction {
            val projectId = selectedProjectId() ?: return@setOnAction
            val quantity = quantityField.text?.toDoubleOrNull() ?: return@setOnAction

            val selectedLeaves = structureView.selectionModel.selectedItems
                .mapNotNull { treeCostMap[it] }
                .distinct()

            selectedLeaves.forEach { code ->
                services.projectGateway.addProjectItem(projectId, code, quantity)
            }

            quantityField.clear()
            refreshProjectItems(projectId)
        }

        updateSelectedItemButton.setOnAction {
            val projectId = selectedProjectId() ?: return@setOnAction
            val selected = projectItemsView.selectionModel.selectedItem ?: return@setOnAction
            val newQty = editQuantityField.text?.toDoubleOrNull() ?: return@setOnAction
            services.projectGateway.addProjectItem(projectId, selected.costCode, newQty)
            editQuantityField.clear()
            refreshProjectItems(projectId)
        }

        exportCsvButton.setOnAction {
            val projectId = selectedProjectId() ?: return@setOnAction
            val stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
            val out = Paths.get(BudgetUiLogic.reportFileName(projectId, stamp))
            services.reportGateway.exportProjectCsv(projectId, out)
            exportStatusLabel.text = "Reporte CSV generado: ${out.toAbsolutePath()}"
        }

        exportPdfButton.setOnAction {
            val projectId = selectedProjectId() ?: return@setOnAction
            val out = Paths.get("report-$projectId-desktop.pdf")
            services.reportGateway.exportProjectPdf(projectId, out)
            exportStatusLabel.text = "Reporte PDF generado: ${out.toAbsolutePath()}"
        }

        estimateButton.setOnAction {
            val projectId = selectedProjectId() ?: return@setOnAction
            val result = services.budgetGateway.estimate(projectId)
            resultLabel.text = "Total estimado: %.2f (%d items)".format(result.total, result.lines.size)
            refreshProjectItems(projectId)
        }

        refreshButton.setOnAction { reloadProjects() }

        projectsView.selectionModel.selectedItemProperty().addListener { _, _, newProject ->
            if (newProject != null) {
                reloadBudgets(newProject.id)
                refreshProjectItems(newProject.id)
            }
        }

        budgetSelector.selectionModel.selectedItemProperty().addListener { _, _, _ ->
            val projectId = selectedProjectId() ?: return@addListener
            val items = services.budgetCatalogGateway.listCatalogForProject(projectId, selectedBudgetId())
            buildStructureTree(items)
        }

        reloadProjects()

        val projectForm = HBox(8.0, nameField, locationField, createButton, refreshButton).apply {
            padding = Insets(12.0)
        }

        val budgetForm = HBox(8.0, Label("Presupuesto:"), budgetSelector, loadCatalogButton).apply {
            padding = Insets(12.0)
        }

        val structureForm = HBox(8.0, quantityField, addSelectedButton, estimateButton, exportCsvButton, exportPdfButton).apply {
            padding = Insets(12.0)
        }

        val editForm = HBox(8.0, editQuantityField, updateSelectedItemButton).apply {
            padding = Insets(12.0)
        }

        val leftPanel = VBox(
            MasDesignSystem.Spacing.section,
            Label("Proyectos"),
            projectsView,
            budgetForm,
            Label("Catálogo / Estructura"),
            structureForm,
            structureView,
        ).apply {
            padding = Insets(12.0)
            prefWidth = 650.0
            style = "-fx-background-color: -color-bg-default; -fx-background-radius: 12; -fx-border-radius: 12;"
        }

        val rightPanel = VBox(
            MasDesignSystem.Spacing.section,
            Label("Ítems del proyecto"),
            editForm,
            projectItemsView,
            Separator(),
            Label("Subtotales por título"),
            subtotalByTitleArea,
            resultLabel,
            parityLabel,
            pendingLabel,
            nextActionLabel,
            exportStatusLabel,
        ).apply {
            padding = Insets(12.0)
            prefWidth = 500.0
            style = "-fx-background-color: -color-bg-default; -fx-background-radius: 12; -fx-border-radius: 12;"
        }

        HBox.setHgrow(leftPanel, Priority.ALWAYS)
        HBox.setHgrow(rightPanel, Priority.ALWAYS)
        val content = HBox(MasDesignSystem.Spacing.card, leftPanel, rightPanel).apply { padding = Insets(12.0) }

        val root = BorderPane().apply {
            top = projectForm
            center = content
        }

        stage.title = "MASCostos - Base Presupuestos"
        stage.scene = Scene(root, 1180.0, 900.0)
        stage.show()
    }
}

fun main() {
    Application.launch(DesktopMain::class.java)
}
