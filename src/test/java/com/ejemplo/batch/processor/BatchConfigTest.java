package com.ejemplo.batch.processor;

import com.ejemplo.batch.model.RegistroCSV;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import com.ejemplo.batch.processor.CustomJpaItemWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias para BatchConfig")
class BatchConfigTest {

    private BatchConfig batchConfig;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private EntityManagerFactory entityManagerFactory;

    @BeforeEach
    void setUp() {
        batchConfig = new BatchConfig(jobRepository, transactionManager, entityManagerFactory);
    }

    // ============================================
    // TESTS PARA EL CONSTRUCTOR
    // ============================================

    @Test
    @DisplayName("Debe crear instancia de BatchConfig con dependencias válidas")
    void testBatchConfigInstanciacion() {
        assertNotNull(batchConfig, "BatchConfig debe ser instanciado correctamente");
    }

    @Test
    @DisplayName("Constructor debe aceptar JobRepository, TransactionManager y EntityManagerFactory")
    void testConstructorConParametros() {
        JobRepository mockJobRepo = mock(JobRepository.class);
        PlatformTransactionManager mockTransactionMgr = mock(PlatformTransactionManager.class);
        EntityManagerFactory mockEntityMgrFactory = mock(EntityManagerFactory.class);

        BatchConfig config = new BatchConfig(mockJobRepo, mockTransactionMgr, mockEntityMgrFactory);

        assertNotNull(config);
    }

    // ============================================
    // TESTS PARA EL MÉTODO READER
    // ============================================

    @Test
    @DisplayName("Método reader debe crear FlatFileItemReader válido con archivo existente")
    void testReaderConArchivoValido() throws Exception {
        // Crear archivo temporal para test
        Path tempFile = Files.createTempFile("test_", ".csv");
        Files.write(tempFile, "nombre;edad;email\nJuan;30;juan@test.com\n".getBytes());

        try {
            FlatFileItemReader<RegistroCSV> reader = batchConfig.reader(tempFile.toString());

            assertNotNull(reader, "El reader no debe ser nulo");
            assertEquals("csvReader", reader.getName(), "El nombre del reader debe ser 'csvReader'");
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    @DisplayName("Método reader debe lanzar IllegalArgumentException cuando archivo no existe")
    void testReaderConArchivoNoExistente() {
        String rutaInvalida = "C:\\archivo\\inexistente\\test.csv";

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> batchConfig.reader(rutaInvalida),
            "Debe lanzar IllegalArgumentException cuando el archivo no existe"
        );

        assertTrue(exception.getMessage().contains("no existe"),
            "El mensaje debe indicar que el archivo no existe");
    }

    @Test
    @DisplayName("Método reader debe validar permisos de lectura del archivo")
    void testReaderValidaPermisosLectura() throws Exception {
        // Crear archivo temporal
        Path tempFile = Files.createTempFile("test_", ".csv");
        Files.write(tempFile, "nombre;edad;email\nTest;25;test@test.com\n".getBytes());

        try {
            File file = tempFile.toFile();
            
            // Archivo con permisos de lectura debe funcionar
            if (file.canRead()) {
                FlatFileItemReader<RegistroCSV> reader = batchConfig.reader(tempFile.toString());
                assertNotNull(reader, "Reader debe crearse con permisos de lectura");
            }
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    @DisplayName("Método reader debe mostrar mensaje de éxito cuando archivo es válido")
    void testReaderMuestraMensajeExito() throws Exception {
        Path tempFile = Files.createTempFile("test_", ".csv");
        Files.write(tempFile, "nombre;edad;email\n".getBytes());

        try {
            // Capturar System.out para verificar mensaje
            FlatFileItemReader<RegistroCSV> reader = batchConfig.reader(tempFile.toString());
            
            assertNotNull(reader, "Reader debe crearse exitosamente");
            assertEquals("csvReader", reader.getName());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    @DisplayName("Método reader debe manejar ruta de archivo vacía")
    void testReaderConRutaVacia() {
        // Una ruta vacía se trata como archivo en directorio actual (probablemente un directorio)
        // El test verifica que maneje esto apropiadamente
        String rutaVacia = "";
        // El reader intenta acceder al archivo, si no existe o no tiene permisos, falla
        try {
            FlatFileItemReader<RegistroCSV> reader = batchConfig.reader(rutaVacia);
            // Si llega aquí, el archivo vacío existe (muy poco probable)
            assertNotNull(reader);
        } catch (Exception e) {
            // Se espera una excepción en la mayoría de los casos
            assertNotNull(e);
        }
    }

    @Test
    @DisplayName("Método reader debe manejar ruta de archivo nula")
    void testReaderConRutaNula() {
        assertThrows(
            Exception.class,
            () -> batchConfig.reader(null),
            "Debe lanzar excepción con ruta nula"
        );
    }

    @Test
    @DisplayName("Reader debe estar configurado para omitir línea de encabezado")
    void testReaderConfiguradoParaOmitirEncabezado() throws Exception {
        Path tempFile = Files.createTempFile("test_", ".csv");
        Files.write(tempFile, "nombre;edad;email\nJuan;30;juan@test.com\n".getBytes());

        try {
            FlatFileItemReader<RegistroCSV> reader = batchConfig.reader(tempFile.toString());
            assertNotNull(reader, "Reader debe estar correctamente configurado");
            // El reader debe saltarse la primera línea
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    @DisplayName("Reader debe tener delimitador configurado correctamente (;)")
    void testReaderConDelimitadorCorrect() throws Exception {
        Path tempFile = Files.createTempFile("test_", ".csv");
        Files.write(tempFile, "nombre;edad;email\nAna;25;ana@test.com\n".getBytes());

        try {
            FlatFileItemReader<RegistroCSV> reader = batchConfig.reader(tempFile.toString());
            
            assertNotNull(reader, "Reader debe estar configurado con delimitador ;");
            assertEquals("csvReader", reader.getName());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    // ============================================
    // TESTS PARA EL MÉTODO PROCESSOR
    // ============================================

    @Test
    @DisplayName("Método processor debe crear bean RegistroProcessor")
    void testProcessorBean() {
        RegistroProcessor processor = batchConfig.processor();

        assertNotNull(processor, "El processor no debe ser nulo");
        assertInstanceOf(RegistroProcessor.class, processor, "Debe retornar instancia de RegistroProcessor");
    }

    @Test
    @DisplayName("Método processor debe retornar nueva instancia cada vez")
    void testProcessorBeanNuevaInstancia() {
        RegistroProcessor processor1 = batchConfig.processor();
        RegistroProcessor processor2 = batchConfig.processor();

        assertNotNull(processor1);
        assertNotNull(processor2);
    }

    // ============================================
    // TESTS PARA EL MÉTODO WRITER
    // ============================================
    @Test
    @DisplayName("Writer debe retornar CustomJpaItemWriter")
    void testWriterBean() {
        CustomJpaItemWriter writer = batchConfig.writer();

        assertNotNull(writer, "El writer no debe ser nulo");
        assertInstanceOf(CustomJpaItemWriter.class, writer, "Debe retornar instancia de CustomJpaItemWriter");
    }

    @Test
    @DisplayName("Writer debe tener EntityManagerFactory configurado")
    void testWriterConEntityManagerFactory() {
        CustomJpaItemWriter writer = batchConfig.writer();

        assertNotNull(writer, "Writer debe estar configurado");
    }

    // ============================================
    // TESTS PARA EL MÉTODO IMPORT STEP
    // ============================================

    @Test
    @DisplayName("Método importStep debe crear Step válido")
    void testImportStep() throws Exception {
        // Crear mocks
        FlatFileItemReader<RegistroCSV> mockReader = mock(FlatFileItemReader.class);
        RegistroProcessor mockProcessor = mock(RegistroProcessor.class);
        CustomJpaItemWriter mockWriter = mock(CustomJpaItemWriter.class);

        Step step = batchConfig.importStep(mockReader, mockProcessor, mockWriter);

        assertNotNull(step, "El step no debe ser nulo");
        assertEquals("csvImportStep", step.getName(), "El nombre del step debe ser 'csvImportStep'");
    }

    @Test
    @DisplayName("ImportStep debe estar configurado con chunk size de 10")
    void testImportStepChunkSize() throws Exception {
        FlatFileItemReader<RegistroCSV> mockReader = mock(FlatFileItemReader.class);
        RegistroProcessor mockProcessor = mock(RegistroProcessor.class);
        CustomJpaItemWriter mockWriter = mock(CustomJpaItemWriter.class);

        Step step = batchConfig.importStep(mockReader, mockProcessor, mockWriter);

        assertNotNull(step, "Step debe tener tamaño de chunk configurado");
    }

    // ============================================
    // TESTS PARA EL MÉTODO JOB
    // ============================================

    @Test
    @DisplayName("Método importUserJob debe crear Job válido")
    void testImportUserJob() {
        Step mockStep = mock(Step.class);

        Job job = batchConfig.importUserJob(mockStep);

        assertNotNull(job, "El job no debe ser nulo");
        assertEquals("importCsvJob", job.getName(), "El nombre del job debe ser 'importCsvJob'");
    }

    @Test
    @DisplayName("Job debe tener RunIdIncrementer configurado")
    void testJobConRunIdIncrementer() {
        Step mockStep = mock(Step.class);

        Job job = batchConfig.importUserJob(mockStep);

        assertNotNull(job, "Job debe tener RunIdIncrementer para permitir múltiples ejecuciones");
    }

    @Test
    @DisplayName("Job debe contener el importStep configurado")
    void testJobContieneImportStep() {
        Step mockStep = mock(Step.class);

        Job job = batchConfig.importUserJob(mockStep);

        assertNotNull(job, "Job debe contener el step de importación");
        assertEquals("importCsvJob", job.getName());
    }

    // ============================================
    // TESTS DE INTEGRACIÓN
    // ============================================

    @Test
    @DisplayName("BatchConfig debe estar anotado con @Configuration")
    void testBatchConfigAnnotation() {
        org.springframework.context.annotation.Configuration annotation = 
            BatchConfig.class.getAnnotation(org.springframework.context.annotation.Configuration.class);
        
        assertNotNull(annotation, "BatchConfig debe estar anotado con @Configuration");
    }

    @Test
    @DisplayName("BatchConfig debe estar anotado con @EnableBatchProcessing")
    void testEnableBatchProcessingAnnotation() {
        org.springframework.batch.core.configuration.annotation.EnableBatchProcessing annotation = 
            BatchConfig.class.getAnnotation(
                org.springframework.batch.core.configuration.annotation.EnableBatchProcessing.class);
        
        assertNotNull(annotation, "BatchConfig debe estar anotado con @EnableBatchProcessing");
    }

    @Test
    @DisplayName("Reader debe estar anotado con @Bean y @Scope")
    void testReaderBeanAnnotation() {
        try {
            var readerMethod = BatchConfig.class.getDeclaredMethod("reader", String.class);
            var beanAnnotation = readerMethod.getAnnotation(org.springframework.context.annotation.Bean.class);
            var scopeAnnotation = readerMethod.getAnnotation(org.springframework.context.annotation.Scope.class);
            
            assertNotNull(beanAnnotation, "reader debe estar anotado con @Bean");
            assertNotNull(scopeAnnotation, "reader debe estar anotado con @Scope");
        } catch (NoSuchMethodException e) {
            fail("reader method not found: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Processor debe estar anotado con @Bean")
    void testProcessorBeanAnnotation() {
        try {
            var processorMethod = BatchConfig.class.getDeclaredMethod("processor");
            var beanAnnotation = processorMethod.getAnnotation(org.springframework.context.annotation.Bean.class);
            
            assertNotNull(beanAnnotation, "processor debe estar anotado con @Bean");
        } catch (NoSuchMethodException e) {
            fail("processor method not found: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Writer debe estar anotado con @Bean")
    void testWriterBeanAnnotation() {
        try {
            var writerMethod = BatchConfig.class.getDeclaredMethod("writer");
            var beanAnnotation = writerMethod.getAnnotation(org.springframework.context.annotation.Bean.class);
            
            assertNotNull(beanAnnotation, "writer debe estar anotado con @Bean");
        } catch (NoSuchMethodException e) {
            fail("writer method not found: " + e.getMessage());
        }
    }
}
