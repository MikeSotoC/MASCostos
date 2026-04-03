package com.uchi.mascostos.data.sqlite

import com.uchi.mascostos.core.model.Proyecto
import com.uchi.mascostos.core.ports.ProyectoRepository

class ProyectoRepositorySqlite(
    private val connector: SQLiteConnector
) : ProyectoRepository {

    override fun listar(): List<Proyecto> {
        val sql = """
            SELECT id, codigo, nombre, cliente, ubicacion, moneda, estado, observaciones
            FROM proyectos
            ORDER BY id
        """.trimIndent()

        connector.openProyectos().use { cn ->
            cn.prepareStatement(sql).use { ps ->
                ps.executeQuery().use { rs ->
                    val items = mutableListOf<Proyecto>()
                    while (rs.next()) {
                        items += Proyecto(
                            id = rs.getLong("id"),
                            codigo = rs.getString("codigo"),
                            nombre = rs.getString("nombre"),
                            cliente = rs.getString("cliente"),
                            ubicacion = rs.getString("ubicacion"),
                            moneda = rs.getString("moneda"),
                            estado = rs.getString("estado"),
                            observaciones = rs.getString("observaciones")
                        )
                    }
                    return items
                }
            }
        }
    }

    override fun obtenerPorId(id: Long): Proyecto? {
        val sql = """
            SELECT id, codigo, nombre, cliente, ubicacion, moneda, estado, observaciones
            FROM proyectos
            WHERE id = ?
        """.trimIndent()

        connector.openProyectos().use { cn ->
            cn.prepareStatement(sql).use { ps ->
                ps.setLong(1, id)
                ps.executeQuery().use { rs ->
                    return if (rs.next()) {
                        Proyecto(
                            id = rs.getLong("id"),
                            codigo = rs.getString("codigo"),
                            nombre = rs.getString("nombre"),
                            cliente = rs.getString("cliente"),
                            ubicacion = rs.getString("ubicacion"),
                            moneda = rs.getString("moneda"),
                            estado = rs.getString("estado"),
                            observaciones = rs.getString("observaciones")
                        )
                    } else {
                        null
                    }
                }
            }
        }
    }

    override fun obtenerPorCodigo(codigo: String): Proyecto? {
        val sql = """
            SELECT id, codigo, nombre, cliente, ubicacion, moneda, estado, observaciones
            FROM proyectos
            WHERE codigo = ?
        """.trimIndent()

        connector.openProyectos().use { cn ->
            cn.prepareStatement(sql).use { ps ->
                ps.setString(1, codigo)
                ps.executeQuery().use { rs ->
                    return if (rs.next()) {
                        Proyecto(
                            id = rs.getLong("id"),
                            codigo = rs.getString("codigo"),
                            nombre = rs.getString("nombre"),
                            cliente = rs.getString("cliente"),
                            ubicacion = rs.getString("ubicacion"),
                            moneda = rs.getString("moneda"),
                            estado = rs.getString("estado"),
                            observaciones = rs.getString("observaciones")
                        )
                    } else {
                        null
                    }
                }
            }
        }
    }

    override fun crear(
        codigo: String,
        nombre: String,
        cliente: String?,
        ubicacion: String?,
        moneda: String,
        observaciones: String?
    ): Proyecto {
        val sql = """
            INSERT INTO proyectos (codigo, nombre, cliente, ubicacion, moneda, observaciones)
            VALUES (?, ?, ?, ?, ?, ?)
        """.trimIndent()

        connector.openProyectos().use { cn ->
            cn.prepareStatement(sql).use { ps ->
                ps.setString(1, codigo)
                ps.setString(2, nombre)
                ps.setString(3, cliente)
                ps.setString(4, ubicacion)
                ps.setString(5, moneda)
                ps.setString(6, observaciones)
                ps.executeUpdate()
            }
        }

        return obtenerPorCodigo(codigo) ?: error("No se pudo crear el proyecto")
    }
}