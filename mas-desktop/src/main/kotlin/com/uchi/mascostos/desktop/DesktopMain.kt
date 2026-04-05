package com.uchi.mascostos.desktop

import atlantafx.base.theme.PrimerLight
import com.uchi.mascostos.core.Bootstrap
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import java.nio.file.Paths

class DesktopMain : Application() {
    override fun start(stage: Stage) {
        Application.setUserAgentStylesheet(PrimerLight().userAgentStylesheet)

        val engine = Bootstrap.createDefaultEngine(Paths.get("SQLDelphin_basica.sql"))

        val root = BorderPane(
            Label(
                "MASCostos base listo. Proveedores cargados: ${engine.listProviders().size}"
            )
        )

        stage.title = "MASCostos - Base Presupuestos"
        stage.scene = Scene(root, 900.0, 600.0)
        stage.show()
    }
}

fun main() {
    Application.launch(DesktopMain::class.java)
}
