# MASCostos (base)

Base inicial de una app de presupuestos **similar a Delphin Express** (sin BIM por ahora), con:

- **Kotlin** como lenguaje principal.
- **JavaFX + AtlantaFX** para la UI de escritorio.
- **API pública de plugins/addons** para extender funcionalidades.
- Uso de la base SQL existente: `SQLDelphin_basica.sql`.

## Módulos

- `mas-api`: contratos públicos para plugins y gateways de negocio.
- `mas-core`: dominio, servicios y adaptadores SQLite.
- `mas-desktop`: app JavaFX de escritorio.

## Funcionalidad implementada en esta fase

1. **Registro de plugins** vía `ServiceLoader`.
2. **Casos de negocio base**: creación/listado de proyectos y estimación de presupuesto.
3. **Adaptadores SQLite** para proyecto, items de proyecto y catálogo de costos.
4. **Runner de script SQL** para inicializar la base desde `SQLDelphin_basica.sql`.
5. **Pantalla desktop base** para crear y listar proyectos.

## Siguiente bloque recomendado

- Árbol de partidas/subpartidas.
- Motor de análisis de precios unitarios.
- Reportes/exportación (PDF/Excel).
- Sistema formal de plugins con versionado y firma.
