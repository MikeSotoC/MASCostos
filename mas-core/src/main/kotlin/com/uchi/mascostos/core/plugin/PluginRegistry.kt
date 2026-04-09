package com.uchi.mascostos.core.plugin

import com.uchi.mascostos.api.PluginApi
import com.uchi.mascostos.api.PluginContext
import java.util.ServiceLoader

class PluginRegistry(private val context: PluginContext) {
    fun loadFromClasspath(): List<PluginApi> {
        val plugins = ServiceLoader.load(PluginApi::class.java).toList()
        validate(plugins)
        plugins.forEach { it.initialize(context) }
        return plugins
    }

    private fun validate(plugins: List<PluginApi>) {
        val ids = mutableSetOf<String>()
        plugins.forEach { plugin ->
            require(plugin.id.isNotBlank()) { "Plugin inválido: id vacío" }
            require(ids.add(plugin.id)) { "Plugin duplicado: ${plugin.id}" }
            require(plugin.version.matches(Regex("""\d+\.\d+\.\d+(-[A-Za-z0-9.]+)?"""))) {
                "Plugin ${plugin.id} con versión inválida: ${plugin.version}"
            }
            require(!plugin.signature.isNullOrBlank()) {
                "Plugin ${plugin.id} sin firma. Requerido para plugins formales."
            }
        }
    }
}
