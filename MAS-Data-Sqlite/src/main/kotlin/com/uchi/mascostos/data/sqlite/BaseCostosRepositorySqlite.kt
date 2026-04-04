package com.uchi.mascostos.data.sqlite

import com.uchi.mascostos.core.model.InsumoBase
import com.uchi.mascostos.core.model.PartidaBase
import com.uchi.mascostos.core.model.Subpresupuesto
import com.uchi.mascostos.core.ports.BaseCostosRepository
import com.uchi.mascostos.core.ports.DetallePartidaBaseRow
import com.uchi.mascostos.core.ports.PartidaJerarquiaNode
import java.sql.Connection

class BaseCostosRepositorySqlite(
    private val connector: SQLiteConnector
) : BaseCostosRepository {

    override fun listarSubpresupuestos(codPresupuesto: String): List<Subpresupuesto> {
        connector.openBaseCostos().use { cn ->
            val sql = if (hasTable(cn, "presupuesto") && hasTable(cn, "titulo")) {
                """
                SELECT p.id_presupuesto AS cod_presupuesto,
                       t.id_titulo AS cod_subpresupuesto,
                       t.descripcion_titulo AS descripcion
                FROM presupuesto p
                JOIN titulo t ON t.id_presupuesto = p.id_presupuesto
                WHERE p.id_presupuesto = ?
                  AND (t.id_titulopadre IS NULL OR TRIM(t.id_titulopadre) = '')
                ORDER BY t.posicion_titulo, t.id_titulo
                """.trimIndent()
            } else {
                """
                SELECT cod_presupuesto, cod_subpresupuesto, descripcion
                FROM subpresupuestos
                WHERE cod_presupuesto = ?
                ORDER BY cod_subpresupuesto
                """.trimIndent()
            }

            cn.prepareStatement(sql).use { ps ->
                ps.setString(1, codPresupuesto)
                ps.executeQuery().use { rs ->
                    val items = mutableListOf<Subpresupuesto>()
                    while (rs.next()) {
                        items += Subpresupuesto(
                            codPresupuesto = rs.getString("cod_presupuesto"),
                            codSubpresupuesto = rs.getString("cod_subpresupuesto"),
                            descripcion = rs.getString("descripcion")
                        )
                    }
                    return items
                }
            }
        }
    }

    override fun listarPartidasPresupuesto(
        codPresupuesto: String,
        codSubpresupuesto: String
    ): List<PartidaBase> {
        connector.openBaseCostos().use { cn ->
            val sql = if (hasTable(cn, "costo_unitario") && hasTable(cn, "titulo")) {
                """
                SELECT
                    cu.id_costounitario AS cod_partida,
                    cu.descripcion_costo AS descripcion,
                    COALESCE(u.abreviatura_unidad, u.descripcion_unidad) AS simbolo,
                    cu.costo_unitario AS precio1,
                    NULL AS horas_hombre,
                    NULL AS horas_maquina,
                    NULL AS rendimiento_mo,
                    NULL AS rendimiento_eq
                FROM costo_unitario cu
                JOIN titulo t ON t.id_titulo = cu.id_titulo
                LEFT JOIN unidad u ON u.id_unidad = cu.id_unidad
                WHERE t.id_presupuesto = ?
                  AND (t.id_titulo = ? OR t.id_titulopadre = ?)
                ORDER BY t.posicion_titulo, cu.posicion_costo, cu.id_costounitario
                """.trimIndent()
            } else {
                """
                SELECT
                    pp.cod_partida,
                    p.descripcion,
                    u.simbolo,
                    pp.precio1,
                    pp.horas_hombre,
                    pp.horas_maquina,
                    p.rendimiento_mo,
                    p.rendimiento_eq
                FROM presupuesto_partida pp
                LEFT JOIN partidas p
                    ON p.cod_partida = pp.cod_partida
                LEFT JOIN unidades u
                    ON u.cod_unidad = p.cod_unidad
                WHERE pp.cod_presupuesto = ?
                  AND pp.cod_subpresupuesto = ?
                ORDER BY pp.cod_partida
                """.trimIndent()
            }

            cn.prepareStatement(sql).use { ps ->
                ps.setString(1, codPresupuesto)
                ps.setString(2, codSubpresupuesto)
                if (sql.contains("t.id_titulopadre = ?")) {
                    ps.setString(3, codSubpresupuesto)
                }
                ps.executeQuery().use { rs ->
                    val items = mutableListOf<PartidaBase>()
                    while (rs.next()) {
                        items += PartidaBase(
                            codPartida = rs.getString("cod_partida"),
                            descripcion = rs.getString("descripcion") ?: rs.getString("cod_partida"),
                            unidad = rs.getString("simbolo"),
                            precioUnitario = rs.getDouble("precio1"),
                            horasHombre = rs.getDouble("horas_hombre"),
                            horasMaquina = rs.getDouble("horas_maquina"),
                            rendimientoMo = rs.getDouble("rendimiento_mo"),
                            rendimientoEq = rs.getDouble("rendimiento_eq")
                        )
                    }
                    return items
                }
            }
        }
    }


    override fun obtenerPartida(codPartida: String): PartidaBase? {
        connector.openBaseCostos().use { cn ->
            val sql = if (hasTable(cn, "costo_unitario")) {
                """
                SELECT
                    cu.id_costounitario AS cod_partida,
                    cu.descripcion_costo AS descripcion,
                    COALESCE(u.abreviatura_unidad, u.descripcion_unidad) AS simbolo,
                    cu.costo_unitario AS precio1,
                    NULL AS horas_hombre,
                    NULL AS horas_maquina,
                    NULL AS rendimiento_mo,
                    NULL AS rendimiento_eq
                FROM costo_unitario cu
                LEFT JOIN unidad u ON u.id_unidad = cu.id_unidad
                WHERE cu.id_costounitario = ?
                LIMIT 1
                """.trimIndent()
            } else {
                """
                SELECT
                    p.cod_partida,
                    p.descripcion,
                    u.simbolo,
                    pp.precio1,
                    pp.horas_hombre,
                    pp.horas_maquina,
                    p.rendimiento_mo,
                    p.rendimiento_eq
                FROM partidas p
                LEFT JOIN unidades u
                    ON u.cod_unidad = p.cod_unidad
                LEFT JOIN presupuesto_partida pp
                    ON pp.cod_partida = p.cod_partida
                WHERE p.cod_partida = ?
                ORDER BY pp.ano DESC, pp.mes DESC
                LIMIT 1
                """.trimIndent()
            }

            cn.prepareStatement(sql).use { ps ->
                ps.setString(1, codPartida)
                ps.executeQuery().use { rs ->
                    return if (rs.next()) {
                        PartidaBase(
                            codPartida = rs.getString("cod_partida"),
                            descripcion = rs.getString("descripcion") ?: rs.getString("cod_partida"),
                            unidad = rs.getString("simbolo"),
                            precioUnitario = rs.getDouble("precio1").let { if (rs.wasNull()) null else it },
                            horasHombre = rs.getDouble("horas_hombre").let { if (rs.wasNull()) null else it },
                            horasMaquina = rs.getDouble("horas_maquina").let { if (rs.wasNull()) null else it },
                            rendimientoMo = rs.getDouble("rendimiento_mo").let { if (rs.wasNull()) null else it },
                            rendimientoEq = rs.getDouble("rendimiento_eq").let { if (rs.wasNull()) null else it }
                        )
                    } else {
                        null
                    }
                }
            }
        }
    }


    override fun obtenerInsumo(codInsumo: String): InsumoBase? {
        connector.openBaseCostos().use { cn ->
            val sql = if (hasTable(cn, "producto")) {
                """
                SELECT
                    p.id_producto AS cod_insumo,
                    p.descripcion_producto AS descripcion,
                    COALESCE(u.abreviatura_unidad, u.descripcion_unidad) AS simbolo
                FROM producto p
                LEFT JOIN unidad u ON u.id_unidad = p.id_unidad
                WHERE p.id_producto = ?
                LIMIT 1
                """.trimIndent()
            } else {
                """
                SELECT
                    i.cod_insumo,
                    i.descripcion,
                    u.simbolo
                FROM insumos i
                LEFT JOIN unidades u
                    ON u.cod_unidad = i.cod_unidad
                WHERE i.cod_insumo = ?
                LIMIT 1
                """.trimIndent()
            }

            cn.prepareStatement(sql).use { ps ->
                ps.setString(1, codInsumo)
                ps.executeQuery().use { rs ->
                    return if (rs.next()) {
                        InsumoBase(
                            codInsumo = rs.getString("cod_insumo"),
                            descripcion = rs.getString("descripcion") ?: rs.getString("cod_insumo"),
                            unidad = rs.getString("simbolo")
                        )
                    } else {
                        null
                    }
                }
            }
        }
    }

    override fun listarDetallePartida(codPartida: String): List<DetallePartidaBaseRow> {
        val sql = """
            SELECT
                pd.cod_insumo,
                i.descripcion,
                u.simbolo,
                pd.tipo,
                pd.cuadrilla,
                pd.cantidad
            FROM partida_detalle pd
            LEFT JOIN insumos i
                ON i.cod_insumo = pd.cod_insumo
            LEFT JOIN unidades u
                ON u.cod_unidad = i.cod_unidad
            WHERE pd.cod_partida = ?
            ORDER BY pd.id_detalle
        """.trimIndent()

        connector.openBaseCostos().use { cn ->
            cn.prepareStatement(sql).use { ps ->
                ps.setString(1, codPartida)
                ps.executeQuery().use { rs ->
                    val items = mutableListOf<DetallePartidaBaseRow>()
                    while (rs.next()) {
                        items += DetallePartidaBaseRow(
                            codInsumo = rs.getString("cod_insumo"),
                            descripcion = rs.getString("descripcion") ?: rs.getString("cod_insumo"),
                            unidad = rs.getString("simbolo"),
                            tipo = rs.getInt("tipo"),
                            cuadrilla = rs.getDouble("cuadrilla"),
                            cantidad = rs.getDouble("cantidad")
                        )
                    }
                    return items
                }
            }
        }
    }

    override fun obtenerPrecioInsumo(
        codPresupuesto: String,
        codSubpresupuesto: String,
        codInsumo: String
    ): Double? {
        val sql = """
            SELECT precio1
            FROM precio_particular_insumo
            WHERE cod_presupuesto = ?
              AND cod_subpresupuesto = ?
              AND cod_insumo = ?
            ORDER BY ano DESC, mes DESC
            LIMIT 1
        """.trimIndent()

        connector.openBaseCostos().use { cn ->
            cn.prepareStatement(sql).use { ps ->
                ps.setString(1, codPresupuesto)
                ps.setString(2, codSubpresupuesto)
                ps.setString(3, codInsumo)
                ps.executeQuery().use { rs ->
                    return if (rs.next()) rs.getDouble("precio1") else null
                }
            }
        }
    }

    override fun listarAncestrosPartida(codPartida: String): List<PartidaJerarquiaNode> {
        val sql = """
        SELECT cod_partida, descripcion
        FROM partidas
        WHERE ? LIKE cod_partida || '%'
          AND cod_partida <> ?
          AND descripcion IS NOT NULL
          AND TRIM(descripcion) <> ''
        ORDER BY LENGTH(cod_partida) ASC, cod_partida ASC
    """.trimIndent()

        connector.openBaseCostos().use { cn ->
            cn.prepareStatement(sql).use { ps ->
                ps.setString(1, codPartida)
                ps.setString(2, codPartida)
                ps.executeQuery().use { rs ->
                    val items = mutableListOf<PartidaJerarquiaNode>()
                    while (rs.next()) {
                        items += PartidaJerarquiaNode(
                            codPartida = rs.getString("cod_partida"),
                            descripcion = rs.getString("descripcion")
                        )
                    }
                    return items
                }
            }
        }
    }

    private fun hasTable(connection: Connection, tableName: String): Boolean {
        val sql = "SELECT 1 FROM sqlite_master WHERE type = 'table' AND lower(name) = lower(?) LIMIT 1"
        connection.prepareStatement(sql).use { ps ->
            ps.setString(1, tableName)
            ps.executeQuery().use { rs ->
                return rs.next()
            }
        }
    }
}
