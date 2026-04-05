package com.uchi.mascostos.desktop

import atlantafx.base.theme.PrimerLight
import com.uchi.mascostos.api.model.ProjectRef
import com.uchi.mascostos.core.Bootstrap
import javafx.application.Application
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
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
        val nameField = TextField().apply { promptText = "Nombre del proyecto" }
        val locationField = TextField().apply { promptText = "Ubicación" }

        val createButton = Button("Crear proyecto")
        val refreshButton = Button("Refrescar")

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

        refreshButton.setOnAction { reloadProjects() }
        reloadProjects()

        val form = HBox(8.0, nameField, locationField, createButton, refreshButton).apply {
            padding = Insets(12.0)
        }

        val content = VBox(
            10.0,
            Label("Proyectos"),
            projectsView,
        ).apply {
            padding = Insets(12.0)
        }

        val root = BorderPane().apply {
            top = form
            center = content
        }

        stage.title = "MASCostos - Base Presupuestos"
        stage.scene = Scene(root, 980.0, 640.0)
        stage.show()
    }
}

fun main() {
    Application.launch(DesktopMain::class.java)
}
