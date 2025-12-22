package com.ejemplo.batch.processor;

import com.ejemplo.batch.config.DatabaseConnectionFactory;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager que crea y destruye todos los componentes de base de datos
 * para cada ejecución de step, asegurando que no haya estado compartido.
 */
@Component
public class DatabaseLifecycleManager implements StepExecutionListener {

    @Autowired
    private DatabaseConnectionFactory connectionFactory;

    // ThreadLocal para almacenar los componentes creados para cada thread
    private static final ThreadLocal<DatabaseComponents> components = new ThreadLocal<>();

    private static class DatabaseComponents {
        DataSource dataSource;
        EntityManagerFactory entityManagerFactory;
        PlatformTransactionManager transactionManager;
        String database;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        String database = stepExecution.getJobParameters().getString("database");
        
        if (database == null || (!database.equals("DB_A") && !database.equals("DB_B"))) {
            database = "DB_A"; // Default a MySQL
        }
        
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("🏭 DatabaseLifecycleManager.beforeStep:");
        System.out.println("   🎯 Database: " + database);
        System.out.println("   🔄 Creando nuevos componentes de base de datos...");
        System.out.println("   🧵 Thread: " + Thread.currentThread().getName());
        System.out.println("═══════════════════════════════════════════════════════════");
        
        // CRÍTICO: Crear nuevos componentes para esta ejecución
        DatabaseComponents dbComponents = new DatabaseComponents();
        dbComponents.database = database;
        dbComponents.dataSource = connectionFactory.createDataSource(database);
        dbComponents.entityManagerFactory = connectionFactory.createEntityManagerFactory(
            dbComponents.dataSource, database);
        dbComponents.transactionManager = connectionFactory.createTransactionManager(
            dbComponents.entityManagerFactory);
        
        // Almacenar en ThreadLocal para que esté disponible durante toda la ejecución
        components.set(dbComponents);
        
        System.out.println("   ✅ Componentes creados y almacenados en ThreadLocal");
        System.out.println("═══════════════════════════════════════════════════════════");
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        DatabaseComponents dbComponents = components.get();
        
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("🏭 DatabaseLifecycleManager.afterStep:");
        System.out.println("   🎯 Database: " + (dbComponents != null ? dbComponents.database : "N/A"));
        System.out.println("   📊 Read Count: " + stepExecution.getReadCount());
        System.out.println("   📊 Write Count: " + stepExecution.getWriteCount());
        System.out.println("   🔄 Cerrando y destruyendo componentes de base de datos...");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        if (dbComponents != null) {
            // Cerrar y destruir todos los componentes
            connectionFactory.closeDatabaseComponents(
                dbComponents.entityManagerFactory, 
                dbComponents.dataSource);
            
            // Limpiar el ThreadLocal
            components.remove();
            
            System.out.println("   ✅ Componentes cerrados y ThreadLocal limpiado");
        } else {
            System.out.println("   ⚠️ No hay componentes para cerrar");
        }
        
        System.out.println("═══════════════════════════════════════════════════════════");
        return stepExecution.getExitStatus();
    }

    /**
     * Obtiene el EntityManagerFactory actual para este thread
     */
    public static EntityManagerFactory getCurrentEntityManagerFactory() {
        DatabaseComponents dbComponents = components.get();
        return dbComponents != null ? dbComponents.entityManagerFactory : null;
    }

    /**
     * Obtiene el TransactionManager actual para este thread
     */
    public static PlatformTransactionManager getCurrentTransactionManager() {
        DatabaseComponents dbComponents = components.get();
        return dbComponents != null ? dbComponents.transactionManager : null;
    }

    /**
     * Obtiene el DataSource actual para este thread
     */
    public static DataSource getCurrentDataSource() {
        DatabaseComponents dbComponents = components.get();
        return dbComponents != null ? dbComponents.dataSource : null;
    }
}

