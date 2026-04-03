# Objetivo de producto: “S10 moderno” para costos y presupuestos (Construcción Civil)

Fecha: 2026-04-03.

## 1) Objetivo principal

Construir una plataforma de **costos y presupuestos para construcción civil** que cubra el flujo operativo mínimo de una oficina técnica (desde base de costos hasta presupuesto de proyecto), con una UX más moderna y arquitectura modular mantenible.

En términos simples:

> "Que un equipo pueda armar, editar, analizar y controlar un presupuesto de obra con productividad similar a S10, pero con mejor experiencia de usuario, portabilidad y evolución técnica".

---

## 2) Módulos mínimos (alcance inicial)

Para un “mínimo viable serio” (MVP profesional) se recomienda implementar estos módulos:

### M1. Catálogo Base de Costos

- Insumos (materiales, mano de obra, equipos, subcontratos).
- Unidades, familias, y clasificación.
- Precios base y precios por período.

### M2. Análisis de Precios Unitarios (APU)

- Partidas con recursos, cuadrillas, rendimientos y cantidades.
- Cálculo de costo unitario por partida.
- Versionado de análisis.

### M3. Presupuesto de Obra

- Estructura por capítulos/subpresupuestos/partidas.
- Metrados, PU, parciales y totales.
- Resumen por capítulos y totales de proyecto.

### M4. Fórmulas Polinómicas (mínimo)

- Definición de monomios e índices.
- Cálculo básico de reajuste.

### M5. Gastos Generales + Utilidad + IGV

- Configuración por proyecto.
- Aplicación sobre costos directos.
- Presupuesto de venta.

### M6. Reportes y exportación

- Presupuesto resumido y detallado.
- APU por partida.
- Exportación a Excel/PDF.

### M7. Productividad operativa

- Búsqueda rápida, duplicar/copy-paste partidas.
- Importación inicial desde hojas Excel (plantilla controlada).

---

## 3) Qué NO debe faltar para que sea “usable en obra”

- Trazabilidad de cambios (quién cambió qué y cuándo).
- Versiones de presupuesto (v1, v2, v3).
- Bloqueo de versión aprobada.
- Control de redondeo y precisión de cálculos.
- Validaciones de coherencia (ej. metrado negativo, PU nulo).

---

## 4) Definición de éxito (KPIs)

- Tiempo de armado de presupuesto completo: **-30%** vs flujo manual actual.
- Errores de consolidación: **< 1%** por cierre de presupuesto.
- Reuso de partidas/APU base: **> 60%**.
- Tiempo de respuesta UI en operaciones frecuentes: **< 300 ms**.

---

## 5) Roadmap recomendado (incremental)

## Fase 1 (0–8 semanas): “Core Presupuestos”

- M1 + M2 + M3.
- Reportes básicos.
- Importación desde plantilla Excel.
- Versionado simple.

**Entregable:** presupuesto completo de una obra mediana (civil) con APU y exportables.

## Fase 2 (8–16 semanas): “Control y reajustes”

- M4 + M5.
- Auditoría de cambios.
- Mejoras de UX en edición masiva.

**Entregable:** presupuesto vendible con reajuste y control formal.

## Fase 3 (16–24 semanas): “Escala operativa”

- Catálogo corporativo compartido.
- Flujos de aprobación (revisor/aprobador).
- Integraciones (BI/ERP/Compras) vía export/import API.

**Entregable:** operación multi-proyecto con gobierno.

---

## 6) Alcance funcional concreto para “Construcción Civil” (mínimo)

Especialidades iniciales:

- Movimiento de tierras.
- Concreto simple/armado.
- Encofrados y acero.
- Albañilería.
- Acabados principales.
- Instalaciones sanitarias y eléctricas básicas (nivel presupuesto, no ingeniería de detalle).

---

## 7) Propuesta de arquitectura del producto

- **MAS-Core**: reglas de dominio (APU, presupuesto, cálculos).
- **MAS-App**: casos de uso y servicios de aplicación.
- **MAS-Data-Sqlite**: persistencia local (MVP) + migrable a motor servidor.
- **MAS-UI-Desktop**: cliente operativo de oficina técnica.

Siguiente paso técnico recomendado:

1. Mantener SQLite para piloto local.
2. Preparar transición a PostgreSQL para operación multiusuario.

---

## 8) Meta final de esta visión

No se busca “copiar S10 pantalla por pantalla”, sino construir un **equivalente moderno**:

- Mismo poder técnico en costos/presupuestos.
- Mejor UX y mantenibilidad.
- Apertura para integración futura.

Si cumplimos los módulos M1..M7 y los criterios de éxito, tendremos un **S10 moderno mínimo viable y útil para construcción civil**.
