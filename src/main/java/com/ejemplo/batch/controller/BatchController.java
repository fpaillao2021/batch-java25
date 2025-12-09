package com.ejemplo.batch.controller;

import com.ejemplo.batch.model.RegistroCSV;
import com.ejemplo.batch.repository.RegistroRepository;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;

import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/batch")
public class BatchController {

    @Autowired
    private JobOperator jobLauncher;

    @Autowired
    private Job importUserJob; 

    @Autowired
    private RegistroRepository registroRepository;

    // --- 1. Ejecutar el Proceso Batch ---
    @PostMapping("/ejecutar/{filename}")
    public String runBatchJob(@PathVariable String filename) {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                // Parámetro para pasar la ruta del archivo al Reader
                .addString("file.input", "data/" + filename) 
                .toJobParameters();

            jobLauncher.run(importUserJob, jobParameters);
            return "El Job de Batch ha sido enviado. Revisa el estado.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error al ejecutar el Job: " + e.getMessage();
        }
    }
    
    // NOTA: Para consultar el estado del Job, podrías inyectar JobExplorer y usar
    // jobExplorer.getJobInstances(jobName, start, count) para ver todos los estados.

    // --- 2. Consultar Registros Procesados ---
    @GetMapping("/registros")
    public List<RegistroCSV> getAllRegistros() {
        return registroRepository.findAll();
    }

    // --- 3. Consultar Detalles de un Registro ---
    @GetMapping("/registros/{id}")
    public Optional<RegistroCSV> getRegistroById(@PathVariable Long id) {
        return registroRepository.findById(id);
    }
}