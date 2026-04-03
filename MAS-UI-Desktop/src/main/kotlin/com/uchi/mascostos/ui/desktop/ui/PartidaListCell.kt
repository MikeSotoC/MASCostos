package com.uchi.mascostos.ui.desktop.ui

import com.uchi.mascostos.app.dto.ProyectoPartidaDto
import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.layout.VBox

class PartidaListCell : ListCell<ProyectoPartidaDto>() {

    override fun updateItem(item: ProyectoPartidaDto?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            text = null
            graphic = null
            return
        }

        val codigo = Label(item.codPartidaBase ?: "-").apply {
            style = "-fx-font-weight: bold;"
        }

        val descripcion = Label(item.descripcion)
        val unidad = Label("Unidad: ${item.unidad ?: "-"}")
        val precio = Label("P.U.: ${item.precioUnitario}")
        val metrado = Label("Metrado: ${item.metrado}")
        val parcial = Label("Parcial: ${item.parcial}")

        graphic = VBox(4.0, codigo, descripcion, unidad, precio, metrado, parcial).apply {
            padding = Insets(8.0)
        }
        text = null
    }
}