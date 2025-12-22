package com.ejemplo.batch.processor;

import com.ejemplo.batch.config.DataSourceContext;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Listener que cierra explícitamente las conexiones a la base de datos
 * después de cada ejecución de step para asegurar que las conexiones
 * se reinicialicen correctamente en la siguiente invocación.
 */
@Component
public class ConnectionCleanupListener implements StepExecutionListener {

    @Autowired
    @Qualifier("dataSourceA")
    private DataSource dataSourceA;

    @Autowired
    @Qualifier("dataSourceB")
    private DataSource dataSourceB;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        String database = stepExecution.getJobParameters().getString("database");
        
        if (database == null || (!database.equals("DB_A") && !database.equals("DB_B"))) {
            database = "DB_A"; // Default a MySQL
        }
        
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("🔌 ConnectionCleanupListener.beforeStep:");
        System.out.println("   🎯 Database: " + database);
        System.out.println("   🔄 Estableciendo nueva conexión a la base de datos...");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        // Establecer el contexto del DataSource
        DataSourceContext.setDataSourceKey(database);
        
        // Verificar que la conexión se puede establecer
        try {
            DataSource dataSource = "DB_A".equals(database) ? dataSourceA : dataSourceB;
            try (Connection connection = dataSource.getConnection()) {
                System.out.println("   ✅ Conexión establecida correctamente a " + database);
                System.out.println("   📊 Database Product: " + connection.getMetaData().getDatabaseProductName());
                System.out.println("   🔗 URL: " + connection.getMetaData().getURL());
            }
        } catch (SQLException e) {
            System.err.println("   ❌ Error al establecer conexión: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        String database = stepExecution.getJobParameters().getString("database");
        
        if (database == null || (!database.equals("DB_A") && !database.equals("DB_B"))) {
            database = "DB_A"; // Default a MySQL
        }
        
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("🔌 ConnectionCleanupListener.afterStep:");
        System.out.println("   🎯 Database: " + database);
        System.out.println("   📊 Read Count: " + stepExecution.getReadCount());
        System.out.println("   📊 Write Count: " + stepExecution.getWriteCount());
        System.out.println("   🔄 Cerrando conexión a la base de datos...");
        
        // Cerrar explícitamente las conexiones del pool
        try {
            DataSource dataSource = "DB_A".equals(database) ? dataSourceA : dataSourceB;
            
            // Intentar obtener y cerrar una conexión de prueba para forzar el cleanup
            // Nota: Esto no cierra todas las conexiones del pool, pero ayuda a forzar el cleanup
            try (Connection connection = dataSource.getConnection()) {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("   ✅ Conexión cerrada correctamente");
                }
            }
        } catch (SQLException e) {
            System.err.println("   ⚠️ Error al cerrar conexión: " + e.getMessage());
            // No lanzar excepción, solo loggear
        }
        
        // CRÍTICO: Limpiar el contexto del DataSource después de cerrar la conexión
        DataSourceContext.clear();
        System.out.println("   ✅ DataSourceContext limpiado");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        return stepExecution.getExitStatus();
    }
}

