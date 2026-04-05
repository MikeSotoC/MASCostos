# MASCostos (base)

Base inicial de una app de presupuestos **similar a Delphin Express** (sin BIM por ahora), con:

- **Kotlin** como lenguaje principal.
- **JavaFX + AtlantaFX** para la UI de escritorio.
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
- `mas-desktop`: app JavaFX de escritorio.

## Funcionalidad implementada en esta fase

1. **Registro de plugins** vía `ServiceLoader`.
2. **Casos de negocio base**: creación/listado de proyectos, agregado de ítems y estimación de presupuesto.
3. **Adaptadores SQLite** para proyecto, items de proyecto y catálogo de costos.
4. **Inicialización condicional de SQLite** desde `SQLDelphin_basica.sql`.
5. **Estructura presupuestal por proyecto**: lectura de `titulo` + `costo_unitario` usando `id_proyecto -> presupuesto -> titulo`.
6. **Pantalla desktop base** para crear/listar proyectos, cargar estructura, agregar seleccionado y calcular total.

## Falta implementar

- Árbol jerárquico de títulos/partidas con expand/collapse.
- Edición de metrado por partida.
- Presupuestos múltiples por proyecto (selección explícita de presupuesto).
- Reportes/exportación (PDF/Excel).
- Sistema formal de plugins con versionado, permisos y firma.

## Siguiente acción recomendada

Implementar la **vista jerárquica de estructura presupuestal**:

1. construir árbol `titulo -> costo_unitario`,
2. habilitar selección múltiple y agregado masivo al proyecto,
3. mostrar subtotal por nodo y total incremental en la UI.
