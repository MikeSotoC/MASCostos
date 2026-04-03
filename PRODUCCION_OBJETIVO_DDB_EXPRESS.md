# Objetivo de producción: nivel ddb.express (S10 moderno)

Fecha: 2026-04-03.

## Visión

Construir una plataforma de costos, presupuestos y control de obra para construcción civil que pueda operar proyectos reales de punta a punta, con experiencia de usuario moderna y arquitectura escalable.

## Módulos objetivo (benchmark ddb.express)

Tomando como benchmark funcional la oferta pública de ddb.express, el objetivo funcional mínimo-medio debe cubrir:

1. **Elaboración de Presupuestos**
   - estructura por títulos/subtítulos/partidas,
   - APU,
   - metrados,
   - curva S,
   - programación tipo Gantt,
   - import/export (xlsx/csv/pdf).
2. **Cuaderno de Obra**
   - bitácora diaria,
   - documentos contractuales,
   - registro fotográfico.
3. **Control y Seguimiento de Obra**
   - requerimientos de insumos,
   - compras,
   - control por recursos (MO/EQ/MAT/Subcontratos),
   - comparativo PU presupuestado vs ejecutado,
   - control financiero (ingresos/egresos).
4. **Reporteador**
   - informes en tiempo real,
   - salidas PDF/Excel/CSV/HTML.
5. **(Opcional fase posterior) BIM / Delphin Office equivalente**
   - integración IFC,
   - utilidades documentales avanzadas.

## Qué significa “nivel producción” para MASCostos

Para decir que llegamos a nivel producción (similar al estándar ddb.express en alcance base), MASCostos debe cumplir al menos:

- Cobertura completa de Presupuesto + APU + Metrados + Gantt + Curva S.
- Flujo operativo: Requerimiento -> Compra -> Consumo en obra -> Desviación de costos.
- Cuaderno de obra digital auditable (bitácora, anexos, evidencia).
- Reporteador con plantillas y exportación formal.
- Seguridad y gobierno: roles, permisos, trazabilidad y versionado.
- Operación estable multiusuario (base servidor + backups + monitoreo).

## Brecha actual del repositorio

Estado del repo actual (a alto nivel):

- **Cubierto parcialmente:** Core de presupuesto/APU y UI desktop inicial.
- **No cubierto o incompleto:** Gantt/Curva S robusto, cuaderno de obra, compras/requerimientos, control financiero, reportería avanzada, seguridad/roles multiusuario.

## Mínimos para Construcción Civil (prioridad real)

### Bloque A — Presupuestación completa (imprescindible)
- Catálogo de insumos + precios históricos.
- APU versionable.
- Presupuesto por capítulos con consolidados.
- Metrados y valorización básica.

### Bloque B — Programación y control
- Gantt con ruta crítica básica.
- Curva S (plan vs ejecutado).
- Avance diario por partida.

### Bloque C — Logística y costos reales
- Requerimientos de insumos desde presupuesto meta.
- Compras vinculadas.
- Consumo real de obra y desvío de costo.

### Bloque D — Gestión y cierre
- Cuaderno de obra.
- Reportes ejecutivos y técnicos.
- Flujo financiero básico de obra.

## Roadmap recomendado (6 meses)

### Fase 1 (Semanas 1–8)
- Endurecer módulo Presupuesto/APU/Metrados.
- Versionado, auditoría, validaciones y reportes base.
- Migraciones DB + pruebas automáticas + CI.

### Fase 2 (Semanas 9–16)
- Programación Gantt + Curva S + valorización por período.
- Panel de avance físico/económico.

### Fase 3 (Semanas 17–24)
- Requerimientos, compras y consumo real.
- Control financiero de obra + cuaderno de obra.
- Reporteador formal y exportables empresariales.

## KPI de aceptación (go-live)

- Elaborar presupuesto completo de obra mediana en < 2 días.
- Variación entre costo planificado y ejecutado trazable por partida/recurso.
- Reportes mensuales emitidos en < 10 minutos.
- 0 pérdida de datos en pruebas de respaldo/restauración.
- Tiempo de respuesta < 300 ms en operaciones frecuentes.

## Resultado esperado

Al finalizar este roadmap, MASCostos debería comportarse como un **S10 moderno enfocado en construcción civil**, cubriendo el ciclo:

**Base de costos -> APU -> Presupuesto -> Programación -> Requerimientos/Compras -> Control de obra -> Control financiero -> Reportes.**
