package com.ejemplo.batch.services;

import com.evertecinc.entitydto.app.batch.model.entity.RegistroCSV;
import java.util.List;
import java.util.Optional;

public interface IJobRegistry {
    
    /**
     * Ejecuta el proceso batch con el archivo especificado
     */
    String runBatchJob(String filename);
    
    /**
     * Obtiene todos los registros procesados
     */
    List<RegistroCSV> getAllRegistros();
    
    /**
     * Obtiene un registro por su ID
     */
    Optional<RegistroCSV> getRegistroById(Long id);
    
    /**
     * Registra un job en el sistema
     */
    boolean registerJob(String jobName);
}
