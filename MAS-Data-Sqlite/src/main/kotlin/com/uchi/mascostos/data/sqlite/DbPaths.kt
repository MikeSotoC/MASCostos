package com.uchi.mascostos.data.sqlite

import java.nio.file.Paths

object DbPaths {

    private const val BASE_COSTOS_ENV = "MAS_BASE_COSTOS_DB"
    private const val PROYECTOS_ENV = "MAS_PROYECTOS_DB"

    private const val BASE_COSTOS_PROP = "mascostos.db.base"
    private const val PROYECTOS_PROP = "mascostos.db.proyectos"

    private const val DEFAULT_DELPHIN_SQLITE = "SQLDelphin_basica.sqlite"

    val BASE_COSTOS: String
        get() = jdbcSqliteUrl(
            configuredPath = System.getenv(BASE_COSTOS_ENV)
                ?: System.getProperty(BASE_COSTOS_PROP),
            defaultFileName = DEFAULT_DELPHIN_SQLITE
        )

    val PROYECTOS: String
        get() = jdbcSqliteUrl(
            configuredPath = System.getenv(PROYECTOS_ENV)
                ?: System.getProperty(PROYECTOS_PROP)
                ?: System.getenv(BASE_COSTOS_ENV)
                ?: System.getProperty(BASE_COSTOS_PROP),
            defaultFileName = DEFAULT_DELPHIN_SQLITE
        )

    private fun jdbcSqliteUrl(configuredPath: String?, defaultFileName: String): String {
        val selectedPath = configuredPath
            ?.takeIf { it.isNotBlank() }
            ?: defaultDbPath(defaultFileName)

        return "jdbc:sqlite:$selectedPath"
    }

    private fun defaultDbPath(defaultFileName: String): String {
        return Paths.get(System.getProperty("user.home"), ".mascostos", defaultFileName).toString()
    }
}
