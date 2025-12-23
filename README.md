# Batch Java 25 — Sistema de Procesamiento Batch

**Descripción**: Proyecto Spring Boot 4.0.1 que implementa procesos batch para procesar archivos CSV. Utiliza Spring Batch 6.x y se integra con `batch-dl-data-mysql` para persistencia de datos vía API REST.

**Versión**: 0.0.1-SNAPSHOT  
**Java**: JDK 25  
**Spring Boot**: 4.0.1  
**Spring Batch**: 6.0.1  
**Build Tool**: Maven 3.9.11

---

## 🏗️ Arquitectura del Sistema

El sistema está compuesto por **3 proyectos** que trabajan juntos:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           DOCKER COMPOSE                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐       │
│  │                  │    │                  │    │                  │       │
│  │   batch-java25   │───▶│ batch-dl-data-   │───▶│     MySQL 8.0    │       │
│  │    (Port 8080)   │    │     mysql        │    │   (Port 3308)    │       │
│  │                  │    │   (Port 8585)    │    │                  │       │
│  └──────────────────┘    └──────────────────┘    └──────────────────┘       │
│         │                        │                                          │
│         └────────────┬───────────┘                                          │
│                      ▼                                                       │
│         ┌────────────────────────┐                                          │
│         │   batch-entity-dto     │                                          │
│         │      (JAR 1.0.1)       │                                          │
│         └────────────────────────┘                                          │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Proyectos

| Proyecto | Descripción | Puerto |
|----------|-------------|--------|
| **batch-java25** | Procesador Batch (este proyecto) | 8080 |
| **batch-dl-data-mysql** | API REST de persistencia | 8585 |
| **batch-entity-dto** | Librería compartida (JAR) | N/A |

---

## 🚀 Quickstart

### Opción 1: Docker Compose (Recomendado)

```bash
# Ejecutar script de construcción
cd /ruta/al/proyecto/batch-java25
chmod +x docker-build.sh
./docker-build.sh
```

El script automáticamente:
1. Compila `batch-entity-dto`
2. Copia el JAR a ambos proyectos
3. Construye las imágenes Docker
4. Levanta todos los servicios

### Opción 2: Ejecución Local

```bash
# 1. Compilar e instalar batch-entity-dto
cd ../batch-entity-dto
mvn clean install -DskipTests

# 2. Compilar batch-java25
cd ../batch-java25
mvn clean compile -DskipTests

# 3. Iniciar batch-dl-data-mysql (en otra terminal)
cd ../batch-dl-data-mysql
mvn spring-boot:run -Dspring-boot.run.profiles=local

# 4. Iniciar batch-java25
cd ../batch-java25
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

---

## 📋 Requisitos Previos

- **Java**: JDK 25.0.1 o superior
- **Maven**: 3.9.11 o superior
- **Docker**: Docker Desktop 4.x (para contenedores)
- **MySQL**: 8.0+ (si ejecutas localmente sin Docker)

---

## 🔧 Configuración

### Perfiles de Configuración

| Archivo | Uso |
|---------|-----|
| `application.properties` | Configuración base |
| `application-local.properties` | Desarrollo local |
| `application-docker.properties` | Docker Compose |

### Activar Perfil Local

Descomenta en `application.properties`:
```properties
spring.profiles.active=local
```

### Variables de Entorno (Docker)

| Variable | Descripción | Valor por Defecto |
|----------|-------------|-------------------|
| `MYSQL_ROOT_PASSWORD` | Contraseña root MySQL | Evertec.2025 |
| `MYSQL_DATABASE` | Nombre de la BD | spring_batch_db |
| `SPRING_PROFILES_ACTIVE` | Perfil Spring | docker |

---

## 🌐 Endpoints de la API

### batch-java25 (Puerto 8080)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/batch/run/{filename}` | Ejecutar proceso batch |
| GET | `/api/batch/registros` | Obtener todos los registros |
| GET | `/api/batch/registro/{id}` | Obtener registro por ID |

### batch-dl-data-mysql (Puerto 8585)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/mysql/dl/save/mandato` | Guardar lista de registros |
| GET | `/api/mysql/dl/mandato/registros` | Obtener todos los registros |
| GET | `/api/mysql/dl/mandato/registro/{id}` | Obtener registro por ID |

### Documentación Interactiva

- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
- **Health Check**: `http://localhost:8080/actuator/health`

---

## 📦 Estructura del Proyecto

```
batch-java25/
├── src/main/java/com/ejemplo/batch/
│   ├── BatchApplication.java           # Punto de entrada
│   ├── config/
│   │   ├── OpenApiConfig.java          # Configuración Swagger
│   │   └── RestTemplateConfig.java     # Cliente HTTP
│   ├── controller/
│   │   └── BatchController.java        # Endpoints REST
│   ├── processor/
│   │   ├── BatchConfig.java            # Configuración Spring Batch
│   │   ├── RegistroProcessor.java      # Procesador de registros
│   │   └── RestItemWriter.java         # Writer vía API REST
│   └── services/
│       ├── IJobRegistry.java           # Interface de servicio
│       └── impl/
│           └── JobRegistryImpl.java    # Implementación
├── src/main/resources/
│   ├── application.properties
│   ├── application-local.properties
│   ├── application-docker.properties
│   └── data/
│       └── registros.csv               # Datos de prueba
├── script/
│   └── init-mysql.sql                  # Script inicialización BD
├── docs/
│   ├── ARQUITECTURA.md                 # Documentación arquitectura
│   └── arquitectura.drawio             # Diagrama Draw.io
├── docker-compose.yml
├── Dockerfile
├── docker-build.sh                     # Script de construcción
└── pom.xml
```

---

## 🔄 Flujo de Procesamiento Batch

```
┌─────────────┐     ┌─────────────┐     ┌─────────────────────┐
│  CSV File   │────▶│  Reader     │────▶│    Processor        │
│registros.csv│     │FlatFileItem │     │RegistroProcessor    │
└─────────────┘     │   Reader    │     │  (uppercase name)   │
                    └─────────────┘     └──────────┬──────────┘
                                                   │
                                                   ▼
                    ┌─────────────────────────────────────────┐
                    │           RestItemWriter                 │
                    │  POST → batch-dl-data-mysql:8585        │
                    │  /api/mysql/dl/save/mandato             │
                    └─────────────────────────────────────────┘
                                                   │
                                                   ▼
                    ┌─────────────────────────────────────────┐
                    │        batch-dl-data-mysql              │
                    │     RegistroRepository (JPA)            │
                    │              ↓                          │
                    │          MySQL 8.0                      │
                    └─────────────────────────────────────────┘
```

**Características:**
- Lectura de CSV con `FlatFileItemReader` (chunk size: 10)
- Procesamiento: convierte nombres a mayúsculas, agrega timestamp
- Escritura vía API REST (no acceso directo a BD)
- Consultas de registros también vía API REST

---

## 🐳 Docker

### Servicios en Docker Compose

| Servicio | Imagen | Puerto | Descripción |
|----------|--------|--------|-------------|
| db | mysql:8.0 | 3308 | Base de datos |
| batch-dl-data-mysql | Build local | 8585 | API persistencia |
| app | Build local | 8080 | Procesador batch |

### Comandos Docker

```bash
# Construir y levantar (recomendado)
./docker-build.sh

# Ver logs
docker-compose logs -f

# Detener servicios
docker-compose down

# Detener y eliminar volúmenes (reset BD)
docker-compose down -v

# Reconstruir imágenes
docker-compose up --build -d
```

---

## 🧪 Testing

### Ejecutar Tests

```bash
# Todos los tests
mvn test

# Con coverage
mvn test jacoco:report
```

### Probar el Batch

```bash
# Ejecutar batch (requiere servicios levantados)
curl http://localhost:8080/api/batch/run/registros.csv

# Consultar registros procesados
curl http://localhost:8080/api/batch/registros

# Consultar registro específico
curl http://localhost:8080/api/batch/registro/1
```

---

## 📊 Dependencias Principales

| Dependencia | Versión | Propósito |
|-------------|---------|-----------|
| Spring Boot | 4.0.1 | Framework |
| Spring Batch | 6.0.1 | Procesamiento batch |
| batch-entity-dto | 1.0.1 | Entidades y DTOs compartidos |
| MySQL Connector | 8.x | Driver JDBC |
| Lombok | Incluido | Reducción de boilerplate |
| SpringDoc OpenAPI | 2.1.0 | Documentación API |

---

## 📖 Documentación Adicional

| Documento | Descripción |
|-----------|-------------|
| [ARQUITECTURA.md](docs/ARQUITECTURA.md) | Arquitectura completa del sistema |
| [arquitectura.drawio](docs/arquitectura.drawio) | Diagrama visual (Draw.io) |
| [INSTALLATION.md](docs/INSTALLATION.md) | Guía de instalación |
| [INTERNAL.md](docs/INTERNAL.md) | Documentación técnica |
| [MESSAGESLOCALES.md](docs/MESSAGESLOCALES.md) | Sistema de mensajes |

---

## ⚠️ Notas Importantes

### Spring Batch 6.x
En Spring Batch 6.x, los paquetes de `item` cambiaron de ubicación:
- ❌ Antes: `org.springframework.batch.item.*`
- ✅ Ahora: `org.springframework.batch.infrastructure.item.*`

### Dependencia batch-entity-dto
El JAR `batch-entity-dto` **NO está en Maven Central**. Debe compilarse localmente:
```bash
cd ../batch-entity-dto
mvn clean install -DskipTests
```

### Base de Datos
- **Local**: Configurar MySQL en `application-local.properties`
- **Docker**: Se inicializa automáticamente con `script/init-mysql.sql`

---

## 🛠️ Troubleshooting

### Error: "batch-entity-dto not found"
```bash
cd ../batch-entity-dto
mvn clean install -DskipTests
```

### Error: "Connection refused" a batch-dl-data-mysql
Asegúrate de que `batch-dl-data-mysql` esté corriendo:
```bash
# Docker
docker-compose logs batch-dl-data-mysql

# Local
cd ../batch-dl-data-mysql
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Error: "Table doesn't exist"
Reinicia los contenedores eliminando volúmenes:
```bash
docker-compose down -v
./docker-build.sh
```

---

**Última actualización**: 23 de Diciembre de 2025
