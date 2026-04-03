# Java version policy

Este proyecto funciona con **Java 17** (default recomendado para compatibilidad) y **Java 21** (opcional).

## Configuración por Gradle toolchain

El valor por defecto está en `gradle.properties`:

```properties
mascostos.java.toolchain=17
```

Si quieres usar Java 21, puedes sobreescribir en CLI:

```bash
./gradlew test -Pmascostos.java.toolchain=21
```

## Recomendación para IDE (IntelliJ)

Si aparece el error `Project source sets cannot be resolved` por toolchain:

1. Asegura tener JDK 17 instalado localmente.
2. Configura el Gradle JVM del proyecto a JDK 17.
3. Reimporta el proyecto Gradle.

## Nota

Se eliminó el bloqueo estricto de versión en `settings.gradle.kts` para evitar fallos de sincronización del IDE y permitir que el toolchain configurado gobierne la compilación.
