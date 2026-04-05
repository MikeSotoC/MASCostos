package com.uchi.mascostos.desktop

import atlantafx.base.theme.PrimerLight
import com.uchi.mascostos.api.model.BudgetCatalogItem
import com.uchi.mascostos.api.model.ProjectRef
import com.uchi.mascostos.core.Bootstrap
import javafx.application.Application
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.SelectionMode
import javafx.scene.control.TextField
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage
import java.nio.file.Paths

class DesktopMain : Application() {
    override fun start(stage: Stage) {
        Application.setUserAgentStylesheet(PrimerLight().userAgentStylesheet)

        val services = Bootstrap.bootWithSqlite(
            sqliteFile = Paths.get("mascostos.sqlite"),
            schemaScript = Paths.get("SQLDelphin_basica.sql"),
        )

        val projects = FXCollections.observableArrayList<ProjectRef>()
        val projectsView = javafx.scene.control.ListView(projects)

        val rootNode = TreeItem("Estructura presupuestal").apply { isExpanded = true }
        val structureView = TreeView(rootNode).apply {
            selectionModel.selectionMode = SelectionMode.MULTIPLE
            isShowRoot = true
        }

        val treeCostMap = mutableMapOf<TreeItem<String>, String>()

        val nameField = TextField().apply { promptText = "Nombre del proyecto" }
        val locationField = TextField().apply { promptText = "Ubicación" }
        val createButton = Button("Crear proyecto")
        val refreshButton = Button("Refrescar")

        val quantityField = TextField().apply { promptText = "Cantidad" }
        val loadCatalogButton = Button("Cargar estructura")
        val addSelectedButton = Button("Agregar selección")
        val estimateButton = Button("Calcular presupuesto")

        val resultLabel = Label("Total estimado: 0.00")
        val pendingLabel = Label(
            "Falta: metrado por partida, selector de presupuesto, reportes y plugins firmados"
        )
        val nextActionLabel = Label(
            "Siguiente acción: edición de metrado y subtotal incremental por nodo"
        )

        fun selectedProjectId(): String? = projectsView.selectionModel.selectedItem?.id

        fun reloadProjects() {
            projects.setAll(services.projectGateway.listProjects())
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
            val items = services.budgetCatalogGateway.listCatalogForProject(projectId)
            buildStructureTree(items)
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
        }

        estimateButton.setOnAction {
            val projectId = selectedProjectId() ?: return@setOnAction
            val result = services.budgetGateway.estimate(projectId)
            resultLabel.text = "Total estimado: %.2f (%d items)".format(result.total, result.lines.size)
        }

        refreshButton.setOnAction { reloadProjects() }
        reloadProjects()

        val projectForm = HBox(8.0, nameField, locationField, createButton, refreshButton).apply {
            padding = Insets(12.0)
        }

        val structureForm = HBox(8.0, loadCatalogButton, quantityField, addSelectedButton, estimateButton).apply {
            padding = Insets(12.0)
        }

        val content = VBox(
            10.0,
            Label("Proyectos"),
            projectsView,
            Label("Estructura jerárquica de costos por presupuesto del proyecto"),
            structureForm,
            structureView,
            resultLabel,
            pendingLabel,
            nextActionLabel,
        ).apply {
            padding = Insets(12.0)
        }

        val root = BorderPane().apply {
            top = projectForm
            center = content
        }

        stage.title = "MASCostos - Base Presupuestos"
        stage.scene = Scene(root, 1120.0, 760.0)
        stage.show()
    }
}

fun main() {
    Application.launch(DesktopMain::class.java)
}
