package com.uchi.mascostos.data.sqlite

import com.uchi.mascostos.core.model.PartidaBase
import com.uchi.mascostos.core.model.Subpresupuesto
import com.uchi.mascostos.core.ports.BaseCostosRepository
import com.uchi.mascostos.core.ports.DetallePartidaBaseRow
import com.uchi.mascostos.core.ports.PartidaJerarquiaNode

class BaseCostosRepositorySqlite(
    private val connector: SQLiteConnector
) : BaseCostosRepository {

    override fun listarSubpresupuestos(codPresupuesto: String): List<Subpresupuesto> {
        val sql = """
            SELECT cod_presupuesto, cod_subpresupuesto, descripcion
            FROM subpresupuestos
            WHERE cod_presupuesto = ?
            ORDER BY cod_subpresupuesto
        """.trimIndent()

        connector.openBaseCostos().use { cn ->
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
        val sql = """
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

        connector.openBaseCostos().use { cn ->
            cn.prepareStatement(sql).use { ps ->
                ps.setString(1, codPresupuesto)
                ps.setString(2, codSubpresupuesto)
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
}