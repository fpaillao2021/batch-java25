package com.ejemplo.batch.services.impl;

import com.ejemplo.batch.model.RegistroCSV;
import com.ejemplo.batch.repository.RegistroRepository;
import com.ejemplo.batch.services.IJobRegistry;
import com.ejemplo.batch.utils.MessagesLocales;
import com.ejemplo.batch.config.DataSourceContext;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Optional;

@Service
public class JobRegistryImpl implements IJobRegistry {

    @Autowired
    private JobOperator jobLauncher;

    @Autowired
    private Job importUserJob;

    @Autowired
    private RegistroRepository registroRepository;

    @Value("${file.data.path}")
    private String dataPath;

    /**
     * Ejecuta el proceso batch con el archivo especificado
     * 
     * SOLUCIÓN CORRECTA: 
     * - Setea el DataSourceContext para que esté disponible en el batch thread
     * - NO limpia el contexto aquí (el batch se ejecuta de forma asíncrona)
     * - JobRegistryImpl solo limpia en caso de error de validación
     * 
     * @param filename Nombre del archivo CSV (ej: registros.csv)
     * @param database Base de datos destino: DB_A (MySQL) o DB_B (PostgreSQL)
     * @return Mensaje de resultado o error
     */
    @Override
    public String runBatchJob(String filename, String database) {
        try {
            // Validar y setear la base de datos
            if (database == null || (!database.equals("DB_A") && !database.equals("DB_B"))) {
                database = "DB_A"; // Default a MySQL
            }
            System.out.println("🔄 Ejecutando Batch en: " + database);
            
            // Setear el contexto de datasource para que esté disponible durante el batch
            DataSourceContext.setDataSourceKey(database);

            // validar nombre y ruta del archivo
            String validationError = validateFile(filename);
            if (validationError != null) {
                DataSourceContext.clear(); // Limpiar contexto en caso de error
                return validationError;
            }

            // crear la ruta completa del archivo
            String filepath = dataPath + "/" + filename;

            // Crear parámetros del job incluyendo la BD
            // IMPORTANTE: La BD se pasa en JobParameters como respaldo
            JobParameters jobParameters = new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .addString("file.input", filepath)
                .addString("database", database)  // ✅ Pasar la BD en JobParameters
                .toJobParameters();

            // Ejecutar el job
            jobLauncher.run(importUserJob, jobParameters);
            
            // ✅ NO LIMPIAR EL CONTEXTO AQUÍ
            // El batch se ejecuta asíncrono, el contexto debe mantenerse disponible
            // CustomJpaItemWriter.write() lo usará para saber a qué BD escribir
            
            return MessagesLocales.MensajeLocal.BATCH_EJECUTADO_EXITOSAMENTE + filename + " en " + database;
            
        } catch (IllegalArgumentException e) {
            // Error de validación del archivo
            DataSourceContext.clear();
            return MessagesLocales.ErrorMensajeLocal.ERROR_VALIDACION_ARCHIVO + e.getMessage();
        } catch (Exception e) {
            // Otros errores durante la ejecución
            DataSourceContext.clear();
            e.printStackTrace();
            return MessagesLocales.ErrorMensajeLocal.ERROR_EJECUTAR_JOB + e.getMessage();
        }
        // ✅ NO hay finally que limpie
        // El contexto será usado mientras el batch procesa datos
    }

    /**
     * Obtiene todos los registros procesados de la BD actual (según DataSourceContext)
     * Si se usa en API, el interceptor habrá establecido la BD correcta
     */
    @Override
    public List<RegistroCSV> getAllRegistros() {
        try {
            return registroRepository.findAll();
        } finally {
            // Limpiar contexto después de consultas de API
            DataSourceContext.clear();
        }
    }

    /**
     * Obtiene un registro por su ID de la BD actual (según DataSourceContext)
     * Si se usa en API, el interceptor habrá establecido la BD correcta
     */
    @Override
    public Optional<RegistroCSV> getRegistroById(Long id) {
        try {
            return registroRepository.findById(id);
        } finally {
            // Limpiar contexto después de consultas de API
            DataSourceContext.clear();
        }
    }

    /**
     * Registra un job en el sistema
     */
    @Override
    public boolean registerJob(String jobName) {
        // Lógica para registrar el job
        System.out.println(MessagesLocales.MensajeLocal.JOB_REGISTRADO + jobName);
        return true;
    }

    /**
     * Valida la existencia y permisos del archivo
     */
    private String validateFile(String fileName) {
      try {
            // Validar que el filename no esté vacío
            if (fileName == null || fileName.trim().isEmpty()) {
                return MessagesLocales.ErrorMensajeLocal.ERROR_FILENAME_VACIO;
            }
            
            String filepath = dataPath + "/" + fileName;
            
            // Validar que el archivo existe ANTES de crear los parámetros
            File file = new File(filepath);
            if (!file.exists()) {
                return MessagesLocales.ErrorMensajeLocal.ERROR_ARCHIVO_NO_EXISTE + fileName + 
                       MessagesLocales.ErrorMensajeLocal.ERROR_ARCHIVO_NO_EXISTE_RUTA + dataPath + 
                       MessagesLocales.ErrorMensajeLocal.ERROR_ARCHIVO_NO_EXISTE_SUFIJO;
            }
            
            if (!file.canRead()) {
                return MessagesLocales.ErrorMensajeLocal.ERROR_PERMISOS_LECTURA + fileName + 
                       MessagesLocales.ErrorMensajeLocal.ERROR_PERMISOS_LECTURA_SUFIJO;
            }
            return null;
      } catch (Exception e) {
            return MessagesLocales.ErrorMensajeLocal.ERROR_INESPERADO_VALIDAR + e.getMessage();
      }
    } 
}
