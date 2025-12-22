package com.ejemplo.batch.processor;

import com.ejemplo.batch.model.RegistroCSV;
import com.ejemplo.batch.utils.MessagesLocales;
import jakarta.persistence.EntityManagerFactory;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.step.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import com.ejemplo.batch.processor.CustomJpaItemWriter;
import com.ejemplo.batch.processor.DataSourceStepListener;
import com.ejemplo.batch.processor.DataSourceChunkListener;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;
import com.ejemplo.batch.config.DynamicTransactionManager;
import java.io.File;

@Configuration
@EnableBatchProcessing
// NOTA: Se crean DOS jobs separados, uno para cada base de datos.
// Cada job tiene su propio JobRepository y almacena metadatos en su respectiva BD.
public class BatchConfig {
    
    @Autowired
    private DataSourceStepListener dataSourceStepListener;
    
    @Autowired
    private DataSourceChunkListener dataSourceChunkListener;
    
    @Autowired
    private FileReaderListener fileReaderListener;
    
    @Autowired
    private ConnectionCleanupListener connectionCleanupListener;
    
    @Autowired
    private DatabaseLifecycleManager databaseLifecycleManager;
    
    @Autowired
    private ReaderInitializationListener readerInitializationListener;
    
    @Autowired
    private EntityManagerFactory entityManagerFactory; // Mantener para compatibilidad, pero no se usará

    // --- Reader (Lector de Archivo CSV) ---
    // CRÍTICO: Usar @StepScope para que Spring Batch cree una nueva instancia para cada step execution
    // Esto asegura que cada ejecución tenga su propia instancia completamente nueva
    @Bean
    @org.springframework.batch.core.configuration.annotation.StepScope
    public FlatFileItemReader<RegistroCSV> reader(
            @Value("#{jobParameters['file.input']}") String pathToFile) {
        
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("📂 INICIO DE CONFIGURACIÓN DEL READER:");
        System.out.println("   📁 Ruta recibida del JobParameter: " + pathToFile);
        System.out.println("   🧵 Thread: " + Thread.currentThread().getName());
        System.out.println("   ⏰ Timestamp: " + java.time.LocalDateTime.now());
        
        // CRÍTICO: Normalizar la ruta del archivo para evitar problemas con rutas relativas
        File file = new File(pathToFile);
        
        // Si la ruta relativa no existe, intentar con diferentes variantes
        if (!file.exists()) {
            System.out.println("   ⚠️ Archivo no encontrado en ruta relativa, intentando variantes...");
            
            // Intentar con ruta absoluta desde el directorio de trabajo actual
            String workingDir = System.getProperty("user.dir");
            File absoluteFile = new File(workingDir, pathToFile);
            System.out.println("   📁 Intentando ruta absoluta desde working dir: " + absoluteFile.getAbsolutePath());
            System.out.println("   ✅ Existe: " + absoluteFile.exists());
            
            if (absoluteFile.exists()) {
                file = absoluteFile;
                pathToFile = absoluteFile.getAbsolutePath();
                System.out.println("   ✅ Usando ruta absoluta: " + pathToFile);
            } else {
                // Intentar con src/main/resources/data/ si la ruta no lo incluye
                if (!pathToFile.contains("src/main/resources/data/")) {
                    File dataDirFile = new File(workingDir, "src/main/resources/data/" + new File(pathToFile).getName());
                    System.out.println("   📁 Intentando con src/main/resources/data/: " + dataDirFile.getAbsolutePath());
                    System.out.println("   ✅ Existe: " + dataDirFile.exists());
                    
                    if (dataDirFile.exists()) {
                        file = dataDirFile;
                        pathToFile = dataDirFile.getAbsolutePath();
                        System.out.println("   ✅ Usando ruta con src/main/resources/data/: " + pathToFile);
                    }
                }
            }
        }
        
        // Validar que el archivo existe
        if (!file.exists()) {
            System.err.println("═══════════════════════════════════════════════════════════");
            System.err.println("❌ ERROR: Archivo no encontrado después de intentar todas las variantes");
            System.err.println("   📁 Ruta original: " + pathToFile);
            System.err.println("   📁 Working directory: " + System.getProperty("user.dir"));
            System.err.println("═══════════════════════════════════════════════════════════");
            throw new IllegalArgumentException(
                MessagesLocales.ErrorMensajeLocal.ERROR_ARCHIVO_CSV_NO_EXISTE + pathToFile);
        }
        
        if (!file.canRead()) {
            System.err.println("❌ ERROR: No hay permisos de lectura para el archivo: " + pathToFile);
            throw new IllegalArgumentException(
                MessagesLocales.ErrorMensajeLocal.ERROR_PERMISOS_LECTURA_CSV + pathToFile);
        }
        
        // CRÍTICO: Usar ruta absoluta para evitar problemas con rutas relativas
        String absolutePath = file.getAbsolutePath();
        FileSystemResource absoluteResource = new FileSystemResource(absolutePath);
        
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("📂 CONFIGURACIÓN FINAL DEL READER:");
        System.out.println("   📁 Ruta final del archivo: " + absolutePath);
        System.out.println("   ✅ Archivo existe: " + file.exists());
        System.out.println("   ✅ Archivo se puede leer: " + file.canRead());
        System.out.println("   📏 Tamaño del archivo: " + file.length() + " bytes");
        System.out.println("   📄 Nombre del archivo: " + file.getName());
        System.out.println("   📄 Resource filename: " + absoluteResource.getFilename());
        System.out.println("   📄 Resource existe: " + absoluteResource.exists());
        System.out.println("   📄 Resource path: " + absoluteResource.getPath());
        System.out.println("   🧵 Thread: " + Thread.currentThread().getName());
        
        // CRÍTICO: Crear un nombre único para el reader que incluya el nombre del archivo completo
        // Esto asegura que Spring Batch no cachee el reader entre ejecuciones
        String readerName = MessagesLocales.MensajeLocal.CSV_READER + "_" + 
                           System.currentTimeMillis() + "_" + 
                           file.getName().replaceAll("[^a-zA-Z0-9]", "_") + "_" +
                           Thread.currentThread().threadId();
        
        // CRÍTICO: Crear un nuevo reader completamente nuevo para cada ejecución
        // Usar un nombre único basado en el archivo y timestamp para evitar cualquier caché
        FlatFileItemReader<RegistroCSV> reader = new FlatFileItemReaderBuilder<RegistroCSV>()
            .name(readerName) // Nombre único con timestamp, nombre de archivo y thread ID
            .resource(absoluteResource) // Usar ruta absoluta para evitar problemas
            .saveState(false) // CRÍTICO: No guardar estado del reader entre ejecuciones
            .delimited()
            // Configura el delimitador (separador de campos)
            .delimiter(";") 
            .names("nombre", "edad", "email") // Nombres de las columnas en el CSV
            .fieldSetMapper(new BeanWrapperFieldSetMapper<RegistroCSV>() {{
                setTargetType(RegistroCSV.class);
            }})
            .linesToSkip(1) // Si el CSV tiene encabezado
            .strict(true) // CRÍTICO: Cambiar a true para detectar problemas con el archivo
            .encoding("UTF-8") // CRÍTICO: Especificar encoding explícitamente
            .build();
        
        // CRÍTICO: Forzar la reinicialización del reader estableciendo explícitamente
        // que no debe usar ningún estado guardado
        reader.setSaveState(false);
        
        System.out.println("   ✅ Reader creado con nombre: " + reader.getName());
        System.out.println("   ✅ Reader usando resource: " + absoluteResource.getPath());
        System.out.println("═══════════════════════════════════════════════════════════");
        
        // CRÍTICO: NO envolver el reader - usar directamente el reader con @StepScope
        // El @StepScope asegura que se cree una nueva instancia para cada ejecución
        // El ResettableFlatFileItemReader puede estar causando problemas de estado
        return reader;
    }

    // --- Processor (Procesador de Datos) ---
    @Bean
    public RegistroProcessor processor() {
        return new RegistroProcessor();
    }

    // --- Writer (Escritor a Base de Datos) ---
    // Usar CustomJpaItemWriter que obtiene el EntityManagerFactory dinámicamente del ThreadLocal
    // CRÍTICO: Necesitamos pasar un EntityManagerFactory temporal para la inicialización del bean
    // Durante la ejecución, se creará un nuevo JpaItemWriter con el EntityManagerFactory correcto del ThreadLocal
    @Bean
    public CustomJpaItemWriter writer(EntityManagerFactory entityManagerFactory) {
        return new CustomJpaItemWriter(entityManagerFactory); // EntityManagerFactory temporal solo para inicialización
    }



    // --- Step para DB_A (MySQL) ---
    // IMPORTANTE: Usar JpaTransactionManager (no DataSourceTransactionManager) para JpaItemWriter
    // El JpaTransactionManager está vinculado al EntityManagerFactory que usa routingDataSource
    @Bean
    public Step importStepDB_A(
            FlatFileItemReader<RegistroCSV> reader,
            RegistroProcessor processor, 
            CustomJpaItemWriter writer,
            @Qualifier("jobRepositoryDB_A") JobRepository jobRepositoryDB_A) {
        return new StepBuilder(MessagesLocales.MensajeLocal.CSV_IMPORT_STEP + "_DB_A", jobRepositoryDB_A)
            .<RegistroCSV, RegistroCSV>chunk(10)
            .reader(reader) // Reader con @StepScope se crea nuevo para cada ejecución
            .processor(processor)
            .writer(writer)
            .transactionManager(new DynamicTransactionManager()) // TransactionManager dinámico que obtiene el manager del ThreadLocal
            .listener(databaseLifecycleManager) // CRÍTICO: PRIMERO crear componentes de BD (debe ejecutarse antes que otros listeners)
            .listener(dataSourceStepListener) // Segundo: establecer contexto del DataSource
            .listener(readerInitializationListener) // Tercero: inicializar reader explícitamente
            .listener(fileReaderListener) // Cuarto: verificar archivo
            .listener(dataSourceChunkListener) // Antes de cada chunk: establecer contexto
            .listener(connectionCleanupListener) // Último: limpiar conexiones
            .build();
    }

    // --- Step para DB_B (PostgreSQL) ---
    // IMPORTANTE: Usar JpaTransactionManager (no DataSourceTransactionManager) para JpaItemWriter
    // El JpaTransactionManager está vinculado al EntityManagerFactory que usa routingDataSource
    @Bean
    public Step importStepDB_B(
            FlatFileItemReader<RegistroCSV> reader,
            RegistroProcessor processor, 
            CustomJpaItemWriter writer,
            @Qualifier("jobRepositoryDB_B") JobRepository jobRepositoryDB_B) {
        return new StepBuilder(MessagesLocales.MensajeLocal.CSV_IMPORT_STEP + "_DB_B", jobRepositoryDB_B)
            .<RegistroCSV, RegistroCSV>chunk(10)
            .reader(reader) // Reader con @StepScope se crea nuevo para cada ejecución
            .processor(processor)
            .writer(writer)
            .transactionManager(new DynamicTransactionManager()) // TransactionManager dinámico que obtiene el manager del ThreadLocal
            .listener(databaseLifecycleManager) // CRÍTICO: PRIMERO crear componentes de BD (debe ejecutarse antes que otros listeners)
            .listener(dataSourceStepListener) // Segundo: establecer contexto del DataSource
            .listener(readerInitializationListener) // Tercero: inicializar reader explícitamente
            .listener(fileReaderListener) // Cuarto: verificar archivo
            .listener(dataSourceChunkListener) // Antes de cada chunk: establecer contexto
            .listener(connectionCleanupListener) // Último: limpiar conexiones
            .build();
    }

    // --- Job para DB_A (MySQL) ---
    @Bean(name = "importUserJobDB_A")
    public Job importUserJobDB_A(
            @Qualifier("importStepDB_A") Step importStepDB_A,
            @Qualifier("jobRepositoryDB_A") JobRepository jobRepositoryDB_A) {
        return new JobBuilder(MessagesLocales.MensajeLocal.IMPORT_CSV_JOB + "_DB_A", jobRepositoryDB_A)
            .flow(importStepDB_A)
            .end()
            .build();
    }

    // --- Job para DB_B (PostgreSQL) ---
    @Bean(name = "importUserJobDB_B")
    public Job importUserJobDB_B(
            @Qualifier("importStepDB_B") Step importStepDB_B,
            @Qualifier("jobRepositoryDB_B") JobRepository jobRepositoryDB_B) {
        return new JobBuilder(MessagesLocales.MensajeLocal.IMPORT_CSV_JOB + "_DB_B", jobRepositoryDB_B)
            .flow(importStepDB_B)
            .end()
            .build();
    }
}