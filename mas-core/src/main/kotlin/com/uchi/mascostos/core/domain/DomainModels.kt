package com.uchi.mascostos.core.domain

data class Project(
    val id: String,
    val name: String,
    val location: String?,
)

data class CostItem(
    val code: String,
    val description: String,
    val unit: String,
    val unitCost: Double,
)

data class ProjectItem(
    val projectId: String,
    val code: String,
    val quantity: Double,
)
