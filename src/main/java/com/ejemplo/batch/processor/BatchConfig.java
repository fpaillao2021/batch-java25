package com.ejemplo.batch.processor;

import com.ejemplo.batch.model.RegistroCSV;
import com.ejemplo.batch.utils.MessagesLocales;
import jakarta.persistence.EntityManagerFactory;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.step.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.database.JpaItemWriter;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;
import java.io.File;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

    // Inyecta el JobRepository, TransactionManager y EntityManagerFactory de Spring Boot 4
    public BatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager, EntityManagerFactory entityManagerFactory) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.entityManagerFactory = entityManagerFactory;
    }

    // --- Reader (Lector de Archivo CSV) ---
    // Usamos proxyMode TARGET_CLASS para que funcione con singletons
    @Bean
    @Scope(value = "step", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public FlatFileItemReader<RegistroCSV> reader(
            @Value("#{jobParameters['file.input']}") String pathToFile) {
        
        // Validar que el archivo existe
        File file = new File(pathToFile);
        if (!file.exists()) {
            throw new IllegalArgumentException(
                MessagesLocales.ErrorMensajeLocal.ERROR_ARCHIVO_CSV_NO_EXISTE + pathToFile);
        }
        
        if (!file.canRead()) {
            throw new IllegalArgumentException(
                MessagesLocales.ErrorMensajeLocal.ERROR_PERMISOS_LECTURA_CSV + pathToFile);
        }
        
        System.out.println(MessagesLocales.MensajeLocal.ARCHIVO_CSV_ENCONTRADO + pathToFile);
        System.out.println(MessagesLocales.MensajeLocal.TAMAÑO_ARCHIVO + file.length() + MessagesLocales.MensajeLocal.BYTES);

        return new FlatFileItemReaderBuilder<RegistroCSV>()
            .name(MessagesLocales.MensajeLocal.CSV_READER)
            .resource(new FileSystemResource(pathToFile))
            .delimited()
            // Configura el delimitador (separador de líneas)
            .delimiter(";") 
            .names("nombre", "edad", "email") // Nombres de las columnas en el CSV
            .fieldSetMapper(new BeanWrapperFieldSetMapper<RegistroCSV>() {{
                setTargetType(RegistroCSV.class);
            }})
            .linesToSkip(1) // Si el CSV tiene encabezado
            .build();
    }

    // --- Processor (Procesador de Datos) ---
    @Bean
    public RegistroProcessor processor() {
        return new RegistroProcessor();
    }

    // --- Writer (Escritor a Base de Datos) ---
    // Ahora usa CustomJpaItemWriter que respeta el DataSourceContext
    @Bean
    public CustomJpaItemWriter writer() {
        return new CustomJpaItemWriter(entityManagerFactory);
    }



    // --- Step (Unidad de Proceso) ---
    @Bean
    public Step importStep(FlatFileItemReader<RegistroCSV> reader, RegistroProcessor processor, CustomJpaItemWriter writer) {
        return new StepBuilder(MessagesLocales.MensajeLocal.CSV_IMPORT_STEP, jobRepository)
            .<RegistroCSV, RegistroCSV>chunk(10) // Procesa en bloques de 10
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .build();
    }

    // --- Job (El Trabajo Completo) ---
    @Bean
    public Job importUserJob(Step importStep) {
        return new JobBuilder(MessagesLocales.MensajeLocal.IMPORT_CSV_JOB, jobRepository)
            .incrementer(new RunIdIncrementer()) // Permite múltiples ejecuciones
            .flow(importStep)
            .end()
            .build();
    }
}