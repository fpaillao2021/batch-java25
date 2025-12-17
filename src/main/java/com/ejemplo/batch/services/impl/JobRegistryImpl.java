package com.ejemplo.batch.services.impl;

import com.ejemplo.batch.model.RegistroCSV;
import com.ejemplo.batch.repository.RegistroRepository;
import com.ejemplo.batch.services.IJobRegistry;
import com.ejemplo.batch.utils.MessagesLocales;

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
     * @param filename Nombre del archivo CSV (ej: registros.csv)
     * @return Mensaje de resultado o error
     */
    @Override
    public String runBatchJob(String filename) {
        try {

            // validar nombre y ruta del archivo
            String validationError = validateFile(filename);
            if (validationError != null) {
                return validationError;
            }

            // crear la ruta completa del archivo
            String filepath = dataPath + "/" + filename;

            // Crear parámetros del job
            JobParameters jobParameters = new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .addString("file.input", filepath)
                .toJobParameters();

            // Ejecutar el job
            jobLauncher.run(importUserJob, jobParameters);
            
            return MessagesLocales.MensajeLocal.BATCH_EJECUTADO_EXITOSAMENTE + filename;
            
        } catch (IllegalArgumentException e) {
            // Error de validación del archivo
            return MessagesLocales.ErrorMensajeLocal.ERROR_VALIDACION_ARCHIVO + e.getMessage();
        } catch (Exception e) {
            // Otros errores durante la ejecución
            e.printStackTrace();
            return MessagesLocales.ErrorMensajeLocal.ERROR_EJECUTAR_JOB + e.getMessage();
        }
    }

    /**
     * Obtiene todos los registros procesados
     */
    @Override
    public List<RegistroCSV> getAllRegistros() {
        return registroRepository.findAll();
    }

    /**
     * Obtiene un registro por su ID
     */
    @Override
    public Optional<RegistroCSV> getRegistroById(Long id) {
        return registroRepository.findById(id);
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
