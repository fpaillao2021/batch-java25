# MessagesLocales - Sistema Centralizado de Mensajes

## Introducción

`MessagesLocales` es una clase de utilidad que centraliza todos los mensajes del sistema en un único lugar. Proporciona acceso a mensajes de éxito/información y mensajes de error a través de interfaces consistentes.

## Estructura

### 1. Clase Principal: MessagesLocales

**Ubicación**: `src/main/java/com/ejemplo/batch/utils/MessagesLocales.java`

La clase contiene dos clases internas estáticas:

#### 1.1 MensajeLocal - Mensajes de Éxito e Información

Mensajes positivos e informativos con indicador ✓

**Constantes disponibles:**

| Constante | Valor |
|-----------|-------|
| `BATCH_EJECUTADO_EXITOSAMENTE` | "✓ El Job de Batch ha sido ejecutado exitosamente con el archivo: " |
| `ARCHIVO_CSV_ENCONTRADO` | "✓ Archivo CSV encontrado: " |
| `TAMAÑO_ARCHIVO` | "✓ Tamaño del archivo: " |
| `BYTES` | " bytes" |
| `JOB_REGISTRADO` | "Registrando el job: " |
| `OPERACION_EXITOSA` | "✓ Operación completada exitosamente" |
| `API_TITULO` | "Batch API" |
| `API_VERSION` | "v0.0.1" |
| `API_DESCRIPCION` | "Documentación OpenAPI para el proyecto batch" |
| `CSV_READER` | "csvReader" |
| `CSV_IMPORT_STEP` | "csvImportStep" |
| `IMPORT_CSV_JOB` | "importCsvJob" |
| `RUTA_ARCHIVO_CSV` | "Ruta del archivo CSV: " |

#### 1.2 ErrorMensajeLocal - Mensajes de Error

Mensajes de error con indicador ✗

**Constantes disponibles:**

| Constante | Valor |
|-----------|-------|
| `ERROR_ARCHIVO_NO_EXISTE` | "✗ ERROR: El archivo '" |
| `ERROR_ARCHIVO_NO_EXISTE_RUTA` | "' no existe en la carpeta '" |
| `ERROR_ARCHIVO_NO_EXISTE_SUFIJO` | "'/'" |
| `ERROR_ARCHIVO_CSV_NO_EXISTE` | "✗ El archivo CSV no existe en la ruta: " |
| `ERROR_PERMISOS_LECTURA_CSV` | "✗ ERROR: No hay permisos de lectura para el archivo '" |
| `ERROR_FILENAME_VACIO` | "✗ ERROR: El nombre del archivo no puede estar vacío" |
| `ERROR_EJECUTAR_JOB` | "✗ ERROR al ejecutar el Job: " |
| `ERROR_GENERAL` | "✗ ERROR: " |
| `ERROR_BASE_DATOS` | "✗ ERROR: Error al acceder a la base de datos: " |
| `ERROR_TIMEOUT` | "✗ ERROR: La operación ha excedido el tiempo límite" |
| `ERROR_USUARIO_NO_AUTORIZADO` | "✗ ERROR: Usuario no autorizado para ejecutar esta operación" |
| `ERROR_VALIDACION_ARCHIVO` | "✗ ERROR en la validación del archivo: " |
| `ERROR_PARAMETROS_INVALIDOS` | "✗ ERROR: Parámetros inválidos: " |

## Métodos de Utilidad

### formatMensaje(String template, String valor)
Concatena un template de mensaje con un valor dinámico.

```java
// Ejemplo
String resultado = MessagesLocales.formatMensaje(
    MessagesLocales.MensajeLocal.ARCHIVO_CSV_ENCONTRADO,
    "datos.csv"
);
// Resultado: "✓ Archivo CSV encontrado: datos.csv"
```

### formatError(String template, String valor)
Formatea mensajes de error con contenido dinámico.

```java
// Ejemplo
String error = MessagesLocales.formatError(
    MessagesLocales.ErrorMensajeLocal.ERROR_GENERAL,
    "Error de conexión"
);
// Resultado: "✗ ERROR: Error de conexión"
```

### formatErrorMultiple(String t1, String v1, String t2, String v2, String t3)
Formatea mensajes de error multi-segmento.

```java
// Ejemplo
String error = MessagesLocales.formatErrorMultiple(
    MessagesLocales.ErrorMensajeLocal.ERROR_ARCHIVO_NO_EXISTE,
    "archivo.csv",
    MessagesLocales.ErrorMensajeLocal.ERROR_ARCHIVO_NO_EXISTE_RUTA,
    "/data",
    MessagesLocales.ErrorMensajeLocal.ERROR_ARCHIVO_NO_EXISTE_SUFIJO
);
// Resultado: "✗ ERROR: El archivo 'archivo.csv' no existe en la carpeta '/data'/'"
```

## Uso en el Código

### Importar la clase

```java
import com.ejemplo.batch.utils.MessagesLocales;
```

### Usar mensajes de éxito

```java
// En JobRegistryImpl.java
public String runBatchJob(String filename) throws Exception {
    try {
        // ... código del job
        return MessagesLocales.MensajeLocal.BATCH_EJECUTADO_EXITOSAMENTE + filename;
    } catch (Exception e) {
        return MessagesLocales.ErrorMensajeLocal.ERROR_EJECUTAR_JOB + e.getMessage();
    }
}
```

### Usar mensajes de error

```java
// En BatchConfig.java
if (!fileExists) {
    throw new IllegalArgumentException(
        MessagesLocales.ErrorMensajeLocal.ERROR_ARCHIVO_CSV_NO_EXISTE + pathToFile
    );
}
```

### Usar métodos de formateo

```java
String formattedError = MessagesLocales.formatErrorMultiple(
    MessagesLocales.ErrorMensajeLocal.ERROR_ARCHIVO_NO_EXISTE,
    "data.csv",
    MessagesLocales.ErrorMensajeLocal.ERROR_ARCHIVO_NO_EXISTE_RUTA,
    "/home/data",
    MessagesLocales.ErrorMensajeLocal.ERROR_ARCHIVO_NO_EXISTE_SUFIJO
);

System.out.println(formattedError);
// ✗ ERROR: El archivo 'data.csv' no existe en la carpeta '/home/data'/'
```

## Archivos Integrados

La clase `MessagesLocales` está integrada en los siguientes archivos:

### 1. [JobRegistryImpl.java](../src/main/java/com/ejemplo/batch/services/impl/JobRegistryImpl.java)
- **Cambios**: 8 referencias de mensajes centralizadas
- **Métodos afectados**: `runBatchJob()`, `registerJob()`

### 2. [BatchConfig.java](../src/main/java/com/ejemplo/batch/processor/BatchConfig.java)
- **Cambios**: 7 referencias de mensajes centralizadas
- **Métodos afectados**: `csvItemReader()`, `csvImportStep()`, `importCsvJob()`

### 3. [OpenApiConfig.java](../src/main/java/com/ejemplo/batch/config/OpenApiConfig.java)
- **Cambios**: 3 referencias de mensajes centralizadas
- **Métodos afectados**: `openApi()`

### 4. [BatchApplication.java](../src/main/java/com/ejemplo/batch/BatchApplication.java)
- **Cambios**: Import preparada para futura integración

## Testing

### Clase de Tests: MessagesLocalesTest

**Ubicación**: `src/test/java/com/ejemplo/batch/utils/MessagesLocalesTest.java`

Contiene 30 pruebas unitarias que validan:

#### Tests de Mensajes de Éxito (11 tests)
- Validación de cada constante `MensajeLocal`
- Verificación de indicadores ✓
- Validación de contenido específico

#### Tests de Mensajes de Error (9 tests)
- Validación de cada constante `ErrorMensajeLocal`
- Verificación de indicadores ✗
- Validación de contenido específico

#### Tests de Métodos de Utilidad (4 tests)
- `testFormatMensaje()` - Concatenación básica
- `testFormatMensajeConValorNulo()` - Manejo de valores nulos
- `testFormatError()` - Formateo de errores
- `testFormatErrorMultiple()` - Formateo multi-segmento

#### Tests de Validación General (6 tests)
- Verificación de no nulidad de constantes
- Validación de indicadores consistentes
- Validación de strings no vacíos
- Validación de formatos consistentes

### Ejecución de Tests

```bash
# Ejecutar solo MessagesLocalesTest
mvnw test -Dtest=MessagesLocalesTest

# Ejecutar todos los tests incluyendo MessagesLocalesTest
mvnw test
```

## Ventajas

1. **Centralización**: Todos los mensajes en un único lugar, facilita mantenimiento
2. **Reutilización**: Evita duplicación de mensajes en el código
3. **Consistencia**: Garantiza mensajes uniformes con indicadores visuales (✓ y ✗)
4. **Internacionalización**: Base para implementar multi-idioma en el futuro
5. **Fácil modificación**: Cambiar mensajes no requiere modificar múltiples archivos
6. **Testing**: 30 pruebas garantizan la integridad de los mensajes

## Mejoras Futuras

1. **Internacionalización (i18n)**: Extender a múltiples idiomas con ResourceBundle
2. **Logging integrado**: Incorporar logging de mensajes automáticamente
3. **Persistencia**: Almacenar mensajes en base de datos para gestión dinámica
4. **Parámetros avanzados**: Soportar mensajes con múltiples parámetros dinámicos
5. **Severidad de mensajes**: Agregar niveles de severidad (INFO, WARN, ERROR, CRITICAL)

## Convenciones

### Nomenclatura de Constantes
- **Mensajes de éxito**: `DESCRIPCION_DEL_MENSAJE`
- **Mensajes de error**: `ERROR_DESCRIPCION_DEL_ERROR`
- **Nombres técnicos**: Sin prefijo adicional (ej: `CSV_READER`, `IMPORT_CSV_JOB`)

### Indicadores Visuales
- **Éxito/Información**: Prefijo `✓` (checkmark)
- **Error**: Prefijo `✗` (cross mark)
- **Sin indicador**: Nombres de trabajos, campos técnicos

### Formato de Mensajes
- Estructura: `[INDICADOR] MENSAJE: [PLANTILLA PARA VALOR]`
- Espacios finales para concatenación: `"Mensaje: "` (con espacio al final)
- Mensajes completos: Sin espacios finales
