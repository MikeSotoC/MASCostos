package com.uchi.mascostos.core.plugin

import com.uchi.mascostos.api.PluginApi
import com.uchi.mascostos.api.PluginContext
import java.util.ServiceLoader

class PluginRegistry(private val context: PluginContext) {
    fun loadFromClasspath(): List<PluginApi> {
        val plugins = ServiceLoader.load(PluginApi::class.java).toList()
        plugins.forEach { it.initialize(context) }
        return plugins
    }
}
