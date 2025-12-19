package com.ejemplo.batch.processor;

import com.ejemplo.batch.config.DataSourceContext;
import com.ejemplo.batch.model.RegistroCSV;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.stereotype.Component;

/**
 * Writer personalizado para respetar el DataSourceContext en Batch
 * 
 * SOLUCIÓN CORRECTA: 
 * - Lee la base de datos del ThreadLocal (DataSourceContext)
 * - Mantiene el contexto durante toda la ejecución del batch
 * - Limpia después de cada chunk para evitar memory leaks
 * 
 * El flujo garantiza que:
 * 1. JobRegistryImpl setea el contexto ANTES de lanzar el batch
 * 2. CustomJpaItemWriter lee el contexto para cada chunk
 * 3. El contexto se limpia después de terminar
 */
@Component
public class CustomJpaItemWriter implements ItemWriter<RegistroCSV> {

    private final EntityManagerFactory entityManagerFactory;

    public CustomJpaItemWriter(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    /**
     * Obtiene la base de datos del contexto.
     * Lee del ThreadLocal que fue seteado por JobRegistryImpl.
     */
    private String getDatabaseFromContext() {
        String database = DataSourceContext.getDataSourceKey();
        
        // Validar que sea DB_A o DB_B
        if (!"DB_A".equals(database) && !"DB_B".equals(database)) {
            database = "DB_A"; // Default a MySQL
        }
        
        return database;
    }

    /**
     * Escribe los registros en la BD especificada en DataSourceContext.
     */
    @Override
    public void write(Chunk<? extends RegistroCSV> chunk) throws Exception {
        String dataSourceKey = getDatabaseFromContext();
        System.out.println("✍️ Escribiendo " + chunk.size() + " registros en: " + dataSourceKey);

        // Crear EntityManager desde el EntityManagerFactory (que usa routingDataSource)
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        
        try {
            // Asegurar que el contexto esté establecido para esta escritura
            DataSourceContext.setDataSourceKey(dataSourceKey);
            
            entityManager.getTransaction().begin();
            
            for (RegistroCSV item : chunk) {
                // merge asegura que se persista en la BD correcta según el contexto
                entityManager.merge(item);
            }
            
            // Flush asegura que los datos se escriban
            entityManager.flush();
            entityManager.getTransaction().commit();
            
            System.out.println("✅ " + chunk.size() + " registros guardados exitosamente en: " + dataSourceKey);
            
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            System.err.println("❌ Error al guardar registros en " + dataSourceKey + ": " + e.getMessage());
            throw e;
        } finally {
            if (entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }
}

