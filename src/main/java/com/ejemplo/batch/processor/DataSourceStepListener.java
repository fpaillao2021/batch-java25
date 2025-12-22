package com.ejemplo.batch.processor;

import com.ejemplo.batch.config.DataSourceContext;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.stereotype.Component;

/**
 * Listener que establece el DataSourceContext antes de ejecutar el step.
 * Lee el parámetro "database" de los JobParameters y lo establece en el contexto.
 * También lo guarda en el ExecutionContext para que esté disponible en ItemStream.open().
 */
@Component
public class DataSourceStepListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        // Leer el parámetro "database" de los JobParameters
        String database = stepExecution.getJobParameters().getString("database");
        
        if (database == null || (!database.equals("DB_A") && !database.equals("DB_B"))) {
            database = "DB_A"; // Default a MySQL
        }
        
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("🔧 DataSourceStepListener.beforeStep:");
        System.out.println("   🎯 Database: " + database);
        System.out.println("   🧵 Thread: " + Thread.currentThread().getName());
        System.out.println("   ⏰ Timestamp: " + java.time.LocalDateTime.now());
        System.out.println("═══════════════════════════════════════════════════════════");
        
        // CRÍTICO: Establecer el contexto del ThreadLocal ANTES de cualquier operación
        DataSourceContext.setDataSourceKey(database);
        
        // CRÍTICO: Guardar en el ExecutionContext para que esté disponible en todo momento
        // Esto asegura que el contexto esté disponible incluso si el ThreadLocal se pierde
        stepExecution.getExecutionContext().putString("database", database);
        stepExecution.getExecutionContext().putString("datasource.key", database);
        
        System.out.println("✅ DataSourceContext establecido a: " + DataSourceContext.getDataSourceKey());
        System.out.println("✅ ExecutionContext['database'] = " + stepExecution.getExecutionContext().getString("database"));
        System.out.println("✅ ExecutionContext['datasource.key'] = " + stepExecution.getExecutionContext().getString("datasource.key"));
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("🔧 DataSourceStepListener.afterStep: Step completado");
        System.out.println("   📊 Exit Status: " + stepExecution.getExitStatus());
        System.out.println("   📝 Write Count: " + stepExecution.getWriteCount());
        System.out.println("   📖 Read Count: " + stepExecution.getReadCount());
        System.out.println("   🔧 Contexto actual: " + DataSourceContext.getDataSourceKey());
        System.out.println("   📁 Archivo procesado: " + stepExecution.getJobParameters().getString("file.input"));
        
        // CRÍTICO: Verificar si el archivo se leyó correctamente
        if (stepExecution.getReadCount() == 0 && "COMPLETED".equals(stepExecution.getExitStatus().getExitCode())) {
            System.err.println("   ⚠️ WARNING: Read Count es 0 pero el step completó exitosamente!");
            System.err.println("   ⚠️ Esto indica que el archivo no se leyó correctamente");
            System.err.println("   📁 Archivo esperado: " + stepExecution.getJobParameters().getString("file.input"));
        }
        
        System.out.println("═══════════════════════════════════════════════════════════");
        // CRÍTICO: NO limpiar el contexto aquí porque el writer puede aún estar escribiendo
        // El contexto se limpiará automáticamente cuando el thread termine
        // DataSourceContext.clear(); // COMENTADO: No limpiar aquí para evitar problemas de concurrencia
        return stepExecution.getExitStatus();
    }
}

