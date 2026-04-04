package com.uchi.mascostos.data.sqlite

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

class BaseCostosRepositorySqliteModernSchemaTest {

    @Test
    fun `usa tablas SQLDelphin para subpresupuestos y partidas`() {
        val dbPath = Files.createTempFile("sql-delphin-modern", ".sqlite")
        val originalBase = System.getProperty("mascostos.db.base")

        try {
            System.setProperty("mascostos.db.base", dbPath.toString())

            val connector = SQLiteConnector()
            connector.openBaseCostos().use { cn ->
                cn.createStatement().use { st ->
                    st.execute("CREATE TABLE presupuesto (id_presupuesto CHAR(12), nombre_presupuesto VARCHAR(255))")
                    st.execute(
                        "CREATE TABLE titulo (id_titulo CHAR(12), id_titulopadre CHAR(12), posicion_titulo INTEGER, id_presupuesto CHAR(12), descripcion_titulo VARCHAR(255))"
                    )
                    st.execute(
                        "CREATE TABLE costo_unitario (id_costounitario CHAR(12), descripcion_costo VARCHAR(255), id_unidad CHAR(12), costo_unitario NUMERIC(16,2), id_titulo CHAR(12), posicion_costo INTEGER)"
                    )
                    st.execute("CREATE TABLE unidad (id_unidad CHAR(12), descripcion_unidad VARCHAR(120), abreviatura_unidad VARCHAR(30))")

                    st.execute("INSERT INTO presupuesto(id_presupuesto, nombre_presupuesto) VALUES ('P01', 'Presupuesto Demo')")
                    st.execute("INSERT INTO titulo(id_titulo, id_titulopadre, posicion_titulo, id_presupuesto, descripcion_titulo) VALUES ('TROOT', NULL, 1, 'P01', 'Capitulo 1')")
                    st.execute("INSERT INTO titulo(id_titulo, id_titulopadre, posicion_titulo, id_presupuesto, descripcion_titulo) VALUES ('TSUB', 'TROOT', 1, 'P01', 'Subtitulo')")
                    st.execute("INSERT INTO unidad(id_unidad, descripcion_unidad, abreviatura_unidad) VALUES ('U01', 'Metro', 'm')")
                    st.execute("INSERT INTO costo_unitario(id_costounitario, descripcion_costo, id_unidad, costo_unitario, id_titulo, posicion_costo) VALUES ('CU01', 'Partida CU', 'U01', 12.5, 'TSUB', 1)")
                }
            }

            val repo = BaseCostosRepositorySqlite(connector)
            val subs = repo.listarSubpresupuestos("P01")
            assertEquals(1, subs.size)
            assertEquals("TROOT", subs.first().codSubpresupuesto)

            val partidas = repo.listarPartidasPresupuesto("P01", "TROOT")
            assertEquals(1, partidas.size)
            assertEquals("CU01", partidas.first().codPartida)
            assertEquals("m", partidas.first().unidad)
        } finally {
            if (originalBase == null) {
                System.clearProperty("mascostos.db.base")
            } else {
                System.setProperty("mascostos.db.base", originalBase)
            }
        }
    }
}
