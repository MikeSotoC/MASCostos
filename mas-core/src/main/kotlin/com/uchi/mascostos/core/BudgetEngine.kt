package com.uchi.mascostos.core

import com.uchi.mascostos.api.BudgetProvider
import com.uchi.mascostos.api.PluginContext
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager

class BudgetEngine(
    private val databasePath: Path,
) : PluginContext {

    private val providers = mutableMapOf<String, BudgetProvider>()

    override fun registerBudgetProvider(provider: BudgetProvider) {
        providers[provider.id] = provider
    }

    fun listProviders(): List<BudgetProvider> = providers.values.toList()

    fun openConnection(): Connection {
        val jdbcUrl = "jdbc:sqlite:${databasePath.toAbsolutePath()}"
        return DriverManager.getConnection(jdbcUrl)
    }
}
