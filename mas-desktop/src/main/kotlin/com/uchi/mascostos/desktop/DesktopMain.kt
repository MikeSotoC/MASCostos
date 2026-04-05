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
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.TextField
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
        val projectsView = ListView(projects)

        val catalogItems = FXCollections.observableArrayList<BudgetCatalogItem>()
        val catalogView = ListView(catalogItems).apply {
            setCellFactory {
                object : ListCell<BudgetCatalogItem>() {
                    override fun updateItem(item: BudgetCatalogItem?, empty: Boolean) {
                        super.updateItem(item, empty)
                        text = if (empty || item == null) {
                            null
                        } else {
                            "${item.titleName} | ${item.costCode} - ${item.description} (${item.unitCost})"
                        }
                    }
                }
            }
        }

        val nameField = TextField().apply { promptText = "Nombre del proyecto" }
        val locationField = TextField().apply { promptText = "Ubicación" }
        val createButton = Button("Crear proyecto")
        val refreshButton = Button("Refrescar")

        val quantityField = TextField().apply { promptText = "Cantidad" }
        val loadCatalogButton = Button("Cargar estructura")
        val addSelectedButton = Button("Agregar seleccionado")
        val estimateButton = Button("Calcular presupuesto")

        val resultLabel = Label("Total estimado: 0.00")
        val pendingLabel = Label(
            "Falta: árbol jerárquico título/partida, edición de metrado, reportes y plugins firmados"
        )
        val nextActionLabel = Label(
            "Siguiente acción: implementar vista jerárquica de títulos y partidas con selección múltiple"
        )

        fun selectedProjectId(): String? = projectsView.selectionModel.selectedItem?.id

        fun reloadProjects() {
            projects.setAll(services.projectGateway.listProjects())
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
            catalogItems.setAll(services.budgetCatalogGateway.listCatalogForProject(projectId))
        }

        addSelectedButton.setOnAction {
            val projectId = selectedProjectId() ?: return@setOnAction
            val selected = catalogView.selectionModel.selectedItem ?: return@setOnAction
            val quantity = quantityField.text?.toDoubleOrNull() ?: return@setOnAction
            services.projectGateway.addProjectItem(projectId, selected.costCode, quantity)
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
            Label("Estructura de costos por presupuesto del proyecto"),
            structureForm,
            catalogView,
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
        stage.scene = Scene(root, 1080.0, 720.0)
        stage.show()
    }
}

fun main() {
    Application.launch(DesktopMain::class.java)
}
