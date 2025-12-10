# Instalación y ejecución (local / Docker)

Requisitos previos:
- Java JDK 25 (o JDK compatible si necesita usar otra versión)
- Maven 3.8+ o el wrapper incluido (`mvnw` / `mvnw.cmd`)
- Docker y Docker Compose (si quieres usar contenedores)

1) Compilar y ejecutar localmente (sin Docker)

Windows (PowerShell):
```
cd c:\source\batch-java25
.\mvnw.cmd -B -DskipTests package
java -jar target\batch-0.0.1-SNAPSHOT.jar
```

Nota: El jar resultante se llamará según el `artifactId` y la versión en el `pom.xml`.

2) Crear y ejecutar con Docker (requiere Docker en la máquina)

Construir imagen:
```
cd c:\source\batch-java25
docker build -t batch-java25:latest .
```

Ejecutar con Docker:
```
docker run --rm -p 9090:9090 --name batch-running batch-java25:latest
```

3) Usar `docker-compose` (recomendado para levantar MySQL + app)
```
cd c:\source\batch-java25
copy .env.example .env
docker compose up --build
```

4) Variables y configuración
- Copia `.env.example` a `.env` y ajusta `MYSQL_ROOT_PASSWORD`, `MYSQL_DATABASE` y `MYSQL_ROOT_USERNAME` según tu entorno.
- Las configuraciones del proyecto están en `src/main/resources/application.properties`.

5) Documentación API (Swagger / OpenAPI)
- Después de iniciar la app, accede a la UI de Swagger en:
  `http://localhost:9090/swagger-ui/index.html`  (o `/swagger-ui.html` dependiendo de la versión)
- El endpoint JSON de OpenAPI está en: `http://localhost:9090/v3/api-docs`

Problemas comunes
- Si Docker falla por imagen no encontrada (ej. JDK 25 no disponible), compila localmente con Maven y usa un `Dockerfile` runtime-only.
- No expongas credenciales en repositorios públicos. Usa variables de entorno o secretos.
