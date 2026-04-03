package com.uchi.mascostos.ui.desktop.ui

import com.uchi.mascostos.app.dto.SubpresupuestoDto
import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.layout.VBox

class SubpresupuestoListCell : ListCell<SubpresupuestoDto>() {

    override fun updateItem(item: SubpresupuestoDto?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            text = null
            graphic = null
            return
        }

        val codigo = Label(item.codSubpresupuesto ?: "-").apply {
            style = "-fx-font-weight: bold;"
        }

        val nombre = Label(item.nombre)
        val orden = Label("Orden: ${item.orden}")

        graphic = VBox(4.0, codigo, nombre, orden).apply {
            padding = Insets(8.0)
        }
        text = null
    }
}