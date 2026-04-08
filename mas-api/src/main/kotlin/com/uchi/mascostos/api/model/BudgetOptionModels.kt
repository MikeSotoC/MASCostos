package com.uchi.mascostos.api.model

data class BudgetOption(
    val id: String,
    val name: String,
) {
    override fun toString(): String = "$name ($id)"
}
