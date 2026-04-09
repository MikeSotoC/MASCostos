package com.uchi.mascostos.core.service

import com.uchi.mascostos.api.model.BudgetCatalogItem

data class ValidationIssue(val code: String, val message: String)

object DomainValidationEngine {
    fun validateItemInput(
        projectId: String,
        quantity: Double,
        catalogItem: BudgetCatalogItem,
        blockedCodes: Set<String>,
    ): List<ValidationIssue> {
        val issues = mutableListOf<ValidationIssue>()

        if (projectId.isBlank()) issues += ValidationIssue("PROJECT_INVALID", "Proyecto inválido o vacío.")
        if (quantity <= 0) issues += ValidationIssue("QTY_INVALID", "La cantidad debe ser mayor que cero.")
        if (catalogItem.costCode in blockedCodes) {
            issues += ValidationIssue("COST_BLOCKED", "La partida ${catalogItem.costCode} está bloqueada para este entorno.")
        }
        if (catalogItem.titleName.contains("Concreto", ignoreCase = true) && quantity > 50_000) {
            issues += ValidationIssue("QTY_TOO_HIGH", "La cantidad para rubro concreto supera el umbral permitido (50,000).")
        }
        if (catalogItem.description.contains("Excavación", ignoreCase = true) && quantity % 1.0 != 0.0) {
            issues += ValidationIssue("QTY_INTEGER_REQUIRED", "Excavación requiere cantidad entera para control de metrado.")
        }

        return issues
    }
}
