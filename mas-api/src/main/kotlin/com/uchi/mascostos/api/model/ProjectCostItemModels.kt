package com.uchi.mascostos.api.model

data class ProjectCostItem(
    val titleName: String,
    val costCode: String,
    val description: String,
    val unit: String,
    val quantity: Double,
    val unitCost: Double,
) {
    val subtotal: Double
        get() = quantity * unitCost
}
