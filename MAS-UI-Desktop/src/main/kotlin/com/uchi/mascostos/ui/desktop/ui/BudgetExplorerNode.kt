package com.uchi.mascostos.ui.desktop.ui

import com.uchi.mascostos.app.dto.ProyectoDto
import com.uchi.mascostos.app.dto.SubpresupuestoDto

sealed class BudgetExplorerNode {

    data object Root : BudgetExplorerNode()

    data class ProyectoNode(
        val proyecto: ProyectoDto
    ) : BudgetExplorerNode()

    data class SubpresupuestoNode(
        val proyecto: ProyectoDto,
        val subpresupuesto: SubpresupuestoDto
    ) : BudgetExplorerNode()
}