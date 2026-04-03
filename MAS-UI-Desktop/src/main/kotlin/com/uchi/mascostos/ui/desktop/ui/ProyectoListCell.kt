package com.uchi.mascostos.ui.desktop.ui

import com.uchi.mascostos.app.dto.ProyectoDto
import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.layout.VBox

class ProyectoListCell : ListCell<ProyectoDto>() {

    override fun updateItem(item: ProyectoDto?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            text = null
            graphic = null
            return
        }

        val codigo = Label(item.codigo).apply {
            style = "-fx-font-weight: bold;"
        }

        val nombre = Label(item.nombre)
        val cliente = Label("Cliente: ${item.cliente ?: "-"}")
        val ubicacion = Label("Ubicación: ${item.ubicacion ?: "-"}")
        val estado = Label("Estado: ${item.estado}")

        graphic = VBox(4.0, codigo, nombre, cliente, ubicacion, estado).apply {
            padding = Insets(8.0)
        }
        text = null
    }
}