package com.ejemplo.batch.utils;

/**
 * Clase centralizada para gestionar todos los mensajes del sistema.
 * Proporciona mensajes de éxito, información y errores de forma centralizada.
 * 
 * Estructura:
 * - MessagesLocales.MensajeLocal: Mensajes de éxito e información
 * - MessagesLocales.ErrorMensajeLocal: Mensajes de error
 * 
 * Ejemplo de uso:
 *   String mensaje = MessagesLocales.BATCH_EJECUTADO_EXITOSAMENTE;
 *   String error = MessagesLocales.ERROR_ARCHIVO_NO_EXISTE;
 */
public class MessagesLocales {

    // ============================================
    // MENSAJES DE ÉXITO E INFORMACIÓN
    // ============================================

    /**
     * Mensajes de éxito y operaciones completadas
     */
    public static class MensajeLocal {

        // Batch
        public static final String BATCH_EJECUTADO_EXITOSAMENTE = 
            "✓ El Job de Batch ha sido ejecutado exitosamente con el archivo: ";
        
        public static final String ARCHIVO_CSV_ENCONTRADO = 
            "✓ Archivo CSV encontrado: ";
        
        public static final String TAMAÑO_ARCHIVO = 
            "✓ Tamaño del archivo: ";
        
        public static final String BYTES = 
            " bytes";

        public static final String JOB_REGISTRADO = 
            "Registrando el job: ";

        public static final String OPERACION_EXITOSA = 
            "✓ Operación completada exitosamente";

        public static final String INICIANDO_BATCH = 
            "Iniciando el método Demo Batch-Java25.";

        public static final String OPERACION_COMPLETADA = 
            "Operación completada exitosamente. Demo Batch-Java25.";

        public static final String METODO_FINALIZADO = 
            "Método hacerAlgo finalizado. Demo Batch-Java25.";

        public static final String API_TITULO = 
            "Batch API";

        public static final String API_VERSION = 
            "v0.0.1";

        public static final String API_DESCRIPCION = 
            "Documentación OpenAPI para el proyecto batch";

        public static final String CSV_READER = 
            "csvReader";

        public static final String CSV_IMPORT_STEP = 
            "csvImportStep";

        public static final String IMPORT_CSV_JOB = 
            "importCsvJob";

        public static final String RUTA_ARCHIVO_CSV = 
            "Ruta del archivo CSV: ";

        public static final String REGISTRO_LISTA_VACIA = 
            "No hay registros disponibles";

        public static final String REGISTRO_ENCONTRADO = 
            "Registro encontrado exitosamente";
    }

    // ============================================
    // MENSAJES DE ERROR
    // ============================================

    /**
     * Mensajes de error del sistema
     */
    public static class ErrorMensajeLocal {

        // Errores de archivo
        public static final String ERROR_ARCHIVO_NO_EXISTE = 
            "✗ ERROR: El archivo '";
        
        public static final String ERROR_ARCHIVO_NO_EXISTE_RUTA = 
            "' no existe en la carpeta '";
        
        public static final String ERROR_ARCHIVO_NO_EXISTE_SUFIJO = 
            "/'";

        public static final String ERROR_ARCHIVO_CSV_NO_EXISTE = 
            "✗ El archivo CSV no existe en la ruta: ";

        public static final String ERROR_PERMISOS_LECTURA = 
            "✗ ERROR: No hay permisos de lectura para el archivo '";
        
        public static final String ERROR_PERMISOS_LECTURA_SUFIJO = 
            "'";

        public static final String ERROR_PERMISOS_LECTURA_CSV = 
            "✗ No hay permisos de lectura para el archivo: ";

        // Errores de validación
        public static final String ERROR_FILENAME_VACIO = 
            "✗ ERROR: El nombre del archivo no puede estar vacío";

        public static final String ERROR_VALIDACION_ARCHIVO = 
            "✗ ERROR: ";

        public static final String ERROR_INESPERADO_VALIDAR = 
            "✗ ERROR inesperado al validar el archivo: ";

        // Errores de ejecución de Job
        public static final String ERROR_EJECUTAR_JOB = 
            "✗ ERROR al ejecutar el Job: ";

        // Errores generales
        public static final String ERROR_GENERAL = 
            "✗ ERROR: ";

        public static final String ERROR_INESPERADO = 
            "✗ Error inesperado: ";

        public static final String ERROR_BASE_DATOS = 
            "✗ ERROR: Error al acceder a la base de datos: ";

        public static final String ERROR_PARAMETROS_INVALIDOS = 
            "✗ ERROR: Parámetros inválidos: ";

        public static final String ERROR_REGISTRO_NO_ENCONTRADO = 
            "✗ ERROR: Registro no encontrado con ID: ";

        public static final String ERROR_OPERACION_NO_PERMITIDA = 
            "✗ ERROR: Operación no permitida";

        public static final String ERROR_TIMEOUT = 
            "✗ ERROR: La operación ha excedido el tiempo límite";

        public static final String ERROR_ARCHIVO_DAÑADO = 
            "✗ ERROR: El archivo está dañado o es inválido";

        // Errores de usuario
        public static final String ERROR_USUARIO_NO_AUTORIZADO = 
            "✗ ERROR: Usuario no autorizado para ejecutar esta operación";

        public static final String ERROR_DATOS_INCOMPLETOS = 
            "✗ ERROR: Los datos proporcionados están incompletos";
    }

    // ============================================
    // UTILIDADES PARA FORMATEAR MENSAJES
    // ============================================

    /**
     * Formatea un mensaje de éxito con un valor dinámico
     * @param template Plantilla del mensaje (ej: BATCH_EJECUTADO_EXITOSAMENTE)
     * @param valor Valor dinámico a insertar
     * @return Mensaje formateado
     */
    public static String formatMensaje(String template, String valor) {
        return template + valor;
    }

    /**
     * Formatea un mensaje de error con un valor dinámico
     * @param template Plantilla del error (ej: ERROR_ARCHIVO_NO_EXISTE)
     * @param valor Valor dinámico a insertar
     * @return Error formateado
     */
    public static String formatError(String template, String valor) {
        return template + valor;
    }

    /**
     * Formatea un mensaje de error con múltiples valores
     * @param template1 Primera parte de la plantilla
     * @param valor1 Primer valor dinámico
     * @param template2 Segunda parte de la plantilla
     * @param valor2 Segundo valor dinámico
     * @param template3 Tercera parte de la plantilla (opcional)
     * @return Error formateado
     */
    public static String formatErrorMultiple(String template1, String valor1, String template2, String valor2, String template3) {
        return template1 + valor1 + template2 + valor2 + template3;
    }
}
