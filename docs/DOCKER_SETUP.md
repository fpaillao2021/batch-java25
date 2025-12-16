# Docker Setup - Guía de Ejecución

## 📋 Descripción General

La aplicación Spring Boot Batch está configurada para ejecutarse en Docker con Docker Compose. El stack incluye:
- **MySQL 8.0**: Base de datos para Spring Batch y datos procesados
- **Spring Boot App**: Aplicación batch Java 25 con Spring Boot 4.0.0

---

## 🏗️ Estructura Docker

### **Dockerfile (Multi-stage build)**

```dockerfile
BUILD STAGE (eclipse-temurin:25-jdk)
  ↓
  - Instala Maven
  - Copia pom.xml y src/
  - Ejecuta: mvn clean package
  - Genera: target/batch-0.0.1-SNAPSHOT.jar
  ↓
RUNTIME STAGE (eclipse-temurin:25-jre)
  ↓
  - Copia JAR desde build stage
  - Copia carpeta data/registros.csv
  - Expone puerto 8080
  - Ejecuta: java -jar /app/app.jar
```

### **docker-compose.yml (Orquestación)**

```
spring-batch-network (bridge)
  ├── db (MySQL 8.0)
  │   ├── Puerto: 3308 (host) → 3306 (container)
  │   ├── Usuario: root
  │   ├── Contraseña: Evertec.2025
  │   ├── Database: spring_batch_db
  │   └── Volumen: mysql_data (persistente)
  │
  └── app (Spring Boot)
      ├── Puerto: 8080 (host) → 8080 (container)
      ├── Espera a que db esté healthy
      └── Volumen: ./data → /app/data
```

---

## 🚀 Ejecución

### **Opción 1: Ejecutar todo con Docker Compose**

```bash
# 1. Ir a la carpeta del proyecto
cd batch-java25

# 2. Construir la imagen y levantar los servicios
docker-compose up -d

# 3. Ver estado de los servicios
docker-compose ps

# 4. Ver logs de la aplicación
docker-compose logs -f app

# 5. Ver logs de la base de datos
docker-compose logs -f db
```

**Salida esperada:**
```
batch-mysql   Up (healthy)
batch-app     Up (running)
```

---

## 📊 Verificación de Funcionamiento

### **1. Verificar que la app está corriendo**

```bash
# Ver si el contenedor está activo
docker ps | grep batch-app

# Ver logs de la app
docker-compose logs app | tail -20
```

**Esperado:**
```
Started BatchApplication in 5.632 seconds (process running for 6.149)
Tomcat started on port 8080
```

### **2. Probar endpoints REST**

```bash
# Obtener registros procesados
curl http://localhost:8080/api/batch/registros

# Ejecutar batch con un archivo
curl -X POST http://localhost:8080/api/batch/ejecutar/registros.csv

# Obtener un registro específico
curl http://localhost:8080/api/batch/registros/1
```

### **3. Verificar base de datos**

```bash
# Entrar a MySQL dentro del contenedor
docker exec -it batch-mysql mysql -uroot -pEvertec.2025 spring_batch_db

# Ver registros procesados
mysql> SELECT COUNT(*) FROM registro_csv;

# Ver metadata de jobs
mysql> SELECT * FROM batch_job_execution ORDER BY START_TIME DESC LIMIT 1;

# Salir
mysql> exit;
```

---

## 🔧 Configuración de Rutas

### **Carpeta data/ - Localización del CSV**

**En tu máquina local:**
```
batch-java25/
  ├── data/
  │   └── registros.csv          ← Archivo que procesa la app
  ├── docker-compose.yml
  ├── Dockerfile
  ...
```

**Dentro del contenedor Docker:**
```
/app/
  ├── app.jar                     ← Aplicación compilada
  ├── data/
  │   └── registros.csv          ← Mapeado desde ./data (local)
```

**En el código Java:**
```java
// JobRegistryImpl.java
String filepath = "src/main/resources/data/" + filename;

// En Docker, se resuelve como:
// "src/main/resources/data/registros.csv"
// → /app/data/registros.csv (porque Dockerfile lo copia)
```

---

## 🌐 Variables de Entorno

El `docker-compose.yml` configura automáticamente:

| Variable | Valor | Propósito |
|----------|-------|----------|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://db:3306/...` | Conexión a MySQL usando nombre del servicio |
| `SPRING_DATASOURCE_USERNAME` | `root` | Usuario de base de datos |
| `SPRING_DATASOURCE_PASSWORD` | `Evertec.2025` | Contraseña de base de datos |
| `SPRING_BATCH_JOB_ENABLED` | `false` | No ejecutar job automáticamente |
| `SPRING_BATCH_JDBC_INITIALIZE_SCHEMA` | `always` | Crear tablas de batch si no existen |

---

## ⚙️ Configuración Dockerfile

### **Cambios Importantes:**

#### 1. **Copia de carpeta data/**
```dockerfile
# Copy data folder for CSV processing
COPY data/ /app/data/
```
✅ Garantiza que registros.csv esté disponible en el contenedor

#### 2. **Variables de entorno**
```dockerfile
ENV SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/...
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=Evertec.2025
```
✅ Configura conexión a base de datos para Docker

#### 3. **Multi-stage build**
```dockerfile
FROM eclipse-temurin:25-jdk AS build    # Etapa 1: Compilación
FROM eclipse-temurin:25-jre             # Etapa 2: Runtime (más pequeño)
```
✅ Reduce tamaño de la imagen final (sin Maven)

---

## 📦 Tamaño de Imagen

```bash
# Ver tamaño de la imagen construida
docker images | grep batch

# Ejemplo de salida esperada:
# batch-java25-app   latest   450MB
```

**Desglose:**
- `eclipse-temurin:25-jre`: ~350 MB
- `Spring Boot JAR`: ~40 MB
- `Otros archivos`: ~10 MB
- **Total**: ~400-450 MB

---

## 🛑 Detener y Limpiar

```bash
# Detener servicios (mantiene datos)
docker-compose down

# Detener y eliminar todo (incluye volúmenes de datos)
docker-compose down -v

# Eliminar la imagen
docker rmi batch-java25-app

# Limpiar todo (contenedores, imágenes, volúmenes sin usar)
docker system prune -a
```

---

## 🐛 Troubleshooting

### **Error: "No qualifying bean of type 'FlatFileItemReader'"**

**Causa**: El archivo CSV no existe en la ruta esperada

**Solución**:
```bash
# Verificar que data/registros.csv existe
ls -la data/

# Verificar que está en el contenedor
docker exec batch-app ls -la /app/data/
```

### **Error: "Unable to connect to MySQL"**

**Causa**: La aplicación intenta conectarse antes de que MySQL esté listo

**Solución**: Dockerfile y docker-compose.yml ya incluyen `healthcheck` y `depends_on`
```yaml
depends_on:
  db:
    condition: service_healthy    # Espera a que MySQL esté saludable
```

### **Error: "Connection refused on port 8080"**

**Causa**: La aplicación no está corriendo o no expone el puerto

**Solución**:
```bash
# Verificar que el contenedor está corriendo
docker ps | grep batch-app

# Ver logs
docker-compose logs app

# Verificar puertos mapeados
docker port batch-app
```

### **CSV no se procesa en Docker**

**Causa**: El archivo está en la carpeta local pero Docker no tiene acceso

**Solución**: El `docker-compose.yml` ya mapea `./data:/app/data`:
```yaml
volumes:
  - ./data:/app/data        # Asegura que local ./data es accesible
```

---

## 📝 Flujo de Ejecución Completo

```
┌────────────────────────────────────────────────────┐
│  1. docker-compose up -d                           │
│  Construye imagen y levanta servicios              │
└────────────┬─────────────────────────────────────┘
             │
             ▼
┌────────────────────────────────────────────────────┐
│  2. MySQL inicia y espera conexiones               │
│  - Crea base de datos spring_batch_db              │
│  - Ejecuta script crear-base-datos.sql             │
│  - Expone puerto 3308                              │
└────────────┬─────────────────────────────────────┘
             │
             ▼
┌────────────────────────────────────────────────────┐
│  3. Spring Boot App inicia                         │
│  - Espera a que MySQL esté healthy                 │
│  - Carga JAR desde /app/app.jar                    │
│  - Conecta a base de datos db:3306                 │
│  - Expone puerto 8080                              │
└────────────┬─────────────────────────────────────┘
             │
             ▼
┌────────────────────────────────────────────────────┐
│  4. Aplicación lista para recibir requests         │
│  - POST /api/batch/ejecutar/registros.csv          │
│  - GET /api/batch/registros                        │
│  - GET /api/batch/registros/{id}                   │
└────────────────────────────────────────────────────┘
```

---

## 📋 Checklist de Verificación

- [ ] `data/registros.csv` existe en la carpeta local
- [ ] `Dockerfile` copia la carpeta `data/`
- [ ] `docker-compose.yml` mapea volúmenes correctamente
- [ ] Variables de entorno en docker-compose.yml coinciden con application.properties
- [ ] `healthcheck` está configurado en el servicio `db`
- [ ] `depends_on` con `condition: service_healthy` está en el servicio `app`
- [ ] Puerto 3308 no está en uso en la máquina local
- [ ] Puerto 8080 no está en uso en la máquina local

---

## 🎯 Comandos Útiles

```bash
# Ver estado de todos los servicios
docker-compose ps

# Ver logs en tiempo real
docker-compose logs -f

# Ejecutar comando dentro del contenedor app
docker exec batch-app curl http://localhost:8080/api/batch/registros

# Ejecutar comando dentro de MySQL
docker exec batch-mysql mysql -uroot -pEvertec.2025 -e "SELECT COUNT(*) FROM spring_batch_db.registro_csv"

# Reconstruir la imagen
docker-compose build --no-cache

# Reiniciar servicios
docker-compose restart

# Eliminar y recrear todo
docker-compose down -v && docker-compose up -d
```

---

## ✅ Configuración Final

**Estado actual:**
- ✅ Dockerfile: Multi-stage, copia data/, configura Java 25
- ✅ docker-compose.yml: MySQL + Spring Boot, healthcheck, volumes mapeados
- ✅ Variables de entorno: Configuradas para Docker
- ✅ Rutas: Coherentes entre local y Docker

**Listo para ejecutar en Docker:**
```bash
docker-compose up -d
```

---

**Última actualización**: 16 de Diciembre de 2025  
**Docker**: Compatible con Java 25  
**MySQL**: 8.0 con persistencia de datos
