package com.uchi.mascostos.ui.desktop

import atlantafx.base.theme.PrimerLight
import com.uchi.mascostos.app.service.AppApiFactory
import com.uchi.mascostos.data.sqlite.BaseCostosRepositorySqlite
import com.uchi.mascostos.data.sqlite.ProyectoPresupuestoRepositorySqlite
import com.uchi.mascostos.data.sqlite.ProyectoRepositorySqlite
import com.uchi.mascostos.data.sqlite.SQLiteConnector
import com.uchi.mascostos.ui.desktop.ui.MainWindowController
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

class DesktopApp : Application() {

    override fun start(stage: Stage) {
        Application.setUserAgentStylesheet(PrimerLight().userAgentStylesheet)

        val connector = SQLiteConnector()
        val proyectoRepository = ProyectoRepositorySqlite(connector)
        val baseCostosRepository = BaseCostosRepositorySqlite(connector)
        val proyectoPresupuestoRepository = ProyectoPresupuestoRepositorySqlite(connector)

        val appApi = AppApiFactory.create(
            proyectoRepository = proyectoRepository,
            baseCostosRepository = baseCostosRepository,
            proyectoPresupuestoRepository = proyectoPresupuestoRepository
        )

        val loader = FXMLLoader(
            DesktopApp::class.java.getResource("/com/uchi/mascostos/ui/desktop/main-window.fxml")
        )

        val root = loader.load<Parent>()
        val controller = loader.getController<MainWindowController>()
        controller.setAppApi(appApi)

        val scene = Scene(root, 1280.0, 780.0)
        scene.stylesheets.add(
            DesktopApp::class.java.getResource("/com/uchi/mascostos/ui/desktop/app.css")!!.toExternalForm()
        )

        stage.title = "MASCostos"
        stage.minWidth = 1100.0
        stage.minHeight = 720.0
        stage.scene = scene
        stage.show()
    }
}

fun main() {
    Application.launch(DesktopApp::class.java)
}