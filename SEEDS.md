# Datos de ejemplo (catálogo + proyecto)

La app inicializa automáticamente la base SQLite nueva con datos de ejemplo.

Si existe `SQLDelphin_basica.sql` en el directorio de trabajo del proyecto, ese archivo se usa primero como bootstrap.

Por defecto usa un único archivo: `~/.mascostos/SQLDelphin_basica.sqlite`.


- Catálogo base: `base_catalogo.sql`
- Proyecto demo: `proyecto_ejemplo.sql`

Ubicación de scripts:

- `MAS-Data-Sqlite/src/main/resources/com/uchi/mascostos/data/sqlite/seed/base_catalogo.sql`
- `MAS-Data-Sqlite/src/main/resources/com/uchi/mascostos/data/sqlite/seed/proyecto_ejemplo.sql`

## ¿Cuándo se ejecutan?

En el primer arranque, si el archivo `.db` no existe, `SQLiteConnector` crea el directorio y ejecuta el script de seed correspondiente.

## Cobertura actual del catálogo semilla

Incluye data de construcción civil para:

- Obras preliminares
- Movimiento de tierras
- Concreto simple y armado
- Encofrado y acero
- Albañilería y tarrajeo
- Instalaciones sanitarias
- Instalaciones eléctricas
- Acabados

Con partidas, insumos, detalles y precios de ejemplo para pruebas funcionales más realistas.

## ¿Cómo regenerar en local?

1. Cierra la app.
2. Elimina `~/.mascostos/SQLDelphin_basica.sqlite` (o la ruta configurada por variables/props).
3. Abre la app nuevamente.

Se recrearán con el catálogo y proyecto de ejemplo.
