package com.uchi.mascostos.api.model

data class ProjectRef(
    val id: String,
    val name: String,
)

data class BudgetLine(
    val code: String,
    val description: String,
    val unit: String,
    val quantity: Double,
    val unitCost: Double,
) {
    val partial: Double
        get() = quantity * unitCost
}

data class BudgetResult(
    val project: ProjectRef,
    val lines: List<BudgetLine>,
) {
    val total: Double
        get() = lines.sumOf { it.partial }
}
