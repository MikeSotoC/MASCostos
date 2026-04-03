package com.uchi.mascostos.ui.desktop.ui

import com.uchi.mascostos.app.dto.ProyectoPartidaDetalleDto
import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.layout.VBox

class DetallePartidaListCell : ListCell<ProyectoPartidaDetalleDto>() {

    override fun updateItem(item: ProyectoPartidaDetalleDto?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            text = null
            graphic = null
            return
        }

        val codigo = Label(item.codInsumoBase ?: "-").apply {
            style = "-fx-font-weight: bold;"
        }

        val descripcion = Label(item.descripcion)
        val unidad = Label("Unidad: ${item.unidad ?: "-"}")
        val cantidad = Label("Cantidad: ${item.cantidad}")
        val precio = Label("P.U.: ${item.precioUnitario}")
        val parcial = Label("Parcial: ${item.parcial}")

        graphic = VBox(4.0, codigo, descripcion, unidad, cantidad, precio, parcial).apply {
            padding = Insets(8.0)
        }
        text = null
    }
}