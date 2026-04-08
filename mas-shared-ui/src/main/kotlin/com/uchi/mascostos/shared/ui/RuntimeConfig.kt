package com.uchi.mascostos.shared.ui

import java.nio.file.Path
import java.nio.file.Paths
import java.util.Properties

enum class RuntimeMode { LOCAL, REMOTE }

data class DesktopRuntimeConfig(
    val mode: RuntimeMode = RuntimeMode.LOCAL,
    val sqlitePath: Path = Paths.get("mascostos.sqlite"),
    val schemaPath: Path = Paths.get("SQLDelphin_basica.sql"),
    val remoteBaseUrl: String = "http://localhost:8080",
)

object RuntimeConfigLoader {
    fun loadDesktopConfig(): DesktopRuntimeConfig {
        val props = Properties()
        val file = Paths.get("mascostos.properties").toFile()
        if (file.exists()) file.inputStream().use { props.load(it) }

        val mode = (System.getenv("MASCOSTOS_MODE")
            ?: props.getProperty("mode")
            ?: "local").trim().uppercase()
        val sqlite = System.getenv("MASCOSTOS_SQLITE")
            ?: props.getProperty("sqlite.path")
            ?: "mascostos.sqlite"
        val schema = System.getenv("MASCOSTOS_SCHEMA")
            ?: props.getProperty("schema.path")
            ?: "SQLDelphin_basica.sql"
        val remote = System.getenv("MASCOSTOS_REMOTE_BASE_URL")
            ?: props.getProperty("remote.baseUrl")
            ?: "http://localhost:8080"

        return DesktopRuntimeConfig(
            mode = if (mode == "REMOTE") RuntimeMode.REMOTE else RuntimeMode.LOCAL,
            sqlitePath = Paths.get(sqlite),
            schemaPath = Paths.get(schema),
            remoteBaseUrl = remote,
        )
    }
}
