# Arquitectura del Sistema Batch

## Visión General

El sistema está compuesto por **3 proyectos** que trabajan juntos para procesar archivos CSV y persistir datos en MySQL.

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
│         │                        │                       │                  │
│         │                        │                       │                  │
│         ▼                        ▼                       ▼                  │
│  ┌──────────────────────────────────────────────────────────────────┐      │
│  │                      batch-entity-dto (JAR)                       │      │
│  │              Entidades, DTOs, Mappers, Mensajes                   │      │
│  └──────────────────────────────────────────────────────────────────┘      │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Proyectos

### 1. batch-entity-dto (Librería Compartida)
**Ubicación:** `../batch-entity-dto`  
**Tipo:** JAR Library  
**Versión:** 1.0.1

#### Contenido:
- `RegistroCSV` - Entidad JPA
- `RegistroCSVDTO` - Data Transfer Object
- `EntityDTOMapper` - Conversión Entity ↔ DTO
- `MessagesLocales` - Mensajes centralizados del sistema

#### Dependencias:
- Spring Data JPA
- Lombok
- Jakarta Persistence

---

### 2. batch-java25 (Procesador Batch)
**Ubicación:** `../batch-java25`  
**Puerto:** 8080  
**Perfil Docker:** `docker`

#### Responsabilidades:
- Procesar archivos CSV usando Spring Batch
- Leer registros del CSV (`FlatFileItemReader`)
- Procesar registros (`RegistroProcessor`)
- Enviar datos a `batch-dl-data-mysql` vía REST API (`RestItemWriter`)
- Consultar registros procesados vía REST API

#### Flujo de Datos:
```
CSV File → FlatFileItemReader → RegistroProcessor → RestItemWriter → REST API
```

#### Endpoints:
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/batch/run/{filename}` | Ejecutar proceso batch |
| GET | `/api/batch/registros` | Obtener todos los registros |
| GET | `/api/batch/registro/{id}` | Obtener registro por ID |

#### Dependencias:
- Spring Boot 4.0.1
- Spring Batch 6.0.1
- batch-entity-dto 1.0.1
- RestTemplate (para llamadas HTTP)

---

### 3. batch-dl-data-mysql (API de Persistencia)
**Ubicación:** `../batch-dl-data-mysql`  
**Puerto:** 8585  
**Perfil Docker:** `docker`

#### Responsabilidades:
- Exponer API REST para operaciones CRUD
- Persistir datos en MySQL usando JPA
- Escanear entidades del JAR externo `batch-entity-dto`

#### Endpoints:
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/mysql/dl/save/mandato` | Guardar lista de registros |
| GET | `/api/mysql/dl/mandato/registros` | Obtener todos los registros |
| GET | `/api/mysql/dl/mandato/registro/{id}` | Obtener registro por ID |

#### Dependencias:
- Spring Boot 4.0.1
- Spring Data JPA
- MySQL Connector
- batch-entity-dto 1.0.1

---

## Base de Datos MySQL

**Contenedor:** `batch-mysql`  
**Puerto Externo:** 3308  
**Puerto Interno:** 3306  
**Base de Datos:** `spring_batch_db`

### Tablas:

#### Tabla de Negocio:
- `registrocsv` - Datos procesados del CSV

#### Tablas de Spring Batch (Metadatos):
- `BATCH_JOB_INSTANCE`
- `BATCH_JOB_EXECUTION`
- `BATCH_JOB_EXECUTION_PARAMS`
- `BATCH_JOB_EXECUTION_CONTEXT`
- `BATCH_STEP_EXECUTION`
- `BATCH_STEP_EXECUTION_CONTEXT`
- `BATCH_JOB_INSTANCE_SEQ`
- `BATCH_JOB_EXECUTION_SEQ`
- `BATCH_STEP_EXECUTION_SEQ`

---

## Flujo de Integración

```
                                    ┌─────────────────┐
                                    │   Usuario/API   │
                                    └────────┬────────┘
                                             │
                                             ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                            batch-java25 (:8080)                              │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                         Spring Batch Job                             │    │
│  │  ┌─────────────┐    ┌─────────────┐    ┌─────────────────────────┐  │    │
│  │  │ CSV Reader  │───▶│  Processor  │───▶│    RestItemWriter       │  │    │
│  │  │ (10 items)  │    │ (uppercase) │    │ (POST to API)           │  │    │
│  │  └─────────────┘    └─────────────┘    └───────────┬─────────────┘  │    │
│  └────────────────────────────────────────────────────┼────────────────┘    │
└───────────────────────────────────────────────────────┼─────────────────────┘
                                                        │
                                                        │ HTTP POST
                                                        │ /api/mysql/dl/save/mandato
                                                        ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                        batch-dl-data-mysql (:8585)                           │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                         REST Controller                              │    │
│  │  ┌─────────────┐    ┌─────────────┐    ┌─────────────────────────┐  │    │
│  │  │ BatchController│──▶│  BatchImp   │───▶│  RegistroRepository   │  │    │
│  │  │             │    │  (Service)  │    │  (JPA)                  │  │    │
│  │  └─────────────┘    └─────────────┘    └───────────┬─────────────┘  │    │
│  └────────────────────────────────────────────────────┼────────────────┘    │
└───────────────────────────────────────────────────────┼─────────────────────┘
                                                        │
                                                        │ JDBC
                                                        ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                            MySQL 8.0 (:3308)                                 │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                        spring_batch_db                               │    │
│  │  ┌─────────────────┐    ┌────────────────────────────────────────┐  │    │
│  │  │   registrocsv   │    │   BATCH_JOB_* (Spring Batch tables)   │  │    │
│  │  └─────────────────┘    └────────────────────────────────────────┘  │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Configuración Docker Compose

### Servicios:
1. **db** - MySQL 8.0
2. **batch-dl-data-mysql** - API de persistencia
3. **app** - Procesador batch

### Red:
- `spring-batch-network` (bridge)

### Volúmenes:
- `mysql_data` - Persistencia de datos MySQL

### Orden de Inicio:
1. MySQL (healthcheck)
2. batch-dl-data-mysql (depends_on: db)
3. batch-java25 (depends_on: db, batch-dl-data-mysql)

---

## Variables de Entorno

| Variable | Descripción | Valor por Defecto |
|----------|-------------|-------------------|
| `MYSQL_ROOT_PASSWORD` | Contraseña root MySQL | Evertec.2025 |
| `MYSQL_DATABASE` | Nombre de la BD | spring_batch_db |
| `MYSQL_ROOT_USERNAME` | Usuario root | root |
| `SPRING_PROFILES_ACTIVE` | Perfil Spring | docker |

---

## Comandos Útiles

```bash
# Construir y levantar servicios
./docker-build.sh

# Ver logs
docker-compose logs -f

# Detener servicios
docker-compose down

# Detener y eliminar volúmenes
docker-compose down -v

# Ejecutar batch
curl http://localhost:8080/api/batch/run/registros.csv

# Consultar registros
curl http://localhost:8080/api/batch/registros
```

---

## Archivos de Configuración

| Proyecto | Archivo | Uso |
|----------|---------|-----|
| batch-java25 | application.properties | Configuración base |
| batch-java25 | application-local.properties | Desarrollo local |
| batch-java25 | application-docker.properties | Docker |
| batch-dl-data-mysql | application.properties | Configuración base |
| batch-dl-data-mysql | application-local.properties | Desarrollo local |
| batch-dl-data-mysql | application-docker.properties | Docker |

