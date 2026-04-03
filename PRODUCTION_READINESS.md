# Revisión de madurez para producción (MASCostos)

Fecha de revisión: 2026-04-03.

## Resumen ejecutivo

El repositorio tiene una base modular clara (`MAS-Core`, `MAS-App`, `MAS-Data-Sqlite`, `MAS-UI-Desktop`) y separación inicial por capas, pero **aún no está listo para producción**. Las principales brechas son: ausencia de pruebas automatizadas, configuración rígida de base de datos, falta de manejo de errores/observabilidad, y ausencia de pipeline de build/release.

## Hallazgos principales

### 1) Build y ejecución reproducible

- El wrapper de Gradle no está completo en el repositorio (no se encuentra `gradle/wrapper/*`), por lo que no hay build reproducible “one-command” para cualquier entorno.
- El script `gradlew` existe, pero no está ejecutable en este entorno por permisos.

**Impacto:** riesgo alto en CI/CD y onboarding (cada entorno compila distinto o no compila).

### 2) Configuración y portabilidad

- Las rutas SQLite están hardcodeadas a una ruta de Windows con usuario local (`C:/Users/Admin/...`).

**Impacto:** bloqueo inmediato fuera del equipo original (Linux/macOS, otra máquina Windows, contenedores, CI).

### 3) Calidad y pruebas

- Los módulos declaran dependencias de test, pero no hay fuentes de pruebas (`src/test`) en el repositorio.
- No se evidencia cobertura de pruebas unitarias, integración o regresión.

**Impacto:** riesgo alto de regresiones funcionales y baja confianza para releases frecuentes.

### 4) Robustez transaccional y consistencia

- Repositorios con operaciones de varias escrituras no muestran transacciones explícitas.
- Hay patrones de “insertar y luego consultar” para devolver entidad creada, susceptibles a condiciones de carrera si escalan concurrencia/usuarios.

**Impacto:** riesgo medio/alto de inconsistencias de datos ante fallos parciales.

### 5) Manejo de errores y observabilidad

- No se observa estrategia homogénea de manejo de excepciones (p. ej. mapeo de errores técnicos a errores de dominio/UI).
- No se observan logs estructurados, métricas ni trazas.

**Impacto:** operación en producción difícil (diagnóstico lento, MTTR alto).

### 6) Seguridad y gobernanza

- No hay señales de controles básicos de seguridad de supply chain y compliance en el repo (SBOM/SCA, escaneo de dependencias, políticas de secretos).
- No se observa gestión por ambientes (dev/qa/prod) ni inyección de configuración sensible.

**Impacto:** riesgo medio/alto para auditoría y operación empresarial.

### 7) Arquitectura y empaquetado

- Arquitectura modular correcta para crecer, pero faltan piezas operativas: documentación de despliegue, versionado de esquema/migraciones, estrategia de backup/restore y compatibilidad.

**Impacto:** escalamiento operativo limitado.

## Qué falta para “nivel producción” (priorizado)

### P0 (bloqueantes, 1–2 semanas)

1. **Completar cadena de build reproducible**
   - Incluir Gradle Wrapper completo (`gradle-wrapper.jar` + properties) y asegurar permisos de `gradlew`.
2. **Externalizar configuración**
   - Reemplazar rutas hardcodeadas por variables de entorno/archivo de configuración por ambiente.
3. **Base mínima de pruebas**
   - Unit tests para servicios de `MAS-App` y repositorios críticos.
   - Smoke test de arranque de app.
4. **Manejo de errores mínimo**
   - Política unificada de errores (infraestructura → aplicación → UI).

### P1 (alta prioridad, 2–4 semanas)

1. **Transacciones y atomicidad** en casos de uso de múltiples escrituras.
2. **Observabilidad**
   - Logging estructurado (niveles, contexto, correlation id).
   - Métricas operativas básicas.
3. **CI/CD**
   - Pipeline con: build, test, lint, análisis estático y artefacto versionado.
4. **Migraciones de DB**
   - Definir herramienta y versionado de esquema (Flyway/Liquibase o equivalente).

### P2 (madurez, 1–2 meses)

1. **Endurecimiento de seguridad**
   - SCA/SAST, revisión de dependencias, firma de artefactos.
2. **Documentación operativa completa**
   - Runbook, recuperación ante fallos, backups, monitoreo.
3. **Pruebas no funcionales**
   - Carga, estabilidad, recuperación.

## Checklist sugerido de salida a producción

- [ ] Build reproducible en entorno limpio con un comando.
- [ ] Configuración por ambiente sin valores hardcodeados sensibles.
- [ ] Cobertura mínima acordada (ej. 60% en dominio/aplicación).
- [ ] Pipeline CI/CD bloqueando merges sin checks.
- [ ] Logs y métricas visibles en entorno de operación.
- [ ] Estrategia de migraciones y rollback de DB validada.
- [ ] Runbook y procedimiento de backup/restore probados.

## Conclusión

MASCostos tiene una buena base de modularidad, pero hoy está más cerca de un **MVP interno** que de un sistema productivo. Si se ejecutan los puntos P0 y P1, el proyecto podría alcanzar una primera versión “production-ready” para un entorno controlado.
