package com.uchi.mascostos.ui.desktop.ui

import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.control.TreeCell
import javafx.scene.layout.VBox

class BudgetExplorerTreeCell : TreeCell<BudgetExplorerNode>() {

    override fun updateItem(item: BudgetExplorerNode?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null || item is BudgetExplorerNode.Root) {
            text = null
            graphic = null
            return
        }

        when (item) {
            is BudgetExplorerNode.ProyectoNode -> {
                val codigo = Label(item.proyecto.codigo).apply {
                    styleClass.add("explorer-code")
                }
                val nombre = Label(item.proyecto.nombre).apply {
                    styleClass.add("explorer-name")
                }

                graphic = VBox(2.0, codigo, nombre).apply {
                    padding = Insets(4.0, 6.0, 4.0, 2.0)
                }
            }

            is BudgetExplorerNode.SubpresupuestoNode -> {
                val codigo = Label(item.subpresupuesto.codSubpresupuesto ?: "-").apply {
                    styleClass.add("explorer-code")
                }
                val nombre = Label(item.subpresupuesto.nombre).apply {
                    styleClass.add("explorer-sub-name")
                }

                graphic = VBox(2.0, codigo, nombre).apply {
                    padding = Insets(3.0, 6.0, 3.0, 2.0)
                }
            }

            BudgetExplorerNode.Root -> {
                text = null
                graphic = null
            }
        }

        text = null
    }
}