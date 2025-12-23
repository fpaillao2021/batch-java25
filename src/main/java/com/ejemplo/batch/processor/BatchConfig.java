package com.ejemplo.batch.processor;

import com.evertecinc.entitydto.app.batch.model.entity.RegistroCSV;
import com.evertecinc.entitydto.app.utils.MessagesLocales;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.step.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.client.RestTemplate;
import java.io.File;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    private final JobRepository jobRepository;
    private final RestTemplate restTemplate;

    // Inyecta el JobRepository y RestTemplate de Spring Boot 4
    public BatchConfig(JobRepository jobRepository, RestTemplate restTemplate) {
        this.jobRepository = jobRepository;
        this.restTemplate = restTemplate;
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

    // --- Writer (Escritor vía API REST de batch-dl-data-mysql) ---
    @Bean
    public ItemWriter<RegistroCSV> writer(@Value("${batch.dl.data.mysql.api.url:http://batch-dl-data-mysql:8585/api/mysql/dl}") String apiBaseUrl) {
        return new RestItemWriter(restTemplate, apiBaseUrl);
    }

    // --- Step (Unidad de Proceso) ---
    @Bean
    public Step importStep(ItemReader<RegistroCSV> reader, RegistroProcessor processor, ItemWriter<RegistroCSV> writer) {
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