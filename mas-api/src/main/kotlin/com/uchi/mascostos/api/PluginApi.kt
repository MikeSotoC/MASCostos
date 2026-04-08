package com.uchi.mascostos.api

/**
 * API pública para extensiones de terceros.
 */
interface PluginApi {
    val id: String
    val displayName: String
    val version: String get() = "0.1.0"
    val vendor: String get() = "unknown"
    val signature: String? get() = null
    val permissions: Set<PluginPermission> get() = setOf(PluginPermission.READ_CATALOG)

    fun initialize(context: PluginContext)
}

interface PluginContext {
    fun registerBudgetProvider(provider: BudgetProvider)
}

interface BudgetProvider {
    val id: String
    val name: String

    fun describe(): String
}

enum class PluginPermission {
    READ_CATALOG,
    WRITE_PROJECT_ITEMS,
    EXPORT_REPORTS,
}
