package com.ejemplo.batch.services;

import com.ejemplo.batch.model.RegistroCSV;
import java.util.List;
import java.util.Optional;

public interface IJobRegistry {
    
    /**
     * Ejecuta el proceso batch con el archivo especificado
     * @param filename Nombre del archivo CSV (ej: registros.csv)
     * @param database Base de datos destino: DB_A (MySQL) o DB_B (PostgreSQL)
     */
    String runBatchJob(String filename, String database);
    
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
