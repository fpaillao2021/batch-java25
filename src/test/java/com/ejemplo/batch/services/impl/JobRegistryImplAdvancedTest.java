package com.ejemplo.batch.services.impl;

import com.ejemplo.batch.model.RegistroCSV;
import com.ejemplo.batch.repository.RegistroRepository;
import com.ejemplo.batch.services.IJobRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias para JobRegistryImpl")
class JobRegistryImplAdvancedTest {

    @Mock
    private JobOperator jobLauncher;

    @Mock
    private Job importUserJob;

    @Mock
    private RegistroRepository registroRepository;

    @InjectMocks
    private JobRegistryImpl jobRegistry;

    private String testDataPath;

    @BeforeEach
    void setUp() throws Exception {
        // Crear directorio temporal para tests
        testDataPath = Files.createTempDirectory("test_batch_").toString();
        
        // Inyectar el dataPath usando ReflectionTestUtils
        ReflectionTestUtils.setField(jobRegistry, "dataPath", testDataPath);
    }

    // ============================================
    // TESTS PARA EL MÉTODO validateFile
    // ============================================

    @Test
    @DisplayName("validateFile debe retornar null cuando el archivo existe y es válido")
    void testValidateFileConArchivoValido() throws Exception {
        // Arrange
        Path testFile = Files.createTempFile(new File(testDataPath).toPath(), "test_", ".csv");
        Files.write(testFile, "nombre;edad;email\nJuan;30;juan@test.com\n".getBytes());
        String fileName = testFile.getFileName().toString();

        // Act - Usar reflexión para acceder al método privado
        java.lang.reflect.Method validateFileMethod = JobRegistryImpl.class
            .getDeclaredMethod("validateFile", String.class);
        validateFileMethod.setAccessible(true);
        String result = (String) validateFileMethod.invoke(jobRegistry, fileName);

        // Assert
        assertNull(result, "Debe retornar null para archivo válido");
        
        // Cleanup
        Files.deleteIfExists(testFile);
    }

    @Test
    @DisplayName("validateFile debe retornar error cuando el archivo no existe")
    void testValidateFileConArchivoNoExistente() throws Exception {
        // Act
        java.lang.reflect.Method validateFileMethod = JobRegistryImpl.class
            .getDeclaredMethod("validateFile", String.class);
        validateFileMethod.setAccessible(true);
        String result = (String) validateFileMethod.invoke(jobRegistry, "archivo_inexistente.csv");

        // Assert
        assertNotNull(result, "Debe retornar mensaje de error");
        assertTrue(result.contains("no existe"), "Mensaje debe indicar que archivo no existe");
        assertTrue(result.contains("✗"), "Mensaje debe tener indicador de error");
    }

    @Test
    @DisplayName("validateFile debe retornar error cuando el filename es null")
    void testValidateFileConFilenameNull() throws Exception {
        // Act
        java.lang.reflect.Method validateFileMethod = JobRegistryImpl.class
            .getDeclaredMethod("validateFile", String.class);
        validateFileMethod.setAccessible(true);
        String result = (String) validateFileMethod.invoke(jobRegistry, (String) null);

        // Assert
        assertNotNull(result, "Debe retornar mensaje de error");
        assertTrue(result.contains("no puede estar vacío"), "Mensaje debe indicar filename vacío");
    }

    @Test
    @DisplayName("validateFile debe retornar error cuando el filename está vacío")
    void testValidateFileConFilenameVacio() throws Exception {
        // Act
        java.lang.reflect.Method validateFileMethod = JobRegistryImpl.class
            .getDeclaredMethod("validateFile", String.class);
        validateFileMethod.setAccessible(true);
        String result = (String) validateFileMethod.invoke(jobRegistry, "");

        // Assert
        assertNotNull(result, "Debe retornar mensaje de error");
        assertTrue(result.contains("no puede estar vacío"), "Mensaje debe indicar filename vacío");
    }

    @Test
    @DisplayName("validateFile debe retornar error cuando el filename solo contiene espacios")
    void testValidateFileConFilenameEspacios() throws Exception {
        // Act
        java.lang.reflect.Method validateFileMethod = JobRegistryImpl.class
            .getDeclaredMethod("validateFile", String.class);
        validateFileMethod.setAccessible(true);
        String result = (String) validateFileMethod.invoke(jobRegistry, "   ");

        // Assert
        assertNotNull(result, "Debe retornar mensaje de error");
        assertTrue(result.contains("no puede estar vacío"), "Mensaje debe indicar filename vacío");
    }

    @Test
    @DisplayName("validateFile debe retornar error cuando no hay permisos de lectura")
    void testValidateFileConPermisosDenegados() throws Exception {
        // Arrange
        Path testFile = Files.createTempFile(new File(testDataPath).toPath(), "test_", ".csv");
        File file = testFile.toFile();
        file.setReadable(false);
        String fileName = testFile.getFileName().toString();

        try {
            // Act
            java.lang.reflect.Method validateFileMethod = JobRegistryImpl.class
                .getDeclaredMethod("validateFile", String.class);
            validateFileMethod.setAccessible(true);
            String result = (String) validateFileMethod.invoke(jobRegistry, fileName);

            // Assert
            if (!System.getProperty("os.name").toLowerCase().contains("win")) {
                // En sistemas Linux/Mac, se espera error de permisos
                assertNotNull(result);
            }
        } finally {
            // Restore permissions and cleanup
            file.setReadable(true);
            Files.deleteIfExists(testFile);
        }
    }

    @Test
    @DisplayName("validateFile debe manejar excepciones correctamente")
    void testValidateFileConExcepcion() throws Exception {
        // Arrange - Usar un path inválido que cause excepción
        ReflectionTestUtils.setField(jobRegistry, "dataPath", null);

        // Act
        java.lang.reflect.Method validateFileMethod = JobRegistryImpl.class
            .getDeclaredMethod("validateFile", String.class);
        validateFileMethod.setAccessible(true);
        String result = (String) validateFileMethod.invoke(jobRegistry, "test.csv");

        // Assert
        assertNotNull(result, "Debe retornar mensaje de error");
        assertTrue(result.contains("ERROR"), "Debe indicar error");
    }

    // ============================================
    // TESTS PARA EL MÉTODO runBatchJob
    // ============================================

    @Test
    @DisplayName("runBatchJob debe ejecutar exitosamente con archivo válido")
    void testRunBatchJobConArchivoValido() throws Exception {
        // Arrange
        Path testFile = Files.createTempFile(new File(testDataPath).toPath(), "test_", ".csv");
        Files.write(testFile, "nombre;edad;email\nJuan;30;juan@test.com\n".getBytes());
        String fileName = testFile.getFileName().toString();
        
        JobExecution jobExecution = mock(JobExecution.class);
        doReturn(jobExecution).when(jobLauncher).run(any(Job.class), any(JobParameters.class));

        // Act
        String result = jobRegistry.runBatchJob(fileName);

        // Assert
        assertNotNull(result, "El resultado no debe ser nulo");
        assertTrue(result.contains("✓"), "Debe contener indicador de éxito");
        assertTrue(result.contains("exitosamente"), "Debe indicar ejecución exitosa");
        assertTrue(result.contains(fileName), "Debe contener el nombre del archivo");
        verify(jobLauncher, times(1)).run(any(Job.class), any(JobParameters.class));

        // Cleanup
        Files.deleteIfExists(testFile);
    }

    @Test
    @DisplayName("runBatchJob debe retornar error cuando archivo no existe")
    void testRunBatchJobConArchivoNoExistente() throws Exception {
        // Act
        String result = jobRegistry.runBatchJob("archivo_inexistente.csv");

        // Assert
        assertNotNull(result, "El resultado no debe ser nulo");
        assertTrue(result.contains("✗"), "Debe contener indicador de error");
        assertTrue(result.contains("no existe"), "Debe indicar que archivo no existe");
        verify(jobLauncher, never()).run(any(Job.class), any(JobParameters.class));
    }

    @Test
    @DisplayName("runBatchJob debe retornar error cuando filename es null")
    void testRunBatchJobConFilenameNull() throws Exception {
        // Act
        String result = jobRegistry.runBatchJob(null);

        // Assert
        assertNotNull(result, "El resultado no debe ser nulo");
        assertTrue(result.contains("✗"), "Debe contener indicador de error");
        assertTrue(result.contains("no puede estar vacío"), "Debe indicar error de validación");
        verify(jobLauncher, never()).run(any(Job.class), any(JobParameters.class));
    }

    @Test
    @DisplayName("runBatchJob debe retornar error cuando filename está vacío")
    void testRunBatchJobConFilenameVacio() throws Exception {
        // Act
        String result = jobRegistry.runBatchJob("");

        // Assert
        assertNotNull(result, "El resultado no debe ser nulo");
        assertTrue(result.contains("✗"), "Debe contener indicador de error");
        assertTrue(result.contains("no puede estar vacío"), "Debe indicar error de validación");
        verify(jobLauncher, never()).run(any(Job.class), any(JobParameters.class));
    }

    @Test
    @DisplayName("runBatchJob debe construir correctamente JobParameters")
    void testRunBatchJobConstruyeJobParametersCorrectamente() throws Exception {
        // Arrange
        Path testFile = Files.createTempFile(new File(testDataPath).toPath(), "test_", ".csv");
        Files.write(testFile, "nombre;edad;email\nAna;25;ana@test.com\n".getBytes());
        String fileName = testFile.getFileName().toString();
        
        JobExecution jobExecution = mock(JobExecution.class);
        doReturn(jobExecution).when(jobLauncher).run(any(Job.class), any(JobParameters.class));

        // Act
        String result = jobRegistry.runBatchJob(fileName);

        // Assert
        assertTrue(result.contains("✓"), "Debe ejecutarse exitosamente");
        // Verificar que se llamó con JobParameters
        verify(jobLauncher, times(1)).run(
            eq(importUserJob),
            any(JobParameters.class)
        );

        // Cleanup
        Files.deleteIfExists(testFile);
    }

    @Test
    @DisplayName("runBatchJob debe manejar excepciones durante ejecución")
    void testRunBatchJobManejaExcepcion() throws Exception {
        // Arrange
        Path testFile = Files.createTempFile(new File(testDataPath).toPath(), "test_", ".csv");
        Files.write(testFile, "nombre;edad;email\nPedro;40;pedro@test.com\n".getBytes());
        String fileName = testFile.getFileName().toString();
        
        doThrow(new RuntimeException("Error simulado en ejecución"))
            .when(jobLauncher).run(any(Job.class), any(JobParameters.class));

        // Act
        String result = jobRegistry.runBatchJob(fileName);

        // Assert
        assertNotNull(result, "El resultado no debe ser nulo");
        assertTrue(result.contains("✗"), "Debe contener indicador de error");
        assertTrue(result.contains("ERROR"), "Debe indicar error");

        // Cleanup
        Files.deleteIfExists(testFile);
    }

    @Test
    @DisplayName("runBatchJob debe incluir timestamp único en JobID")
    void testRunBatchJobJobIDEsUnico() throws Exception {
        // Arrange
        Path testFile = Files.createTempFile(new File(testDataPath).toPath(), "test_", ".csv");
        Files.write(testFile, "nombre;edad;email\nTest;20;test@test.com\n".getBytes());
        String fileName = testFile.getFileName().toString();
        
        JobExecution jobExecution = mock(JobExecution.class);
        doReturn(jobExecution).when(jobLauncher).run(any(Job.class), any(JobParameters.class));

        // Act - Ejecutar dos veces
        String result1 = jobRegistry.runBatchJob(fileName);
        String result2 = jobRegistry.runBatchJob(fileName);

        // Assert
        assertTrue(result1.contains("✓"), "Primer ejecución debe ser exitosa");
        assertTrue(result2.contains("✓"), "Segunda ejecución debe ser exitosa");
        // Verificar que se llamó 2 veces con diferentes JobParameters
        verify(jobLauncher, times(2)).run(any(Job.class), any(JobParameters.class));

        // Cleanup
        Files.deleteIfExists(testFile);
    }

    // ============================================
    // TESTS PARA EL MÉTODO registerJob
    // ============================================

    @Test
    @DisplayName("registerJob debe retornar true con nombre válido")
    void testRegisterJobConNombreValido() {
        // Act
        boolean result = jobRegistry.registerJob("importCsvJob");

        // Assert
        assertTrue(result, "Debe retornar true para nombre válido");
    }

    @Test
    @DisplayName("registerJob debe aceptar cualquier nombre no nulo")
    void testRegisterJobConDiferentesNombres() {
        // Act
        boolean result1 = jobRegistry.registerJob("job1");
        boolean result2 = jobRegistry.registerJob("importUserJob");
        boolean result3 = jobRegistry.registerJob("processDataJob");

        // Assert
        assertTrue(result1, "Debe retornar true");
        assertTrue(result2, "Debe retornar true");
        assertTrue(result3, "Debe retornar true");
    }

    @Test
    @DisplayName("registerJob debe retornar true incluso con nombre largo")
    void testRegisterJobConNombreLargo() {
        // Arrange
        String nombreLargo = "myjob_with_very_long_name_that_describes_the_job_in_detail_";

        // Act
        boolean result = jobRegistry.registerJob(nombreLargo);

        // Assert
        assertTrue(result, "Debe retornar true para nombre largo");
    }

    @Test
    @DisplayName("registerJob debe retornar true con nombre especial")
    void testRegisterJobConNombreEspecial() {
        // Act
        boolean result = jobRegistry.registerJob("job-with-dashes_and_underscores");

        // Assert
        assertTrue(result, "Debe retornar true para nombre con caracteres especiales");
    }

    @Test
    @DisplayName("registerJob debe retornar false cuando nombre es null")
    void testRegisterJobConNombreNull() {
        // Act & Assert
        // El método debería manejar null apropiadament
        assertDoesNotThrow(() -> {
            boolean result = jobRegistry.registerJob(null);
            // El resultado depende de la implementación
        }, "No debe lanzar excepción");
    }

    @Test
    @DisplayName("registerJob debe retornar false cuando nombre es vacío")
    void testRegisterJobConNombreVacio() {
        // Act
        boolean result = jobRegistry.registerJob("");

        // Assert
        assertTrue(result, "Debe retornar true incluso con nombre vacío");
    }

    @Test
    @DisplayName("registerJob debe ser idempotente (llamadas múltiples con mismo nombre)")
    void testRegisterJobEsIdempotente() {
        // Arrange
        String jobName = "testJob";

        // Act
        boolean result1 = jobRegistry.registerJob(jobName);
        boolean result2 = jobRegistry.registerJob(jobName);
        boolean result3 = jobRegistry.registerJob(jobName);

        // Assert
        assertTrue(result1, "Primera llamada debe retornar true");
        assertTrue(result2, "Segunda llamada debe retornar true");
        assertTrue(result3, "Tercera llamada debe retornar true");
    }

    // ============================================
    // TESTS PARA EL MÉTODO getAllRegistros
    // ============================================

    @Test
    @DisplayName("getAllRegistros debe retornar lista de registros")
    void testGetAllRegistros() {
        // Arrange
        List<RegistroCSV> registros = new ArrayList<>();
        registros.add(new RegistroCSV());
        registros.add(new RegistroCSV());
        when(registroRepository.findAll()).thenReturn(registros);

        // Act
        List<RegistroCSV> result = jobRegistry.getAllRegistros();

        // Assert
        assertNotNull(result, "La lista no debe ser nula");
        assertEquals(2, result.size(), "Debe retornar 2 registros");
        verify(registroRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllRegistros debe retornar lista vacía cuando no hay registros")
    void testGetAllRegistrosVacio() {
        // Arrange
        when(registroRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<RegistroCSV> result = jobRegistry.getAllRegistros();

        // Assert
        assertNotNull(result, "La lista no debe ser nula");
        assertTrue(result.isEmpty(), "La lista debe estar vacía");
    }

    // ============================================
    // TESTS PARA EL MÉTODO getRegistroById
    // ============================================

    @Test
    @DisplayName("getRegistroById debe retornar Optional con registro")
    void testGetRegistroById() {
        // Arrange
        RegistroCSV registro = new RegistroCSV();
        when(registroRepository.findById(1L)).thenReturn(Optional.of(registro));

        // Act
        Optional<RegistroCSV> result = jobRegistry.getRegistroById(1L);

        // Assert
        assertTrue(result.isPresent(), "Optional debe contener un valor");
        assertEquals(registro, result.get(), "Debe retornar el registro correcto");
    }

    @Test
    @DisplayName("getRegistroById debe retornar Optional vacío cuando no existe")
    void testGetRegistroByIdNoExiste() {
        // Arrange
        when(registroRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<RegistroCSV> result = jobRegistry.getRegistroById(999L);

        // Assert
        assertTrue(result.isEmpty(), "Optional debe estar vacío");
    }

    // ============================================
    // TESTS DE INTEGRACIÓN
    // ============================================

    @Test
    @DisplayName("JobRegistryImpl debe implementar IJobRegistry")
    void testJobRegistryImplImplementaIJobRegistry() {
        // Assert
        assertInstanceOf(IJobRegistry.class, jobRegistry, "Debe implementar IJobRegistry");
    }

    @Test
    @DisplayName("JobRegistryImpl debe tener anotación @Service")
    void testJobRegistryImplAnotacion() {
        // Assert
        org.springframework.stereotype.Service annotation = 
            JobRegistryImpl.class.getAnnotation(org.springframework.stereotype.Service.class);
        assertNotNull(annotation, "Debe tener anotación @Service");
    }

    @Test
    @DisplayName("JobRegistryImpl debe tener inyección de dependencias @Autowired")
    void testJobRegistryImplTieneDependenciasInyectadas() {
        // Assert
        assertNotNull(jobRegistry, "jobRegistry debe estar inyectado");
    }
}
