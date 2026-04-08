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
- `mas-android`: app Android Compose con la misma estructura funcional de UI base (SQLite local por defecto, remoto opcional).
- `mas-backend`: API HTTP (Ktor) para exponer `mas-core` a clientes externos (incluyendo Android).

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
10. **Android UI paralela** con módulos visuales equivalentes y estilo Material 3 base.
11. **Lógica UI compartida** entre Desktop/Android para totalización y convención de reportes.
12. **Contrato de aplicación unificado** (`BudgetAppService`) con implementación core y mock Android.
13. **Backend HTTP Ktor** con endpoints de proyectos, presupuestos, catálogo, ítems y export CSV.
14. **Android conectado al backend real** (`RemoteBudgetAppService`) con manejo básico de errores de conexión.
15. **Cliente Android robustecido**: reintentos, timeouts configurables, cache GET corta y token Bearer opcional.
16. **Exportación PDF real** disponible en backend/core y clientes Desktop/Android.
17. **Hardening de plugins/validaciones**: validación de firma/versionado para carga de plugins y controles de negocio extra en alta de ítems.
18. **UX fase 2 parcial**: Android con skeletons de carga y configuración de endpoint/token/reintentos vía `BuildConfig`.
19. **Backend por rol (opcional)**: modo de enforcement por encabezado `X-Role` para edición/reportes.
20. **Cache local básica Android**: snapshot JSON en `SharedPreferences` para arranque con datos offline.
21. **Modo local-first**: Android y Desktop trabajan con SQLite local; remoto queda como opción de integración.
22. **UI profesional por secciones**: Android y Desktop dejaron el flujo de “pantalla única” y ahora separan navegación por etapas (Proyectos / Catálogo / Ítems-Reportes).

## Sistema de configuraciones (Android + Windows/Desktop)

- **Android** (via `BuildConfig` en `mas-android/build.gradle.kts`):
  - `USE_REMOTE` (`false` por defecto),
  - `API_BASE_URL`,
  - `API_TOKEN`,
  - `API_MAX_RETRIES`.
- **Desktop/Windows** (via `mascostos.properties` opcional + variables de entorno):
  - `mode=local|remote` (`MASCOSTOS_MODE`),
  - `sqlite.path` (`MASCOSTOS_SQLITE`),
  - `schema.path` (`MASCOSTOS_SCHEMA`),
  - `remote.baseUrl` (`MASCOSTOS_REMOTE_BASE_URL`).

## Falta implementar

- Completar tokens visuales y tematización profunda (modo oscuro, tipografía de marca, componentes dedicados).
- Evolucionar la caché local Android (actualmente snapshot JSON) a almacenamiento estructurado (Room/SQLDelight).
- Evolucionar plugins formales a un manifiesto firmado con validación criptográfica real.
- Completar reglas avanzadas por tipo de partida, costos bloqueados y controles por usuario/rol.
- Sincronización bidireccional de metrado con tablas de metrado nativas.

## Siguiente acción recomendada

Continuar con **sprint UX + confiabilidad (fase 2)**:

1. completar skeleton/loading states y flujos de error accionables,
2. consolidar sistema de diseño compartido en componentes reutilizables,
3. agregar autenticación por usuario/rol y cache persistente para trabajo offline.
