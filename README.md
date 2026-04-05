# MASCostos (base)

Base inicial de una app de presupuestos **similar a Delphin Express** (sin BIM por ahora), con:

- **Kotlin** como lenguaje principal.
- **JavaFX + AtlantaFX** para la UI de escritorio.
- **API pública de plugins/addons** para extender funcionalidades.
- Uso de la base SQL existente: `SQLDelphin_basica.sql`.

## Módulos

- `mas-api`: contratos públicos para plugins.
- `mas-core`: dominio, casos de uso y acceso de datos.
- `mas-desktop`: app JavaFX de escritorio.

## Objetivo de esta primera base

1. Definir una arquitectura extensible.
2. Exponer una API estable para terceros.
3. Preparar la integración de datos desde la base SQL actual.
