package com.ejemplo.batch.processor;

import com.ejemplo.batch.config.DataSourceContext;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.listener.ChunkListener;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.stereotype.Component;

/**
 * Listener que establece el DataSourceContext antes de escribir cada chunk.
 * Se ejecuta ANTES de que el writer obtenga la conexión de la base de datos.
 * 
 * Esto es crítico porque el AbstractRoutingDataSource lee el contexto
 * cuando se obtiene la conexión, no cuando se escribe.
 */
@Component
public class DataSourceChunkListener implements ChunkListener {

    @Override
    public void beforeChunk(ChunkContext chunkContext) {
        // Obtener el StepContext que contiene los JobParameters y ExecutionContext
        StepContext stepContext = chunkContext.getStepContext();
        if (stepContext != null) {
            // CRÍTICO: Intentar obtener el database del ExecutionContext primero (más confiable)
            // Si no está disponible, obtenerlo de los JobParameters
            String database = stepContext.getStepExecution().getExecutionContext().getString("database");
            
            if (database == null || (!database.equals("DB_A") && !database.equals("DB_B"))) {
                // Fallback: leer de JobParameters
                database = stepContext.getJobParameters().get("database") != null 
                    ? stepContext.getJobParameters().get("database").toString() 
                    : null;
            }
            
            if (database == null || (!database.equals("DB_A") && !database.equals("DB_B"))) {
                database = "DB_A"; // Default a MySQL
            }
            
            System.out.println("═══════════════════════════════════════════════════════════");
            System.out.println("🔧 ANTES DE PROCESAR CHUNK:");
            System.out.println("   🎯 DataSource Key configurado: " + database);
            System.out.println("   🧵 Thread: " + Thread.currentThread().getName());
            System.out.println("   ⏰ Timestamp: " + java.time.LocalDateTime.now());
            System.out.println("   🔍 Contexto actual (ThreadLocal): " + DataSourceContext.getDataSourceKey());
            System.out.println("═══════════════════════════════════════════════════════════");
            
            // CRÍTICO: Establecer el contexto ANTES de que el writer obtenga la conexión
            // Esto debe hacerse en CADA chunk porque el contexto puede perderse entre chunks
            DataSourceContext.setDataSourceKey(database);
            
            // CRÍTICO: También actualizar el ExecutionContext por si acaso
            stepContext.getStepExecution().getExecutionContext().putString("database", database);
            stepContext.getStepExecution().getExecutionContext().putString("datasource.key", database);
            
            System.out.println("✅ DataSourceContext establecido correctamente a: " + DataSourceContext.getDataSourceKey());
            System.out.println("✅ ExecutionContext actualizado");
        }
    }

    @Override
    public void afterChunk(ChunkContext chunkContext) {
        // No limpiar aquí, el contexto debe mantenerse durante la transacción
        String database = DataSourceContext.getDataSourceKey();
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("🔧 DataSourceChunkListener.afterChunk: Chunk procesado");
        System.out.println("   🎯 Contexto actual: " + database);
        System.out.println("   🧵 Thread: " + Thread.currentThread().getName());
        System.out.println("   ⏰ Timestamp: " + java.time.LocalDateTime.now());
        
        // Obtener información del StepExecution para verificar el estado
        if (chunkContext.getStepContext() != null) {
            var stepExecution = chunkContext.getStepContext().getStepExecution();
            if (stepExecution != null) {
                System.out.println("   📊 Write Count: " + stepExecution.getWriteCount());
                System.out.println("   📊 Commit Count: " + stepExecution.getCommitCount());
                System.out.println("   📊 Rollback Count: " + stepExecution.getRollbackCount());
                System.out.println("   📊 Status: " + stepExecution.getStatus());
                System.out.println("   📊 Read Count: " + stepExecution.getReadCount());
                System.out.println("   📊 Filter Count: " + stepExecution.getFilterCount());
                
                // CRÍTICO: Si write_count es 0 pero el job está completado, hay un problema
                if (stepExecution.getWriteCount() == 0 && stepExecution.getStatus().isUnsuccessful() == false) {
                    System.err.println("   ⚠️ WARNING: Write Count es 0 pero el step no falló!");
                    System.err.println("   ⚠️ Esto puede indicar que los datos no se están escribiendo correctamente");
                }
            }
        }
        
        // Verificar que el contexto sigue establecido
        if (database == null) {
            System.err.println("⚠️ WARNING: Contexto perdido después del chunk!");
        } else {
            System.out.println("✅ Contexto mantenido correctamente: " + database);
        }
        System.out.println("═══════════════════════════════════════════════════════════");
    }

    @Override
    public void afterChunkError(ChunkContext chunkContext) {
        System.out.println("❌ DataSourceChunkListener.afterChunkError: Limpiando contexto después de error");
        DataSourceContext.clear();
    }
}

