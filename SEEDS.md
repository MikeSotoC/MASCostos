# Datos de ejemplo (catálogo + proyecto)

La app ahora inicializa automáticamente bases SQLite nuevas con datos de ejemplo:

- Catálogo base: `base_catalogo.sql`
- Proyecto demo: `proyecto_ejemplo.sql`

Ubicación de scripts:

- `MAS-Data-Sqlite/src/main/resources/com/uchi/mascostos/data/sqlite/seed/base_catalogo.sql`
- `MAS-Data-Sqlite/src/main/resources/com/uchi/mascostos/data/sqlite/seed/proyecto_ejemplo.sql`

## ¿Cuándo se ejecutan?

En el primer arranque, si el archivo `.db` no existe, `SQLiteConnector` crea el directorio y ejecuta el script de seed correspondiente.

## ¿Cómo regenerar en local?

1. Cierra la app.
2. Elimina los archivos DB en `~/.mascostos` (o en la ruta configurada por variables/props).
3. Abre la app nuevamente.

Se recrearán con el catálogo y proyecto de ejemplo.
