# Java version policy

Este proyecto debe ejecutarse con **Java 21** (recomendado) o **Java 17**.

## Ejemplos

### Linux/macOS

```bash
export JAVA_HOME=/ruta/a/jdk-21
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew test
```

### Alternativa Java 17

```bash
export JAVA_HOME=/ruta/a/jdk-17
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew test
```

## Nota

Si el entorno usa Java 25+ por defecto, puede fallar la evaluación de scripts Kotlin DSL en ciertas combinaciones de Gradle/Kotlin.
