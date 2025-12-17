# Batch Java 25 — Documentación Interna

**Descripción**: Proyecto Spring Boot 4.0 que implementa procesos batch para procesar archivos CSV y una API REST mínima. Utiliza Spring Batch, Spring Data JPA, Hibernate y MySQL/H2 para persistencia de datos.

**Versión**: 0.0.1-SNAPSHOT  
**Java**: JDK 25 (LTS)  
**Spring Boot**: 4.0.0  
**Build Tool**: Maven 3.9.11

---

## 🚀 Quickstart

### Compilar el proyecto

```powershell
# Windows (PowerShell)
cd c:\Users\et61632\Documents\proyecto-batch\workspace\batch-java25
.\mvnw clean package -DskipTests
```

### Ejecutar localmente

#### Opción 1: JAR (Standalone)
```powershell
java -jar target\batch-0.0.1-SNAPSHOT.jar
```

#### Opción 2: Docker (Imagen local)
```powershell
docker build -t batch-java25:latest .
docker run --rm -p 8080:8080 --name batch-app batch-java25:latest
```

#### Opción 3: Docker Compose (App + MySQL)
```powershell
# Copiar configuración de ejemplo
copy .env.example .env

# Iniciar servicios
docker compose up --build

# Detener servicios
docker compose down
```

---

## 📋 Requisitos Previos

### Desarrollo Local
- **Java**: JDK 25.0.1 o superior (compatible con versiones anteriores)
- **Maven**: 3.9.11 o superior (o usar el wrapper `mvnw`)
- **Base de Datos**: MySQL 8.0+ (para producción) o H2 en memoria (para testing)
- **Docker**: Docker Desktop 4.x (opcional, para contenedores)

### Windows
- **PowerShell**: 5.1 o superior
- **Git Bash** o **WSL** (para comandos UNIX-like)

---

## 🔧 Configuración

### Archivo Principal: `src/main/resources/application.properties`

```properties
# Puerto de la aplicación
server.port=8080

# Perfil activo (local o docker)
# spring.profiles.active=local

# Spring Batch
spring.batch.job.enabled=false
spring.batch.jdbc.initialize-schema=always

# Rutas de archivos
file.data.path=src/main/resources/data
file.input=src/main/resources/data/registros.csv
```

### Perfiles de Configuración

- **`application-local.properties`**: Configuración para desarrollo local con H2
- **`application-docker.properties`**: Configuración para ejecución en Docker con MySQL

### Variables de Entorno (`.env`)

```env
MYSQL_HOST=db
MYSQL_PORT=3306
MYSQL_ROOT_PASSWORD=root
MYSQL_DATABASE=batch
MYSQL_USER=batch_user
MYSQL_PASSWORD=batch_password
```

---

## 🌐 Endpoints de la API

### API REST Base
- **URL Base**: `http://localhost:8080`
- **Health Check**: `http://localhost:8080/actuator/health`

### Documentación Interactiva
- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

### Endpoints Funcionales
Consulta la documentación OpenAPI en el Swagger UI para ver los endpoints disponibles.

---

## 📦 Arquitectura del Proyecto

### Estructura de Directorios

```
src/main/java/com/ejemplo/batch/
├── BatchApplication.java          # Punto de entrada
├── config/                        # Configuración Spring
│   ├── OpenApiConfig.java         # Documentación OpenAPI/Swagger
│   └── BatchConfig.java           # Configuración del batch
├── controller/                    # Controladores REST
├── model/                         # Entidades JPA (RegistroCSV)
├── processor/                     # Procesadores de batch
├── repository/                    # Interfaces JPA
├── services/                      # Lógica de negocio
│   └── impl/
│       ├── JobRegistryImpl.java    # Registro y ejecución de jobs
│       └── JobRegistryImplAdvancedTest.java
└── utils/
    └── MessagesLocales.java       # Centralización de mensajes

src/main/resources/
├── application.properties         # Configuración base
├── application-local.properties   # Perfil local
├── application-docker.properties  # Perfil Docker
└── data/
    └── registros.csv              # Datos de prueba
```

### Stack Tecnológico

| Componente | Versión | Propósito |
|-----------|---------|-----------|
| Spring Boot | 4.0.0 | Framework web y batch |
| Spring Batch | Incluido en SB | Procesamiento batch |
| Spring Data JPA | Incluido en SB | Acceso a BD |
| Hibernate | 7.1.8 | ORM |
| MySQL Connector | Última | Driver JDBC MySQL |
| H2 Database | Test | BD en memoria para testing |
| JUnit 5 | 5.10.2 | Framework de testing |
| Mockito | 5.x | Mocking en tests |
| OpenAPI 3 | Swagger UI | Documentación de API |
| JaCoCo | 0.8.13 | Code coverage |

---

## 🧪 Testing

### Ejecutar Tests

```powershell
# Todos los tests
.\mvnw test

# Tests específicos
.\mvnw test -Dtest=MessagesLocalesTest

# Con coverage
.\mvnw test jacoco:report
```

### Cobertura de Tests

- **Total**: 100+ tests unitarios
- **Componentes principales**: JobRegistry, BatchConfig, MessagesLocales
- **Framework**: JUnit 5 (Jupiter) + Mockito
- **Base de datos**: H2 en memoria

---

## 🐳 Docker & Containerización

### Dockerfile

El proyecto utiliza un **multi-stage build**:

```dockerfile
# Stage 1: Build
FROM bellsoft/liberica-openjdk-alpine:25 as builder
# Compila el proyecto

# Stage 2: Runtime
FROM bellsoft/liberica-jre-alpine:25
# Ejecuta la aplicación compilada
```

### Docker Compose

Inicia dos servicios:

```yaml
services:
  db:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: batch
  
  app:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      - db
```

### Construir Imagen

```powershell
docker build -t batch-java25:latest .
```

---

## 📊 Características Principales

### 1. Procesamiento Batch de CSV
- Lectura de archivos CSV
- Validación de registros
- Persistencia en base de datos
- Manejo de errores

### 2. API REST
- Endpoints para ejecutar jobs
- Consulta de registros
- Health checks
- Documentación OpenAPI

### 3. Mensajería Centralizada
- Clase `MessagesLocales` para mensajes uniformes
- Mensajes de éxito (✓) y error (✗)
- Métodos de formateo

### 4. Logging y Monitoreo
- Spring Actuator para health checks
- JaCoCo para code coverage
- Tests exhaustivos

---

## 🔒 Seguridad y Buenas Prácticas

### Credenciales
- **✓ Usar `.env`** para variables sensibles
- **✗ NO subir** `.env` al repositorio
- **Usar** secretos en CI/CD

### Base de Datos (Producción)
- Crear usuario MySQL sin permisos de root
- Usar credenciales fuertes
- Implementar backups regulares

### Docker Compose
- Agregar `healthcheck` para MySQL
- Usar networking interno
- Limitar recursos (memory, cpu)

### Código
- No subir archivos generados (`target/`, `*.class`)
- Usar `.gitignore` apropiadamente
- Mantener código limpio y documentado

---

## 📖 Documentación Adicional

| Documento | Propósito |
|-----------|-----------|
| [INSTALLATION.md](docs/INSTALLATION.md) | Guía detallada de instalación |
| [INTERNAL.md](docs/INTERNAL.md) | Documentación técnica interna |
| [MESSAGESLOCALES.md](docs/MESSAGESLOCALES.md) | Sistema de mensajes centralizados |
| [RESUMEN_MESSAGESLOCALES.md](docs/RESUMEN_MESSAGESLOCALES.md) | Resumen de cambios de centralización |
| [VALIDACION_REGISTROS_BATCH.md](docs/VALIDACION_REGISTROS_BATCH.md) | Reglas de validación de registros |
| [VERIFICACION_FINAL.md](docs/VERIFICACION_FINAL.md) | Verificación y estado del proyecto |
| [DOCUMENTACION_INDEX.md](docs/DOCUMENTACION_INDEX.md) | Índice completo de documentación |
| [ENTREGA_FINAL.md](ENTREGA_FINAL.md) | Resumen final del proyecto |

---

## 🛠️ Comandos Útiles

### Build y Compilación

```powershell
# Compilar sin tests
.\mvnw clean package -DskipTests

# Compilar con tests
.\mvnw clean package

# Limpiar build anterior
.\mvnw clean
```

### Testing

```powershell
# Ejecutar todos los tests
.\mvnw test

# Tests de un módulo específico
.\mvnw test -Dtest=MessagesLocalesTest

# Generar reporte de coverage
.\mvnw jacoco:report
```

### Ejecución

```powershell
# Ejecutar JAR
java -jar target\batch-0.0.1-SNAPSHOT.jar

# Ejecutar con perfil específico
java -jar target\batch-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

### Docker

```powershell
# Construir imagen
docker build -t batch-java25:latest .

# Ejecutar contenedor
docker run -p 8080:8080 batch-java25:latest

# Compose
docker compose up --build
docker compose down
```

---

## 📞 Soporte y Contacto

Para preguntas sobre:

- **Instalación y configuración**: Ver [INSTALLATION.md](docs/INSTALLATION.md)
- **Arquitectura y diseño**: Ver [INTERNAL.md](docs/INTERNAL.md)
- **Sistema de mensajes**: Ver [MESSAGESLOCALES.md](docs/MESSAGESLOCALES.md)
- **Validación de datos**: Ver [VALIDACION_REGISTROS_BATCH.md](docs/VALIDACION_REGISTROS_BATCH.md)
- **Estado del proyecto**: Ver [VERIFICACION_FINAL.md](docs/VERIFICACION_FINAL.md)

---

## 📝 Notas de Desarrollo

- El proyecto usa **Java 25 (LTS)** como versión objetivo
- **Spring Boot 4.0.0** requiere Java 17+
- Tests en memoria con **H2 Database**
- MySQL para persistencia en producción
- **JUnit 5** y **Mockito** para testing
- **Code coverage** con JaCoCo

---

**Última actualización**: 17 de Diciembre de 2025
