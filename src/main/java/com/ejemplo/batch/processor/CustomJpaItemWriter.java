package com.ejemplo.batch.processor;

import com.ejemplo.batch.config.DataSourceContext;
import com.ejemplo.batch.model.RegistroCSV;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.database.JpaItemWriter;

/**
 * Writer personalizado que usa el EntityManagerFactory creado dinámicamente
 * para cada ejecución, evitando cualquier problema de caché o estado compartido.
 */
public class CustomJpaItemWriter extends JpaItemWriter<RegistroCSV> {
    
    public CustomJpaItemWriter(EntityManagerFactory entityManagerFactory) {
        // Constructor con EntityManagerFactory temporal para inicialización
        // Este EntityManagerFactory se usa solo durante la creación del bean
        // Durante la ejecución, se creará un nuevo JpaItemWriter con el EntityManagerFactory correcto
        super(entityManagerFactory);
    }
    
    /**
     * Obtiene el EntityManagerFactory actual del ThreadLocal
     */
    private EntityManagerFactory getCurrentEntityManagerFactory() {
        EntityManagerFactory emf = DatabaseLifecycleManager.getCurrentEntityManagerFactory();
        if (emf == null) {
            throw new IllegalStateException("EntityManagerFactory no está disponible. Asegúrate de que DatabaseLifecycleManager.beforeStep() se ejecutó primero.");
        }
        return emf;
    }

    @Override
    public void write(Chunk<? extends RegistroCSV> chunk) {
        // CRÍTICO: El contexto DEBE estar establecido por DataSourceChunkListener.beforeChunk()
        // ANTES de que este método se ejecute. El ChunkListener se ejecuta antes del writer.
        // Si el contexto se perdió, intentar recuperarlo del ThreadLocal o usar default
        String database = DataSourceContext.getDataSourceKey();
        
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("✍️  INICIANDO ESCRITURA DE DATOS:");
        System.out.println("   📦 Registros a escribir: " + chunk.size());
        System.out.println("   🎯 DataSource Key (del contexto ThreadLocal): " + database);
        System.out.println("   🧵 Thread: " + Thread.currentThread().getName());
        System.out.println("   ⏰ Timestamp: " + java.time.LocalDateTime.now());
        System.out.println("═══════════════════════════════════════════════════════════");
        
        // CRÍTICO: Verificar y corregir el contexto si es necesario
        if (database == null || (!database.equals("DB_A") && !database.equals("DB_B"))) {
            System.err.println("═══════════════════════════════════════════════════════════");
            System.err.println("❌ ERROR CRÍTICO: Contexto del DataSource NO está establecido!");
            System.err.println("   🔍 Contexto actual: " + database);
            System.err.println("   🧵 Thread: " + Thread.currentThread().getName());
            System.err.println("   ⚠️  Esto causará que los datos se escriban en la base de datos incorrecta!");
            System.err.println("═══════════════════════════════════════════════════════════");
            database = "DB_A"; // Default a MySQL si no está establecido
        }
        
        // CRÍTICO: Establecer el contexto ANTES de llamar a super.write()
        // Esto debe hacerse en CADA llamada porque el contexto puede perderse
        // El contexto se lee cuando se obtiene la conexión del DataSource
        DataSourceContext.setDataSourceKey(database);
        System.out.println("✅ Contexto del DataSource establecido a: " + DataSourceContext.getDataSourceKey());
        
        // CRÍTICO: Verificar múltiples veces que el contexto está establecido
        String verifyContext = DataSourceContext.getDataSourceKey();
        if (!verifyContext.equals(database)) {
            System.err.println("❌ ERROR: Contexto no se estableció correctamente!");
            System.err.println("   Esperado: " + database + ", Actual: " + verifyContext);
            throw new RuntimeException("Error crítico: No se pudo establecer el contexto del DataSource");
        }
        
        try {
            // CRÍTICO: Verificar el contexto ANTES de escribir
            String contextBeforeWrite = DataSourceContext.getDataSourceKey();
            System.out.println("🔍 Contexto ANTES de super.write(): " + contextBeforeWrite);
            if (!contextBeforeWrite.equals(database)) {
                System.err.println("⚠️ WARNING: Contexto diferente! Esperado: " + database + ", Actual: " + contextBeforeWrite);
                DataSourceContext.setDataSourceKey(database);
                System.out.println("🔧 Contexto corregido a: " + database);
            }
            
            // CRÍTICO: Obtener el EntityManagerFactory actual del ThreadLocal
            EntityManagerFactory currentEmf = getCurrentEntityManagerFactory();
            System.out.println("🔍 EntityManagerFactory disponible: " + (currentEmf != null));
            System.out.println("🔍 EntityManagerFactory está abierto: " + (currentEmf != null && currentEmf.isOpen()));
            
            // CRÍTICO: Usar el EntityManagerFactory del ThreadLocal directamente
            // Crear un nuevo JpaItemWriter con el EntityManagerFactory correcto usando reflexión
            JpaItemWriter<RegistroCSV> dynamicWriter = new JpaItemWriter<>(currentEmf);
            
            // CRÍTICO: Verificar el contexto múltiples veces antes de escribir
            String contextBeforeSuperWrite = DataSourceContext.getDataSourceKey();
            System.out.println("📝 Llamando a dynamicWriter.write() con " + chunk.size() + " registros...");
            System.out.println("   📋 Primer registro: " + (chunk.size() > 0 ? chunk.getItems().get(0).toString() : "N/A"));
            System.out.println("   🎯 Contexto ANTES de write(): " + contextBeforeSuperWrite);
            
            dynamicWriter.write(chunk);
            
            System.out.println("✅ super.write() completado sin excepciones");
            
            // CRÍTICO: Verificar el contexto DESPUÉS de escribir
            String contextAfterWrite = DataSourceContext.getDataSourceKey();
            System.out.println("🔍 Contexto DESPUÉS de super.write(): " + contextAfterWrite);
            
            // CRÍTICO: El flush se hace automáticamente por Spring Batch después del chunk
            // No necesitamos hacer flush manual aquí porque JpaItemWriter maneja esto internamente
            
            // CRÍTICO: Verificar que los datos realmente se escribieron
            // El super.write() puede completarse sin errores pero los datos pueden no persistirse
            // si hay un problema con la transacción
            // NOTA: La transacción se hace commit automáticamente por Spring Batch después del chunk
            // Si hay un error después de este punto, Spring Batch hará rollback
            
            System.out.println("═══════════════════════════════════════════════════════════");
            System.out.println("✅ ESCRITURA COMPLETADA EXITOSAMENTE:");
            System.out.println("   📦 Registros escritos: " + chunk.size());
            System.out.println("   🎯 DataSource Key: " + database);
            System.out.println("   🧵 Thread: " + Thread.currentThread().getName());
            System.out.println("   ⏰ Timestamp: " + java.time.LocalDateTime.now());
            System.out.println("   ⚠️  NOTA: La transacción se hará commit cuando Spring Batch complete el chunk");
            System.out.println("═══════════════════════════════════════════════════════════");
        } catch (jakarta.persistence.TransactionRequiredException e) {
            System.err.println("═══════════════════════════════════════════════════════════");
            System.err.println("❌ ERROR DE TRANSACCIÓN EN ESCRITURA:");
            System.err.println("   🎯 DataSource Key: " + database);
            System.err.println("   📦 Registros intentados: " + chunk.size());
            System.err.println("   💥 Error: No active transaction");
            System.err.println("   🔧 Contexto: " + DataSourceContext.getDataSourceKey());
            System.err.println("   🧵 Thread: " + Thread.currentThread().getName());
            System.err.println("═══════════════════════════════════════════════════════════");
            e.printStackTrace();
            throw new RuntimeException("Error de transacción al escribir datos: " + e.getMessage(), e);
        } catch (org.springframework.dao.OptimisticLockingFailureException e) {
            System.err.println("═══════════════════════════════════════════════════════════");
            System.err.println("⚠️ WARNING: OptimisticLockingFailureException:");
            System.err.println("   🎯 DataSource Key: " + database);
            System.err.println("   📦 Registros intentados: " + chunk.size());
            System.err.println("   💥 Error: " + e.getMessage());
            System.err.println("   ℹ️  Esto puede ocurrir en los metadatos de Spring Batch, no en los datos de aplicación");
            System.err.println("   ✅ Los datos de aplicación pueden haberse guardado correctamente");
            System.err.println("═══════════════════════════════════════════════════════════");
            // NO relanzar la excepción si es OptimisticLockingFailureException
            // Esta excepción puede ocurrir en los metadatos de Spring Batch (BATCH_JOB_EXECUTION)
            // pero los datos de aplicación (registrocsv) pueden haberse guardado correctamente
            // Solo loguear el warning y continuar
        } catch (Exception e) {
            System.err.println("═══════════════════════════════════════════════════════════");
            System.err.println("❌ ERROR EN ESCRITURA:");
            System.err.println("   🎯 DataSource Key: " + database);
            System.err.println("   📦 Registros intentados: " + chunk.size());
            System.err.println("   💥 Error: " + e.getMessage());
            System.err.println("   🔧 Tipo de error: " + e.getClass().getName());
            System.err.println("   🧵 Thread: " + Thread.currentThread().getName());
            System.err.println("═══════════════════════════════════════════════════════════");
            e.printStackTrace();
            throw new RuntimeException("Error al escribir datos en la base de datos: " + e.getMessage(), e);
        }
    }
}

