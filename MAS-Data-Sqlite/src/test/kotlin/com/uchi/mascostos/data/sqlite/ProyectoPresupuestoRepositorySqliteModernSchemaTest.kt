package com.uchi.mascostos.data.sqlite

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProyectoPresupuestoRepositorySqliteModernSchemaTest {

    @Test
    fun `lista y actualiza partidas sobre esquema SQLDelphin moderno`() {
        val dbPath = Files.createTempFile("sql-delphin-proy", ".sqlite")
        val original = System.getProperty("mascostos.db.proyectos")

        try {
            System.setProperty("mascostos.db.proyectos", dbPath.toString())

            val connector = SQLiteConnector()
            connector.openProyectos().use { cn ->
                cn.createStatement().use { st ->
                    st.execute("CREATE TABLE presupuesto (id_presupuesto CHAR(12), id_proyecto CHAR(12), nombre_presupuesto VARCHAR(255))")
                    st.execute("CREATE TABLE titulo (id_titulo CHAR(12), id_titulopadre CHAR(12), posicion_titulo INTEGER, id_presupuesto CHAR(12), descripcion_titulo VARCHAR(255))")
                    st.execute("CREATE TABLE unidad (id_unidad CHAR(12), descripcion_unidad VARCHAR(120), abreviatura_unidad VARCHAR(30))")
                    st.execute("CREATE TABLE costo_unitario (id_costounitario CHAR(12), descripcion_costo VARCHAR(255), id_unidad CHAR(12), costo_unitario NUMERIC(16,2), id_titulo CHAR(12), posicion_costo INTEGER, cantidad NUMERIC(16,2), parcial_costo NUMERIC(16,2))")

                    st.execute("INSERT INTO presupuesto(id_presupuesto, id_proyecto, nombre_presupuesto) VALUES ('P1', '1', 'Presupuesto 1')")
                    st.execute("INSERT INTO titulo(id_titulo, id_titulopadre, posicion_titulo, id_presupuesto, descripcion_titulo) VALUES ('TROOT', NULL, 1, 'P1', 'Capitulo')")
                    st.execute("INSERT INTO titulo(id_titulo, id_titulopadre, posicion_titulo, id_presupuesto, descripcion_titulo) VALUES ('TSUB', 'TROOT', 1, 'P1', 'Sub')")
                    st.execute("INSERT INTO unidad(id_unidad, descripcion_unidad, abreviatura_unidad) VALUES ('U1', 'Metro', 'm')")
                    st.execute("INSERT INTO costo_unitario(id_costounitario, descripcion_costo, id_unidad, costo_unitario, id_titulo, posicion_costo, cantidad, parcial_costo) VALUES ('CU1', 'Partida 1', 'U1', 10, 'TROOT', 1, 2, 20)")
                }
            }

            val repo = ProyectoPresupuestoRepositorySqlite(connector)
            val subs = repo.listarSubpresupuestosProyecto(1)
            assertEquals(1, subs.size)

            val partidas = repo.listarPartidasProyecto(1, subs.first().id)
            assertEquals(1, partidas.size)
            assertEquals("CU1", partidas.first().codPartidaBase)
            assertEquals("m", partidas.first().unidad)

            repo.actualizarProyectoPartidaPrecioUnitario(partidas.first().id, 15.0)
            repo.actualizarProyectoPartidaMetrado(partidas.first().id, 3.0, 45.0)

            connector.openProyectos().use { cn ->
                cn.createStatement().use { st ->
                    st.executeQuery("SELECT costo_unitario, cantidad, parcial_costo FROM costo_unitario WHERE id_costounitario = 'CU1'").use { rs ->
                        assertTrue(rs.next())
                        assertEquals(15.0, rs.getDouble("costo_unitario"))
                        assertEquals(3.0, rs.getDouble("cantidad"))
                        assertEquals(45.0, rs.getDouble("parcial_costo"))
                    }
                }
            }
        } finally {
            if (original == null) System.clearProperty("mascostos.db.proyectos")
            else System.setProperty("mascostos.db.proyectos", original)
        }
    }
}
