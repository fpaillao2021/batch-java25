package com.ejemplo.batch.services.impl;

import com.ejemplo.batch.model.RegistroCSV;
import com.ejemplo.batch.repository.RegistroRepository;
import com.ejemplo.batch.services.IJobRegistry;

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
            
            return "✓ El Job de Batch ha sido ejecutado exitosamente con el archivo: " + filename;
            
        } catch (IllegalArgumentException e) {
            // Error de validación del archivo
            return "✗ ERROR: " + e.getMessage();
        } catch (Exception e) {
            // Otros errores durante la ejecución
            e.printStackTrace();
            return "✗ ERROR al ejecutar el Job: " + e.getMessage();
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
        System.out.println("Registrando el job: " + jobName);
        return true;
    }

    /**
     * Valida la existencia y permisos del archivo
    */
    private String validateFile(String fileName) {
      try {
            // Validar que el filename no esté vacío
            if (fileName == null || fileName.trim().isEmpty()) {
                return "✗ ERROR: El nombre del archivo no puede estar vacío";
            }
            
            String filepath = dataPath + "/" + fileName;
            
            // Validar que el archivo existe ANTES de crear los parámetros
            File file = new File(filepath);
            if (!file.exists()) {
                return "✗ ERROR: El archivo '" + fileName + "' no existe en la carpeta '" + dataPath + "/'";
            }
            
            if (!file.canRead()) {
                return "✗ ERROR: No hay permisos de lectura para el archivo '" + fileName + "'";
            }
            return null;
      } catch (Exception e) {
            return "✗ ERROR inesperado al validar el archivo: " + e.getMessage();
      }
    } 
}
