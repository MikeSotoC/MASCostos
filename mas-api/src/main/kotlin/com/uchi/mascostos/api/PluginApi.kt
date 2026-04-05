package com.uchi.mascostos.api

/**
 * API pública para extensiones de terceros.
 */
interface PluginApi {
    val id: String
    val displayName: String

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
