# MASCostos (base)

Base inicial de una app de presupuestos **similar a Delphin Express** (sin BIM por ahora), con:

- **Kotlin** como lenguaje principal.
- **JavaFX + AtlantaFX** para la UI de escritorio.
- **Interfaz Android (Jetpack Compose) alineada con Desktop**.
- **API pública de plugins/addons** para extender funcionalidades.
- Uso de la base SQL existente: `SQLDelphin_basica.sql`.

## Regla principal de este proyecto

`SQLDelphin_basica.sql` es la **fuente de verdad** del modelo de datos:

1. El archivo contiene `CREATE TABLE` + `INSERT` para SQLite.
2. El arranque del sistema debe inicializar la base desde ese SQL cuando sea necesario.
3. Nuevos casos de uso deben mapear primero a las tablas/campos existentes en ese script.

## Módulos

- `mas-api`: contratos públicos para plugins y gateways de negocio.
- `mas-core`: dominio, servicios y adaptadores SQLite.
- `mas-shared-ui`: lógica de UI compartida (subtotales, total y convención de reporte).
- `mas-desktop`: app JavaFX de escritorio.
- `mas-android`: app Android Compose con la misma estructura funcional de UI base.

## Funcionalidad implementada en esta fase

1. **Registro de plugins** vía `ServiceLoader`.
2. **Casos de negocio base**: creación/listado de proyectos, agregado de ítems y estimación de presupuesto.
3. **Adaptadores SQLite** para proyecto, items de proyecto y catálogo de costos.
4. **Inicialización condicional de SQLite** desde `SQLDelphin_basica.sql`.
5. **Estructura presupuestal por proyecto**: lectura de `titulo` + `costo_unitario` usando `id_proyecto -> presupuesto -> titulo`.
6. **Vista jerárquica en Desktop** agrupada por título y selección múltiple para agregar costos en lote.
7. **Metrado básico**: edición de cantidad por ítem agregado + subtotales por título en tiempo real.
8. **Selector de presupuesto activo** por proyecto para filtrar la estructura cargada.
9. **Reporte CSV** exportable desde desktop (resumen/detalle por ítems).
10. **Android UI paralela** con módulos visuales equivalentes.
11. **Lógica UI compartida** entre Desktop/Android para totalización y convención de reportes.
12. **Contrato de aplicación unificado** (`BudgetAppService`) con implementación core y mock Android para facilitar la futura conexión real.

## Falta implementar

- Reportes/exportación PDF real.
- Sistema formal de plugins con versionado, permisos y firma.
- Validaciones avanzadas de negocio (costos bloqueados, reglas por tipo de partida).
- Sincronización bidireccional de metrado con tablas de metrado nativas.
- Integración de `mas-android` con gateways reales (local/remoto) compartiendo estado con `mas-core`.

## Siguiente acción recomendada

Implementar **conexión real Android ↔ core**:

1. adaptar `mas-core` a un backend consumible por Android (local API o remoto),
2. reemplazar mock data Android por datos reales de proyectos/presupuestos,
3. reutilizar `mas-shared-ui` para mantener paridad funcional en ambos frentes.
