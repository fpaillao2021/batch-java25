# Documentación interna: Configuración y detalles del proyecto

Estructura relevante del proyecto:
- `src/main/java/com/ejemplo/batch` : código fuente Java
  - `controller/` : controladores REST
  - `processor/` : configuración y procesadores batch
  - `repository/` : repositorios JPA
  - `model/` : entidades y modelos
- `src/main/resources/application.properties` : configuración de la aplicación
- `Dockerfile`, `docker-compose.yml`, `.env.example` : containerización y despliegue local

Propiedades importantes en `application.properties`:
- `server.port` : puerto del servidor (por defecto `9090`)
- `spring.datasource.url` : URL de conexión a la base de datos
- `spring.datasource.username` / `spring.datasource.password` : credenciales DB
- `spring.batch.job.enabled` : controlar la ejecución automática de jobs (false por defecto)

Cómo funciona la integración con la base de datos:
- La app usa Spring Data JPA y `mysql-connector-j`.
- Spring Batch usa la misma base de datos para sus metadatos. `spring.batch.jdbc.initialize-schema=always` crea las tablas necesarias para Batch.

Swagger / OpenAPI
- Se añadió `springdoc-openapi-starter-webmvc-ui` y una clase `OpenApiConfig` en `com.ejemplo.batch.config` que define título/versión.
- Endpoints útiles:
  - UI: `/swagger-ui/index.html`
  - JSON: `/v3/api-docs`

Notas operativas
- Evitar usar `root` en producción para la base de datos; crear un usuario con permisos limitados.
- Considerar agregar `healthcheck` en `docker-compose` para que el servicio `app` espere a que MySQL esté listo.
- Para entornos CI/CD, construir el JAR en el pipeline y usar un Docker image runtime-only para despliegues.

Archivos a revisar si algo deja de funcionar:
- `pom.xml` (dependencias y java.version)
- `Dockerfile` (etiquetas de imagen JDK/Maven pueden necesitar ajustes)
- `docker-compose.yml` (variables de entorno y dependencias)
