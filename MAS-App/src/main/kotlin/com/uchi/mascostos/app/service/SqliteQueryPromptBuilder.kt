package com.uchi.mascostos.app.service

object SqliteQueryPromptBuilder {

    private const val DEFAULT_SCHEMA = """
SCHEMA_SQLDELPHIN_BASICA (extracto mínimo, completar por introspección en runtime):
- proyecto(id_proyecto, nombre_proyecto, id_usuario, fecha_inicio, fecha_fin, duracion)
- presupuesto(id_presupuesto, id_proyecto, nombre_presupuesto, costo_directo, total_presupuesto)
- titulo(id_titulo, id_presupuesto, id_titulopadre, descripcion_titulo, numeracion_titulo)
- costo_unitario(id_costounitario, id_titulo, descripcion_costo, id_unidad, costo_unitario)
- composicion_costounitario(id_composicion, id_subtotal, id_costoauxiliar, cantidad_composicion, costo_composicion)
- subtotal_costounitario(id_subtotal, id_costounitario, id_tipocosto, subtotal)
- unidad(id_unidad, descripcion_unidad, abreviatura_unidad)
- producto(id_producto, descripcion_producto, id_unidad, id_categoria)
- stock_producto(id_stock, id_sucursal, id_producto, saldo_almacen)
- compra_fact(id_comprafact, id_proveedor, id_documento, fecha_documento, total_documento)
- detalle_compra_fact(id_detallecomprafact, id_comprafact, id_producto, cantidad, precio_unitario)
- salida(id_salida, id_cliente, fecha_documento, id_proyecto)
- detalle_salida(id_detallesalida, id_salida, id_producto, cantidad, costo_unitario)
- requerimiento(id_requerimiento, id_proyecto, fecha_documento, total_documento)
- detalle_requerimiento(id_detallerequerimiento, id_requerimiento, id_producto, cantidad, precio_unitario)
- persona(id_persona, nombre_persona, apellidos_persona, id_tipopersona)
- usuario(id_usuario, login_usuario, id_perfil, id_persona)
- kardex(id_movimiento, id_producto, fecha_movimiento, entrada_almacen, salida_almacen, saldo_almacen)
- moneda(id_moneda, nombre_moneda, simbolo_moneda)
- parametro(id_parametro, valor_parametro, valor_texto)
"""

    private const val TEMPLATE = """
Eres un motor experto en generación de consultas SQL para SQLite, integrado en una aplicación Kotlin.

Tu única tarea es convertir texto del usuario en consultas SQL válidas sobre SQLDelphin_basica.sqlite.

========================
REGLAS OBLIGATORIAS
===================

* SOLO generar consultas SELECT
* PROHIBIDO usar INSERT, UPDATE, DELETE, DROP o ALTER
* NO inventar tablas
* NO inventar columnas
* SIEMPRE usar LIMIT 50
* USAR JOIN cuando haya relaciones
* SI la consulta es ambigua, asumir el caso más común
* RESPONDER SOLO con SQL (sin explicaciones, sin texto extra)

========================
SCHEMA DISPONIBLE
=================

{SCHEMA_CONTEXT}

NOTA: Si falta detalle de columnas para una tabla, usa únicamente columnas comunes/seguras y prioriza joins por llaves obvias (id_*).

========================
FORMATO DE RESPUESTA
====================

* SOLO SQL
* SIN comillas triples
* SIN explicaciones
* SIN markdown

========================
ENTRADA DEL USUARIO
===================

{USER_INPUT}
"""

    fun build(userInput: String): String = build(userInput, DEFAULT_SCHEMA)

    fun build(userInput: String, schemaContext: String): String {
        val safeSchema = schemaContext.trim().ifEmpty { DEFAULT_SCHEMA.trim() }
        return TEMPLATE
            .replace("{SCHEMA_CONTEXT}", safeSchema)
            .replace("{USER_INPUT}", userInput.trim())
    }
}
