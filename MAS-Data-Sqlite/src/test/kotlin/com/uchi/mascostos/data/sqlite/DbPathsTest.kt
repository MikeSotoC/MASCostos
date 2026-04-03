package com.uchi.mascostos.data.sqlite

import kotlin.test.Test
import kotlin.test.assertEquals

class DbPathsTest {

    @Test
    fun `usa system property para base costos si existe`() {
        val original = System.getProperty("mascostos.db.base")
        try {
            System.setProperty("mascostos.db.base", "/tmp/base-costos.db")

            assertEquals("jdbc:sqlite:/tmp/base-costos.db", DbPaths.BASE_COSTOS)
        } finally {
            restoreProperty("mascostos.db.base", original)
        }
    }

    @Test
    fun `usa system property para proyectos si existe`() {
        val original = System.getProperty("mascostos.db.proyectos")
        try {
            System.setProperty("mascostos.db.proyectos", "/tmp/proyectos.db")

            assertEquals("jdbc:sqlite:/tmp/proyectos.db", DbPaths.PROYECTOS)
        } finally {
            restoreProperty("mascostos.db.proyectos", original)
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
