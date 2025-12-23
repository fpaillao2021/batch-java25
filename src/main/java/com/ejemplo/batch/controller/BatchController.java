package com.ejemplo.batch.controller;

import com.evertecinc.entitydto.app.batch.model.entity.RegistroCSV;
import com.ejemplo.batch.services.IJobRegistry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/batch")
public class BatchController {

    @Autowired
    private IJobRegistry jobRegistryService;

    /**
     * Ejecutar el Proceso Batch
     */
    @PostMapping("/ejecutar/{filename}")
    public String runBatchJob(@PathVariable String filename) {
        return jobRegistryService.runBatchJob(filename);
    }

    /**
     * Consultar todos los Registros Procesados
     */
    @GetMapping("/registros")
    public List<RegistroCSV> getAllRegistros() {
        return jobRegistryService.getAllRegistros();
    }

    /**
     * Consultar Detalles de un Registro
     */
    @GetMapping("/registros/{id}")
    public Optional<RegistroCSV> getRegistroById(@PathVariable Long id) {
        return jobRegistryService.getRegistroById(id);
    }
}