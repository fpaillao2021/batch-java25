# Batch (proyecto) — Documentación interna

**Descripción:**: Proyecto Spring Boot que implementa procesos batch y una API REST mínima. Usa Spring Batch, Spring Data JPA y MySQL. Incluye integración con Datadog para monitoreo y observabilidad (APM, logs y métricas).

**Rápido (Quickstart)**
- **Compilar localmente:**
  - Windows (PowerShell):
    ```powershell
    cd c:\source\batch-java25
    .\mvnw.cmd -B -DskipTests package
    ```
- **Ejecutar el JAR localmente:**
  ```powershell
  java -jar target\batch-0.0.1-SNAPSHOT.jar
  ```
- **Con Docker (imagen local):**
  ```powershell
  cd c:\source\batch-java25
  docker build -t batch-java25:latest .
  docker run --rm -p 9090:9090 --name batch-running batch-java25:latest
  ```
- **Con Docker Compose (app + MySQL):**
  ```powershell
  cd c:\source\batch-java25
  copy .env.example .env
  docker compose up --build
  ```

**Endpoints útiles**
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

**Configuración importante**
- Archivo principal: `src/main/resources/application.properties`.
- Puerto del servidor: `server.port` (por defecto `9090`).
- Conexión DB: `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password`.
- Spring Batch metadata: `spring.batch.jdbc.initialize-schema` controla creación de tablas de Batch.

**Monitoreo con Datadog**
- **APM (Application Performance Monitoring)**: Rastrea trazas de rendimiento de la aplicación.
- **Logs**: Recopila logs de la aplicación y contenedores.
- **Métricas**: Envía métricas del sistema y aplicación a Datadog.
- Configuración: En `docker-compose.yml`, el servicio `datadog-agent` requiere `DD_API_KEY`. Variables de entorno en `app`: `DD_SERVICE`, `DD_ENV`, `DD_VERSION`.
- Análisis estático: `static-analysis.datadog.yml` para análisis de código con Datadog.

**Contenerización**
- `Dockerfile`: multi-stage para build y runtime (usa Temurin JDK/JRE). Si la etiqueta JDK/imagen no existe en Docker Hub, compila localmente y usa un Dockerfile runtime-only.
- `.dockerignore`: excluye `target`, `.git`, archivos IDE.
- `docker-compose.yml`: levanta `db` (MySQL), `app` (Spring Boot) y `datadog-agent` (para monitoreo). Copia `.env.example` a `.env` antes de levantar.
- **Datadog Integración**: El agente de Datadog está configurado para APM (Application Performance Monitoring), logs y métricas. El Java agent se inyecta automáticamente en la JVM de la aplicación. Requiere una clave API de Datadog configurada en `docker-compose.yml`.

**Qué instalar / configurar en la máquina de desarrollo**
- Java JDK 25 (o compatible si usas otra versión).
- Maven (o usar el wrapper `mvnw`).
- Docker + Docker Desktop (si vas a usar contenedores) y `docker compose`.
- En Windows: PowerShell v5.1 (o Git Bash / WSL para comandos UNIX-like).
- **Datadog**: Clave API de Datadog para monitoreo (configurada en `docker-compose.yml`).

**Buenas prácticas y notas**
- No subir credenciales a Git; usa `.env` o secretos del CI/CD.
- En entornos de producción, crea un usuario MySQL sin permisos de root para la app.
- Añadir un `healthcheck` en `docker-compose.yml` para que `app` espere a que MySQL esté listo.
- Si se necesita compatibilidad con versiones más antiguas de Java, ajustar `pom.xml` `java.version` y etiquetas en `Dockerfile`.

**Dónde mirar para más detalles**
- Guía de instalación y ejecución: `docs/INSTALLATION.md`
- Documentación técnica interna: `docs/INTERNAL.md`
- Configuración OpenAPI: `src/main/java/com/ejemplo/batch/config/OpenApiConfig.java`
- Configuración Datadog: `docker-compose.yml` (servicio `datadog-agent`) y `static-analysis.datadog.yml`

Si quieres, puedo:
- Añadir un `README` más orientado a desarrolladores (cómo ejecutar pruebas, flujos de trabajo comunes).
- Generar un `Dockerfile.runtime` alternativo y ejemplo de `Dockerfile` más ligero.
