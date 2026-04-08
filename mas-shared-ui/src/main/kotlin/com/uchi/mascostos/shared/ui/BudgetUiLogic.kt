package com.uchi.mascostos.shared.ui

data class UiCostItem(
    val title: String,
    val code: String,
    val quantity: Double,
    val unitCost: Double,
)

object BudgetUiLogic {
    fun subtotalsByTitle(items: List<UiCostItem>): Map<String, Double> {
        return items.groupBy { it.title }
            .mapValues { (_, rows) -> rows.sumOf { it.quantity * it.unitCost } }
            .toSortedMap()
    }

    fun total(items: List<UiCostItem>): Double {
        return items.sumOf { it.quantity * it.unitCost }
    }

    fun reportFileName(projectId: String, stamp: String): String {
        return "report-$projectId-$stamp.csv"
    }
}
