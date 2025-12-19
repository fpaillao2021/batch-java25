package com.ejemplo.batch.controller;

import com.ejemplo.batch.model.RegistroCSV;
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
     * 
     * @param filename archivo a procesar
     * @param database base de datos destino (DB_A o DB_B)
     *                 Se obtiene del header X-Database, por defecto DB_A
     * @return mensaje de estado
     */
    @PostMapping("/ejecutar/{filename}")
    public String runBatchJob(
            @PathVariable String filename,
            @RequestHeader(value = "X-Database", defaultValue = "DB_A") String database) {
        return jobRegistryService.runBatchJob(filename, database);
    }

    /**
     * Consultar todos los Registros Procesados
     * 
     * @param database base de datos de consulta (DB_A o DB_B)
     *                 Se obtiene del header X-Database, por defecto DB_A
     * @return lista de registros
     */
    @GetMapping("/registros")
    public List<RegistroCSV> getAllRegistros(
            @RequestHeader(value = "X-Database", defaultValue = "DB_A") String database) {
        return jobRegistryService.getAllRegistros();
    }

    /**
     * Consultar Detalles de un Registro
     * 
     * @param id identificador del registro
     * @param database base de datos de consulta (DB_A o DB_B)
     *                 Se obtiene del header X-Database, por defecto DB_A
     * @return registro encontrado
     */
    @GetMapping("/registros/{id}")
    public Optional<RegistroCSV> getRegistroById(
            @PathVariable Long id,
            @RequestHeader(value = "X-Database", defaultValue = "DB_A") String database) {
        return jobRegistryService.getRegistroById(id);
    }
}