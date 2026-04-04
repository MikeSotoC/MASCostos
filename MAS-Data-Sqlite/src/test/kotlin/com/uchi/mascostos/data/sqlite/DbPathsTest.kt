package com.uchi.mascostos.data.sqlite

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DbPathsTest {

    @Test
    fun `usa system property para base costos si existe`() {
        val original = System.getProperty("mascostos.db.base")
        try {
            System.setProperty("mascostos.db.base", "/tmp/SQLDelphin_basica.sqlite")

            assertEquals("jdbc:sqlite:/tmp/SQLDelphin_basica.sqlite", DbPaths.BASE_COSTOS)
        } finally {
            restoreProperty("mascostos.db.base", original)
        }
    }

    @Test
    fun `usa system property para proyectos si existe`() {
        val original = System.getProperty("mascostos.db.proyectos")
        try {
            System.setProperty("mascostos.db.proyectos", "/tmp/SQLDelphin_basica.sqlite")

            assertEquals("jdbc:sqlite:/tmp/SQLDelphin_basica.sqlite", DbPaths.PROYECTOS)
        } finally {
            restoreProperty("mascostos.db.proyectos", original)
        }
    }

    @Test
    fun `proyectos hereda ruta base cuando no se define property propia`() {
        val originalBase = System.getProperty("mascostos.db.base")
        val originalProy = System.getProperty("mascostos.db.proyectos")
        try {
            System.setProperty("mascostos.db.base", "/tmp/SQLDelphin_basica.sqlite")
            System.clearProperty("mascostos.db.proyectos")

            assertEquals("jdbc:sqlite:/tmp/SQLDelphin_basica.sqlite", DbPaths.PROYECTOS)
        } finally {
            restoreProperty("mascostos.db.base", originalBase)
            restoreProperty("mascostos.db.proyectos", originalProy)
        }
    }

    @Test
    fun `por defecto usa SQLDelphin_basica sqlite`() {
        val originalBase = System.getProperty("mascostos.db.base")
        val originalProy = System.getProperty("mascostos.db.proyectos")
        try {
            System.clearProperty("mascostos.db.base")
            System.clearProperty("mascostos.db.proyectos")

            assertTrue(DbPaths.BASE_COSTOS.contains("SQLDelphin_basica.sqlite"))
            assertTrue(DbPaths.PROYECTOS.contains("SQLDelphin_basica.sqlite"))
        } finally {
            restoreProperty("mascostos.db.base", originalBase)
            restoreProperty("mascostos.db.proyectos", originalProy)
        }
    }

    private fun restoreProperty(key: String, value: String?) {
        if (value == null) {
            System.clearProperty(key)
        } else {
            System.setProperty(key, value)
        }
    }
}
