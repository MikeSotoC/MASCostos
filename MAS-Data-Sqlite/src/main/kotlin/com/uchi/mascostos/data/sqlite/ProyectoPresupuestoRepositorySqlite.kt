package com.uchi.mascostos.data.sqlite

import com.uchi.mascostos.core.model.ProyectoPartida
import com.uchi.mascostos.core.model.ProyectoPartidaDetalle
import com.uchi.mascostos.core.model.ProyectoSubpresupuesto
import com.uchi.mascostos.core.ports.ProyectoPresupuestoRepository
import java.sql.Connection
import java.sql.ResultSet
import kotlin.math.abs

class ProyectoPresupuestoRepositorySqlite(
    private val connector: SQLiteConnector
) : ProyectoPresupuestoRepository {

    override fun listarSubpresupuestosProyecto(proyectoId: Long): List<ProyectoSubpresupuesto> {
        connector.openProyectos().use { cn ->
            if (canUseModernSchema(cn)) {
                val sql = """
                    SELECT t.id_titulo, t.descripcion_titulo, t.posicion_titulo
                    FROM titulo t
                    JOIN presupuesto p ON p.id_presupuesto = t.id_presupuesto
                    WHERE p.id_proyecto = ?
                      AND (t.id_titulopadre IS NULL OR TRIM(t.id_titulopadre) = '')
                    ORDER BY t.posicion_titulo, t.id_titulo
                """.trimIndent()

                cn.prepareStatement(sql).use { ps ->
                    ps.setString(1, proyectoId.toString())
                    ps.executeQuery().use { rs ->
                        val items = mutableListOf<ProyectoSubpresupuesto>()
                        while (rs.next()) {
                            val tituloId = rs.getString("id_titulo")
                            items += ProyectoSubpresupuesto(
                                id = stableId(tituloId),
                                proyectoId = proyectoId,
                                codSubpresupuesto = tituloId,
                                nombre = rs.getString("descripcion_titulo"),
                                orden = rs.getInt("posicion_titulo"),
                                activo = true
                            )
                        }
                        return items
                    }
                }
            }

            val sql = """
                SELECT id, proyecto_id, cod_subpresupuesto, nombre, orden, activo
                FROM proyecto_subpresupuestos
                WHERE proyecto_id = ?
                ORDER BY orden, id
            """.trimIndent()

            cn.prepareStatement(sql).use { ps ->
                ps.setLong(1, proyectoId)
                ps.executeQuery().use { rs ->
                    val items = mutableListOf<ProyectoSubpresupuesto>()
                    while (rs.next()) {
                        items += ProyectoSubpresupuesto(
                            id = rs.getLong("id"),
                            proyectoId = rs.getLong("proyecto_id"),
                            codSubpresupuesto = rs.getString("cod_subpresupuesto"),
                            nombre = rs.getString("nombre"),
                            orden = rs.getInt("orden"),
                            activo = rs.getInt("activo") == 1
                        )
                    }
                    return items
                }
            }
        }
    }

    override fun crearSubpresupuestoProyecto(
        proyectoId: Long,
        codSubpresupuesto: String?,
        nombre: String,
        orden: Int
    ): ProyectoSubpresupuesto {
        val sql = """
            INSERT INTO proyecto_subpresupuestos (proyecto_id, cod_subpresupuesto, nombre, orden)
            VALUES (?, ?, ?, ?)
        """.trimIndent()

        connector.openProyectos().use { cn ->
            cn.prepareStatement(sql).use { ps ->
                ps.setLong(1, proyectoId)
                ps.setString(2, codSubpresupuesto)
                ps.setString(3, nombre)
                ps.setInt(4, orden)
                ps.executeUpdate()
            }
        }

        return listarSubpresupuestosProyecto(proyectoId).last()
    }

    override fun listarPartidasProyecto(
        proyectoId: Long,
        subpresupuestoId: Long
    ): List<ProyectoPartida> {
        connector.openProyectos().use { cn ->
            if (canUseModernSchema(cn)) {
                val tituloId = resolveTituloId(cn, proyectoId, subpresupuestoId)
                if (tituloId == null) return emptyList()

                val sql = """
                    SELECT cu.id_costounitario,
                           cu.descripcion_costo,
                           COALESCE(u.abreviatura_unidad, u.descripcion_unidad) AS unidad,
                           COALESCE(cu.cantidad, 0) AS metrado,
                           COALESCE(cu.costo_unitario, 0) AS precio_unitario,
                           COALESCE(cu.parcial_costo, 0) AS parcial,
                           COALESCE(cu.posicion_costo, 0) AS orden
                    FROM costo_unitario cu
                    LEFT JOIN unidad u ON u.id_unidad = cu.id_unidad
                    WHERE cu.id_titulo = ?
                    ORDER BY cu.posicion_costo, cu.id_costounitario
                """.trimIndent()

                cn.prepareStatement(sql).use { ps ->
                    ps.setString(1, tituloId)
                    ps.executeQuery().use { rs ->
                        val items = mutableListOf<ProyectoPartida>()
                        while (rs.next()) {
                            items += ProyectoPartida(
                                id = stableId(rs.getString("id_costounitario")),
                                proyectoId = proyectoId,
                                subpresupuestoId = subpresupuestoId,
                                codPartidaBase = rs.getString("id_costounitario"),
                                descripcion = rs.getString("descripcion_costo"),
                                unidad = rs.getString("unidad"),
                                metrado = rs.getDouble("metrado"),
                                precioUnitario = rs.getDouble("precio_unitario"),
                                parcial = rs.getDouble("parcial"),
                                rendimientoMo = null,
                                rendimientoEq = null,
                                horasHombre = null,
                                horasMaquina = null,
                                origen = "SQLDELPHIN",
                                orden = rs.getInt("orden"),
                                activo = true
                            )
                        }
                        return items
                    }
                }
            }

            val sql = """
                SELECT *
                FROM proyecto_partidas
                WHERE proyecto_id = ?
                  AND subpresupuesto_id = ?
                ORDER BY orden, id
            """.trimIndent()

            cn.prepareStatement(sql).use { ps ->
                ps.setLong(1, proyectoId)
                ps.setLong(2, subpresupuestoId)
                ps.executeQuery().use { rs ->
                    val items = mutableListOf<ProyectoPartida>()
                    while (rs.next()) {
                        items += mapProyectoPartida(rs)
                    }
                    return items
                }
            }
        }
    }

    override fun crearProyectoPartida(
        proyectoId: Long,
        subpresupuestoId: Long?,
        codPartidaBase: String?,
        descripcion: String,
        unidad: String?,
        precioUnitario: Double,
        rendimientoMo: Double?,
        rendimientoEq: Double?,
        horasHombre: Double?,
        horasMaquina: Double?,
        origen: String,
        orden: Int
    ): ProyectoPartida {
        val sql = """
            INSERT INTO proyecto_partidas (
                proyecto_id, subpresupuesto_id, cod_partida_base, descripcion, unidad,
                metrado, precio_unitario, parcial, rendimiento_mo, rendimiento_eq,
                horas_hombre, horas_maquina, origen, orden
            )
            VALUES (?, ?, ?, ?, ?, 0, ?, 0, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        connector.openProyectos().use { cn ->
            cn.prepareStatement(sql).use { ps ->
                ps.setLong(1, proyectoId)
                if (subpresupuestoId == null) ps.setNull(2, java.sql.Types.BIGINT) else ps.setLong(2, subpresupuestoId)
                ps.setString(3, codPartidaBase)
                ps.setString(4, descripcion)
                ps.setString(5, unidad)
                ps.setDouble(6, precioUnitario)
                if (rendimientoMo == null) ps.setNull(7, java.sql.Types.DOUBLE) else ps.setDouble(7, rendimientoMo)
                if (rendimientoEq == null) ps.setNull(8, java.sql.Types.DOUBLE) else ps.setDouble(8, rendimientoEq)
                if (horasHombre == null) ps.setNull(9, java.sql.Types.DOUBLE) else ps.setDouble(9, horasHombre)
                if (horasMaquina == null) ps.setNull(10, java.sql.Types.DOUBLE) else ps.setDouble(10, horasMaquina)
                ps.setString(11, origen)
                ps.setInt(12, orden)
                ps.executeUpdate()
            }
        }

        return obtenerProyectoPartida(proyectoId, codPartidaBase ?: "")
            ?: error("No se pudo crear la partida")
    }

    override fun obtenerProyectoPartida(proyectoId: Long, codPartidaBase: String): ProyectoPartida? {
        val sql = """
            SELECT *
            FROM proyecto_partidas
            WHERE proyecto_id = ?
              AND cod_partida_base = ?
            LIMIT 1
        """.trimIndent()

        connector.openProyectos().use { cn ->
            cn.prepareStatement(sql).use { ps ->
                ps.setLong(1, proyectoId)
                ps.setString(2, codPartidaBase)
                ps.executeQuery().use { rs ->
                    return if (rs.next()) mapProyectoPartida(rs) else null
                }
            }
        }
    }

    override fun obtenerProyectoPartidaPorId(id: Long): ProyectoPartida? {
        val sql = """
            SELECT *
            FROM proyecto_partidas
            WHERE id = ?
        """.trimIndent()

        connector.openProyectos().use { cn ->
            cn.prepareStatement(sql).use { ps ->
                ps.setLong(1, id)
                ps.executeQuery().use { rs ->
                    return if (rs.next()) mapProyectoPartida(rs) else null
                }
            }
        }
    }

    override fun actualizarProyectoPartidaMetrado(
        proyectoPartidaId: Long,
        metrado: Double,
        parcial: Double
    ) {
        connector.openProyectos().use { cn ->
            if (canUseModernSchema(cn)) {
                val codigo = resolveCostoUnitarioId(cn, proyectoPartidaId) ?: return
                val sqlModern = """
                    UPDATE costo_unitario
                    SET cantidad = ?, parcial_costo = ?
                    WHERE id_costounitario = ?
                """.trimIndent()
                cn.prepareStatement(sqlModern).use { ps ->
                    ps.setDouble(1, metrado)
                    ps.setDouble(2, parcial)
                    ps.setString(3, codigo)
                    ps.executeUpdate()
                }
                return
            }

            val sql = """
                UPDATE proyecto_partidas
                SET metrado = ?, parcial = ?
                WHERE id = ?
            """.trimIndent()

            cn.prepareStatement(sql).use { ps ->
                ps.setDouble(1, metrado)
                ps.setDouble(2, parcial)
                ps.setLong(3, proyectoPartidaId)
                ps.executeUpdate()
            }
        }
    }

    override fun actualizarProyectoPartidaPrecioUnitario(
        proyectoPartidaId: Long,
        precioUnitario: Double
    ) {
        connector.openProyectos().use { cn ->
            if (canUseModernSchema(cn)) {
                val codigo = resolveCostoUnitarioId(cn, proyectoPartidaId) ?: return
                val sqlModern = """
                    UPDATE costo_unitario
                    SET costo_unitario = ?
                    WHERE id_costounitario = ?
                """.trimIndent()
                cn.prepareStatement(sqlModern).use { ps ->
                    ps.setDouble(1, precioUnitario)
                    ps.setString(2, codigo)
                    ps.executeUpdate()
                }
                return
            }

            val sql = """
                UPDATE proyecto_partidas
                SET precio_unitario = ?
                WHERE id = ?
            """.trimIndent()

            cn.prepareStatement(sql).use { ps ->
                ps.setDouble(1, precioUnitario)
                ps.setLong(2, proyectoPartidaId)
                ps.executeUpdate()
            }
        }
    }

    override fun listarDetalleProyectoPartida(proyectoPartidaId: Long): List<ProyectoPartidaDetalle> {
        val sql = """
            SELECT *
            FROM proyecto_partida_detalle
            WHERE proyecto_partida_id = ?
            ORDER BY id
        """.trimIndent()

        connector.openProyectos().use { cn ->
            cn.prepareStatement(sql).use { ps ->
                ps.setLong(1, proyectoPartidaId)
                ps.executeQuery().use { rs ->
                    val items = mutableListOf<ProyectoPartidaDetalle>()
                    while (rs.next()) {
                        items += ProyectoPartidaDetalle(
                            id = rs.getLong("id"),
                            proyectoPartidaId = rs.getLong("proyecto_partida_id"),
                            codInsumoBase = rs.getString("cod_insumo_base"),
                            descripcion = rs.getString("descripcion"),
                            unidad = rs.getString("unidad"),
                            tipo = rs.getInt("tipo"),
                            cuadrilla = rs.getDouble("cuadrilla"),
                            cantidad = rs.getDouble("cantidad"),
                            precioUnitario = rs.getDouble("precio_unitario"),
                            parcial = rs.getDouble("parcial"),
                            origen = rs.getString("origen"),
                            activo = rs.getInt("activo") == 1
                        )
                    }
                    return items
                }
            }
        }
    }

    override fun crearDetalleProyectoPartida(
        proyectoPartidaId: Long,
        codInsumoBase: String?,
        descripcion: String,
        unidad: String?,
        tipo: Int?,
        cuadrilla: Double?,
        cantidad: Double,
        precioUnitario: Double,
        parcial: Double,
        origen: String
    ): ProyectoPartidaDetalle {
        val sql = """
            INSERT INTO proyecto_partida_detalle (
                proyecto_partida_id, cod_insumo_base, descripcion, unidad,
                tipo, cuadrilla, cantidad, precio_unitario, parcial, origen
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        connector.openProyectos().use { cn ->
            cn.prepareStatement(sql).use { ps ->
                ps.setLong(1, proyectoPartidaId)
                ps.setString(2, codInsumoBase)
                ps.setString(3, descripcion)
                ps.setString(4, unidad)
                if (tipo == null) ps.setNull(5, java.sql.Types.INTEGER) else ps.setInt(5, tipo)
                if (cuadrilla == null) ps.setNull(6, java.sql.Types.DOUBLE) else ps.setDouble(6, cuadrilla)
                ps.setDouble(7, cantidad)
                ps.setDouble(8, precioUnitario)
                ps.setDouble(9, parcial)
                ps.setString(10, origen)
                ps.executeUpdate()
            }
        }

        return listarDetalleProyectoPartida(proyectoPartidaId).last()
    }

    override fun actualizarDetalleProyectoPartidaPrecio(
        detalleId: Long,
        precioUnitario: Double,
        parcial: Double
    ) {
        val sql = """
            UPDATE proyecto_partida_detalle
            SET precio_unitario = ?, parcial = ?
            WHERE id = ?
        """.trimIndent()

        connector.openProyectos().use { cn ->
            cn.prepareStatement(sql).use { ps ->
                ps.setDouble(1, precioUnitario)
                ps.setDouble(2, parcial)
                ps.setLong(3, detalleId)
                ps.executeUpdate()
            }
        }
    }

    override fun eliminarDetalleProyectoPartida(proyectoPartidaId: Long) {
        val sql = """
            DELETE FROM proyecto_partida_detalle
            WHERE proyecto_partida_id = ?
        """.trimIndent()

        connector.openProyectos().use { cn ->
            cn.prepareStatement(sql).use { ps ->
                ps.setLong(1, proyectoPartidaId)
                ps.executeUpdate()
            }
        }
    }

    private fun mapProyectoPartida(rs: ResultSet): ProyectoPartida {
        val subpresupuestoValue = rs.getLong("subpresupuesto_id")
        val subpresupuestoId = if (rs.wasNull()) null else subpresupuestoValue

        val rendimientoMoValue = rs.getDouble("rendimiento_mo")
        val rendimientoMo = if (rs.wasNull()) null else rendimientoMoValue

        val rendimientoEqValue = rs.getDouble("rendimiento_eq")
        val rendimientoEq = if (rs.wasNull()) null else rendimientoEqValue

        val horasHombreValue = rs.getDouble("horas_hombre")
        val horasHombre = if (rs.wasNull()) null else horasHombreValue

        val horasMaquinaValue = rs.getDouble("horas_maquina")
        val horasMaquina = if (rs.wasNull()) null else horasMaquinaValue

        return ProyectoPartida(
            id = rs.getLong("id"),
            proyectoId = rs.getLong("proyecto_id"),
            subpresupuestoId = subpresupuestoId,
            codPartidaBase = rs.getString("cod_partida_base"),
            descripcion = rs.getString("descripcion"),
            unidad = rs.getString("unidad"),
            metrado = rs.getDouble("metrado"),
            precioUnitario = rs.getDouble("precio_unitario"),
            parcial = rs.getDouble("parcial"),
            rendimientoMo = rendimientoMo,
            rendimientoEq = rendimientoEq,
            horasHombre = horasHombre,
            horasMaquina = horasMaquina,
            origen = rs.getString("origen"),
            orden = rs.getInt("orden"),
            activo = rs.getInt("activo") == 1
        )
    }

    private fun canUseModernSchema(connection: Connection): Boolean {
        return hasTable(connection, "presupuesto") && hasTable(connection, "titulo") && hasTable(connection, "costo_unitario")
    }

    private fun hasTable(connection: Connection, tableName: String): Boolean {
        val sql = "SELECT 1 FROM sqlite_master WHERE type = 'table' AND lower(name) = lower(?) LIMIT 1"
        connection.prepareStatement(sql).use { ps ->
            ps.setString(1, tableName)
            ps.executeQuery().use { rs -> return rs.next() }
        }
    }

    private fun stableId(value: String?): Long {
        return abs((value ?: "").hashCode().toLong())
    }

    private fun resolveTituloId(connection: Connection, proyectoId: Long, syntheticId: Long): String? {
        val sql = """
            SELECT t.id_titulo
            FROM titulo t
            JOIN presupuesto p ON p.id_presupuesto = t.id_presupuesto
            WHERE p.id_proyecto = ?
              AND (t.id_titulopadre IS NULL OR TRIM(t.id_titulopadre) = '')
        """.trimIndent()
        connection.prepareStatement(sql).use { ps ->
            ps.setString(1, proyectoId.toString())
            ps.executeQuery().use { rs ->
                while (rs.next()) {
                    val id = rs.getString("id_titulo")
                    if (stableId(id) == syntheticId) return id
                }
            }
        }
        return null
    }

    private fun resolveCostoUnitarioId(connection: Connection, syntheticId: Long): String? {
        val sql = "SELECT id_costounitario FROM costo_unitario"
        connection.prepareStatement(sql).use { ps ->
            ps.executeQuery().use { rs ->
                while (rs.next()) {
                    val id = rs.getString("id_costounitario")
                    if (stableId(id) == syntheticId) return id
                }
            }
        }
        return null
    }
}
