package com.uchi.mascostos.api.model

data class BudgetCatalogItem(
    val titleId: String?,
    val titleName: String,
    val costCode: String,
    val description: String,
    val unit: String,
    val unitCost: Double,
)
