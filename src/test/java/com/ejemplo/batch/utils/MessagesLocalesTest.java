package com.ejemplo.batch.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas unitarias para MessagesLocales")
class MessagesLocalesTest {

    // ============================================
    // TESTS PARA MENSAJES DE ÉXITO
    // ============================================

    @Test
    @DisplayName("MensajeLocal.BATCH_EJECUTADO_EXITOSAMENTE debe contener el texto correcto")
    void testBatchEjecutadoExitosamente() {
        // Assert
        assertNotNull(MessagesLocales.MensajeLocal.BATCH_EJECUTADO_EXITOSAMENTE);
        assertTrue(MessagesLocales.MensajeLocal.BATCH_EJECUTADO_EXITOSAMENTE.contains("✓"));
        assertTrue(MessagesLocales.MensajeLocal.BATCH_EJECUTADO_EXITOSAMENTE.contains("exitosamente"));
        assertTrue(MessagesLocales.MensajeLocal.BATCH_EJECUTADO_EXITOSAMENTE.contains("archivo"));
    }

    @Test
    @DisplayName("MensajeLocal.ARCHIVO_CSV_ENCONTRADO debe tener el formato correcto")
    void testArchivoCsvEncontrado() {
        // Assert
        assertNotNull(MessagesLocales.MensajeLocal.ARCHIVO_CSV_ENCONTRADO);
        assertTrue(MessagesLocales.MensajeLocal.ARCHIVO_CSV_ENCONTRADO.contains("✓"));
        assertTrue(MessagesLocales.MensajeLocal.ARCHIVO_CSV_ENCONTRADO.contains("encontrado"));
    }

    @Test
    @DisplayName("MensajeLocal.TAMAÑO_ARCHIVO debe estar definido")
    void testTamañoArchivo() {
        // Assert
        assertNotNull(MessagesLocales.MensajeLocal.TAMAÑO_ARCHIVO);
        assertTrue(MessagesLocales.MensajeLocal.TAMAÑO_ARCHIVO.contains("✓"));
        assertTrue(MessagesLocales.MensajeLocal.TAMAÑO_ARCHIVO.contains("Tamaño"));
    }

    @Test
    @DisplayName("MensajeLocal.BYTES debe ser 'bytes'")
    void testBytes() {
        // Assert
        assertNotNull(MessagesLocales.MensajeLocal.BYTES);
        assertEquals(" bytes", MessagesLocales.MensajeLocal.BYTES);
    }

    @Test
    @DisplayName("MensajeLocal.JOB_REGISTRADO debe estar definido")
    void testJobRegistrado() {
        // Assert
        assertNotNull(MessagesLocales.MensajeLocal.JOB_REGISTRADO);
        assertTrue(MessagesLocales.MensajeLocal.JOB_REGISTRADO.contains("Registrando"));
    }

    @Test
    @DisplayName("MensajeLocal.OPERACION_EXITOSA debe contener indicador de éxito")
    void testOperacionExitosa() {
        // Assert
        assertNotNull(MessagesLocales.MensajeLocal.OPERACION_EXITOSA);
        assertTrue(MessagesLocales.MensajeLocal.OPERACION_EXITOSA.contains("✓"));
        assertTrue(MessagesLocales.MensajeLocal.OPERACION_EXITOSA.contains("exitosamente"));
    }

    @Test
    @DisplayName("MensajeLocal.API_TITULO debe ser 'Batch API'")
    void testApiTitulo() {
        // Assert
        assertNotNull(MessagesLocales.MensajeLocal.API_TITULO);
        assertEquals("Batch API", MessagesLocales.MensajeLocal.API_TITULO);
    }

    @Test
    @DisplayName("MensajeLocal.API_VERSION debe ser 'v0.0.1'")
    void testApiVersion() {
        // Assert
        assertNotNull(MessagesLocales.MensajeLocal.API_VERSION);
        assertEquals("v0.0.1", MessagesLocales.MensajeLocal.API_VERSION);
    }

    @Test
    @DisplayName("MensajeLocal.API_DESCRIPCION debe estar definida")
    void testApiDescripcion() {
        // Assert
        assertNotNull(MessagesLocales.MensajeLocal.API_DESCRIPCION);
        assertTrue(MessagesLocales.MensajeLocal.API_DESCRIPCION.contains("OpenAPI"));
    }

    @Test
    @DisplayName("MensajeLocal.CSV_READER debe ser 'csvReader'")
    void testCsvReader() {
        // Assert
        assertNotNull(MessagesLocales.MensajeLocal.CSV_READER);
        assertEquals("csvReader", MessagesLocales.MensajeLocal.CSV_READER);
    }

    @Test
    @DisplayName("MensajeLocal.CSV_IMPORT_STEP debe ser 'csvImportStep'")
    void testCsvImportStep() {
        // Assert
        assertNotNull(MessagesLocales.MensajeLocal.CSV_IMPORT_STEP);
        assertEquals("csvImportStep", MessagesLocales.MensajeLocal.CSV_IMPORT_STEP);
    }

    @Test
    @DisplayName("MensajeLocal.IMPORT_CSV_JOB debe ser 'importCsvJob'")
    void testImportCsvJob() {
        // Assert
        assertNotNull(MessagesLocales.MensajeLocal.IMPORT_CSV_JOB);
        assertEquals("importCsvJob", MessagesLocales.MensajeLocal.IMPORT_CSV_JOB);
    }

    // ============================================
    // TESTS PARA MENSAJES DE ERROR
    // ============================================

    @Test
    @DisplayName("ErrorMensajeLocal.ERROR_ARCHIVO_NO_EXISTE debe tener indicador de error")
    void testErrorArchivoNoExiste() {
        // Assert
        assertNotNull(MessagesLocales.ErrorMensajeLocal.ERROR_ARCHIVO_NO_EXISTE);
        assertTrue(MessagesLocales.ErrorMensajeLocal.ERROR_ARCHIVO_NO_EXISTE.contains("✗"));
        assertTrue(MessagesLocales.ErrorMensajeLocal.ERROR_ARCHIVO_NO_EXISTE.contains("ERROR"));
    }

    @Test
    @DisplayName("ErrorMensajeLocal.ERROR_ARCHIVO_CSV_NO_EXISTE debe estar definido")
    void testErrorArchivoCsvNoExiste() {
        // Assert
        assertNotNull(MessagesLocales.ErrorMensajeLocal.ERROR_ARCHIVO_CSV_NO_EXISTE);
        assertTrue(MessagesLocales.ErrorMensajeLocal.ERROR_ARCHIVO_CSV_NO_EXISTE.contains("✗"));
    }

    @Test
    @DisplayName("ErrorMensajeLocal.ERROR_PERMISOS_LECTURA debe contener mensaje de permisos")
    void testErrorPermisosLectura() {
        // Assert
        assertNotNull(MessagesLocales.ErrorMensajeLocal.ERROR_PERMISOS_LECTURA);
        assertTrue(MessagesLocales.ErrorMensajeLocal.ERROR_PERMISOS_LECTURA.contains("✗"));
        assertTrue(MessagesLocales.ErrorMensajeLocal.ERROR_PERMISOS_LECTURA.contains("permisos"));
    }

    @Test
    @DisplayName("ErrorMensajeLocal.ERROR_FILENAME_VACIO debe estar definido")
    void testErrorFilenameVacio() {
        // Assert
        assertNotNull(MessagesLocales.ErrorMensajeLocal.ERROR_FILENAME_VACIO);
        assertTrue(MessagesLocales.ErrorMensajeLocal.ERROR_FILENAME_VACIO.contains("✗"));
        assertTrue(MessagesLocales.ErrorMensajeLocal.ERROR_FILENAME_VACIO.contains("vacío"));
    }

    @Test
    @DisplayName("ErrorMensajeLocal.ERROR_EJECUTAR_JOB debe contener texto correcto")
    void testErrorEjecutarJob() {
        // Assert
        assertNotNull(MessagesLocales.ErrorMensajeLocal.ERROR_EJECUTAR_JOB);
        assertTrue(MessagesLocales.ErrorMensajeLocal.ERROR_EJECUTAR_JOB.contains("✗"));
        assertTrue(MessagesLocales.ErrorMensajeLocal.ERROR_EJECUTAR_JOB.contains("Job"));
    }

    @Test
    @DisplayName("ErrorMensajeLocal.ERROR_GENERAL debe tener indicador de error")
    void testErrorGeneral() {
        // Assert
        assertNotNull(MessagesLocales.ErrorMensajeLocal.ERROR_GENERAL);
        assertTrue(MessagesLocales.ErrorMensajeLocal.ERROR_GENERAL.contains("✗"));
        assertTrue(MessagesLocales.ErrorMensajeLocal.ERROR_GENERAL.contains("ERROR"));
    }

    @Test
    @DisplayName("ErrorMensajeLocal.ERROR_BASE_DATOS debe estar definido")
    void testErrorBaseDatos() {
        // Assert
        assertNotNull(MessagesLocales.ErrorMensajeLocal.ERROR_BASE_DATOS);
        assertTrue(MessagesLocales.ErrorMensajeLocal.ERROR_BASE_DATOS.contains("✗"));
        assertTrue(MessagesLocales.ErrorMensajeLocal.ERROR_BASE_DATOS.contains("base de datos"));
    }

    @Test
    @DisplayName("ErrorMensajeLocal.ERROR_PARAMETROS_INVALIDOS debe estar definido")
    void testErrorParametrosInvalidos() {
        // Assert
        assertNotNull(MessagesLocales.ErrorMensajeLocal.ERROR_PARAMETROS_INVALIDOS);
        assertTrue(MessagesLocales.ErrorMensajeLocal.ERROR_PARAMETROS_INVALIDOS.contains("✗"));
    }

    @Test
    @DisplayName("ErrorMensajeLocal.ERROR_TIMEOUT debe estar definido")
    void testErrorTimeout() {
        // Assert
        assertNotNull(MessagesLocales.ErrorMensajeLocal.ERROR_TIMEOUT);
        assertTrue(MessagesLocales.ErrorMensajeLocal.ERROR_TIMEOUT.contains("✗"));
        assertTrue(MessagesLocales.ErrorMensajeLocal.ERROR_TIMEOUT.contains("tiempo límite"));
    }

    @Test
    @DisplayName("ErrorMensajeLocal.ERROR_USUARIO_NO_AUTORIZADO debe estar definido")
    void testErrorUsuarioNoAutorizado() {
        // Assert
        assertNotNull(MessagesLocales.ErrorMensajeLocal.ERROR_USUARIO_NO_AUTORIZADO);
        assertTrue(MessagesLocales.ErrorMensajeLocal.ERROR_USUARIO_NO_AUTORIZADO.contains("✗"));
    }

    // ============================================
    // TESTS PARA MÉTODOS AUXILIARES
    // ============================================

    @Test
    @DisplayName("formatMensaje debe concatenar template y valor correctamente")
    void testFormatMensaje() {
        // Arrange
        String template = "Mensaje: ";
        String valor = "Test";

        // Act
        String resultado = MessagesLocales.formatMensaje(template, valor);

        // Assert
        assertNotNull(resultado);
        assertEquals("Mensaje: Test", resultado);
        assertTrue(resultado.contains(template));
        assertTrue(resultado.contains(valor));
    }

    @Test
    @DisplayName("formatMensaje con valor nulo debe manejarse")
    void testFormatMensajeConValorNulo() {
        // Arrange
        String template = "Error: ";
        String valor = null;

        // Act
        String resultado = MessagesLocales.formatMensaje(template, valor);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.contains("Error: "));
        assertTrue(resultado.contains("null"));
    }

    @Test
    @DisplayName("formatError debe funcionar con plantilla y valor")
    void testFormatError() {
        // Arrange
        String template = MessagesLocales.ErrorMensajeLocal.ERROR_GENERAL;
        String valor = "Test error";

        // Act
        String resultado = MessagesLocales.formatError(template, valor);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.contains("✗"));
        assertTrue(resultado.contains("Test error"));
    }

    @Test
    @DisplayName("formatErrorMultiple debe concatenar múltiples valores")
    void testFormatErrorMultiple() {
        // Arrange
        String template1 = "El archivo '";
        String valor1 = "archivo.txt";
        String template2 = "' no existe en '";
        String valor2 = "/home/user";
        String template3 = "'";

        // Act
        String resultado = MessagesLocales.formatErrorMultiple(template1, valor1, template2, valor2, template3);

        // Assert
        assertNotNull(resultado);
        assertEquals("El archivo 'archivo.txt' no existe en '/home/user'", resultado);
        assertTrue(resultado.contains("archivo.txt"));
        assertTrue(resultado.contains("/home/user"));
    }

    // ============================================
    // TESTS DE VALIDACIÓN GENERAL
    // ============================================

    @Test
    @DisplayName("Todos los mensajes de éxito deben ser no nulos")
    void testTodasLasConstantesNoNulas() {
        // Assert - Mensajes de éxito
        assertNotNull(MessagesLocales.MensajeLocal.BATCH_EJECUTADO_EXITOSAMENTE);
        assertNotNull(MessagesLocales.MensajeLocal.ARCHIVO_CSV_ENCONTRADO);
        assertNotNull(MessagesLocales.MensajeLocal.JOB_REGISTRADO);

        // Assert - Mensajes de error
        assertNotNull(MessagesLocales.ErrorMensajeLocal.ERROR_ARCHIVO_NO_EXISTE);
        assertNotNull(MessagesLocales.ErrorMensajeLocal.ERROR_FILENAME_VACIO);
        assertNotNull(MessagesLocales.ErrorMensajeLocal.ERROR_EJECUTAR_JOB);
    }

    @Test
    @DisplayName("Los mensajes de error deben tener indicador ✗")
    void testTodosLosErroresTienenIndicador() {
        // Assert
        assertTrue(MessagesLocales.ErrorMensajeLocal.ERROR_ARCHIVO_NO_EXISTE.contains("✗"));
        assertTrue(MessagesLocales.ErrorMensajeLocal.ERROR_FILENAME_VACIO.contains("✗"));
        assertTrue(MessagesLocales.ErrorMensajeLocal.ERROR_EJECUTAR_JOB.contains("✗"));
        assertTrue(MessagesLocales.ErrorMensajeLocal.ERROR_GENERAL.contains("✗"));
        assertTrue(MessagesLocales.ErrorMensajeLocal.ERROR_BASE_DATOS.contains("✗"));
    }

    @Test
    @DisplayName("Los principales mensajes de éxito deben tener indicador ✓")
    void testMensajesExitosaTienenIndicador() {
        // Assert
        assertTrue(MessagesLocales.MensajeLocal.BATCH_EJECUTADO_EXITOSAMENTE.contains("✓"));
        assertTrue(MessagesLocales.MensajeLocal.ARCHIVO_CSV_ENCONTRADO.contains("✓"));
        assertTrue(MessagesLocales.MensajeLocal.OPERACION_EXITOSA.contains("✓"));
    }

    @Test
    @DisplayName("Todos los mensajes deben ser strings no vacías")
    void testTodosLosStringNoVacios() {
        // Assert
        assertNotEquals("", MessagesLocales.MensajeLocal.BATCH_EJECUTADO_EXITOSAMENTE);
        assertNotEquals("", MessagesLocales.MensajeLocal.API_TITULO);
        assertNotEquals("", MessagesLocales.ErrorMensajeLocal.ERROR_GENERAL);
    }

    @Test
    @DisplayName("Formato de mensaje con argumentos debe ser consistente")
    void testFormatosConsistentes() {
        // Assert
        String archivo = "test.csv";
        String mensaje = MessagesLocales.formatMensaje(
            MessagesLocales.MensajeLocal.BATCH_EJECUTADO_EXITOSAMENTE, 
            archivo
        );
        
        assertTrue(mensaje.contains("✓"));
        assertTrue(mensaje.contains("exitosamente"));
        assertTrue(mensaje.contains(archivo));
    }

    @Test
    @DisplayName("Constantes de nombres de pasos y jobs deben ser válidos")
    void testConstantesDeNombresValidas() {
        // Assert
        assertNotNull(MessagesLocales.MensajeLocal.CSV_READER);
        assertNotNull(MessagesLocales.MensajeLocal.CSV_IMPORT_STEP);
        assertNotNull(MessagesLocales.MensajeLocal.IMPORT_CSV_JOB);
        
        assertTrue(MessagesLocales.MensajeLocal.CSV_READER.contains("Reader"));
        assertTrue(MessagesLocales.MensajeLocal.CSV_IMPORT_STEP.contains("Step"));
        assertTrue(MessagesLocales.MensajeLocal.IMPORT_CSV_JOB.contains("Job"));
    }

    @Test
    @DisplayName("Mensajes de inicialización deben estar completos")
    void testMensajesIniciacion() {
        // Assert
        assertNotNull(MessagesLocales.MensajeLocal.INICIANDO_BATCH);
        assertNotNull(MessagesLocales.MensajeLocal.OPERACION_COMPLETADA);
        assertNotNull(MessagesLocales.MensajeLocal.METODO_FINALIZADO);
        
        assertTrue(MessagesLocales.MensajeLocal.INICIANDO_BATCH.length() > 0);
        assertTrue(MessagesLocales.MensajeLocal.OPERACION_COMPLETADA.length() > 0);
    }
}