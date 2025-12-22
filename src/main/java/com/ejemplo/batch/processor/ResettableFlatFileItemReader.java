package com.ejemplo.batch.processor;

import com.ejemplo.batch.model.RegistroCSV;
import org.springframework.batch.infrastructure.item.ItemStreamReader;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.ExecutionContext;

/**
 * Wrapper del FlatFileItemReader que fuerza la reinicialización completa
 * entre ejecuciones para evitar problemas de caché.
 */
public class ResettableFlatFileItemReader implements ItemStreamReader<RegistroCSV> {

    private final FlatFileItemReader<RegistroCSV> delegate;
    private boolean opened = false;

    public ResettableFlatFileItemReader(FlatFileItemReader<RegistroCSV> delegate) {
        this.delegate = delegate;
    }

    @Override
    public RegistroCSV read() throws Exception {
        return delegate.read();
    }

    @Override
    public void open(ExecutionContext executionContext) {
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("🔄 ResettableFlatFileItemReader.open():");
        System.out.println("   📁 Reader name: " + delegate.getName());
        System.out.println("   🔄 Estado anterior (opened): " + opened);
        
        // CRÍTICO: Cerrar el reader si ya estaba abierto antes de abrirlo de nuevo
        if (opened) {
            System.out.println("   ⚠️ Reader ya estaba abierto, cerrando primero...");
            try {
                delegate.close();
                System.out.println("   ✅ Reader cerrado correctamente");
            } catch (Exception e) {
                System.err.println("   ⚠️ Error al cerrar reader: " + e.getMessage());
            }
        }
        
        // Abrir el reader
        delegate.open(executionContext);
        opened = true;
        
        System.out.println("   ✅ Reader abierto correctamente");
        System.out.println("═══════════════════════════════════════════════════════════");
    }

    @Override
    public void update(ExecutionContext executionContext) {
        delegate.update(executionContext);
    }

    @Override
    public void close() {
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("🔄 ResettableFlatFileItemReader.close():");
        System.out.println("   📁 Reader name: " + delegate.getName());
        
        if (opened) {
            try {
                delegate.close();
                opened = false;
                System.out.println("   ✅ Reader cerrado correctamente");
                
                // CRÍTICO: Pequeño delay para asegurar que el reader se cierre completamente
                // antes de que se cierre la conexión a la base de datos
                Thread.sleep(50);
            } catch (Exception e) {
                System.err.println("   ⚠️ Error al cerrar reader: " + e.getMessage());
                opened = false;
            }
        } else {
            System.out.println("   ⚠️ Reader no estaba abierto");
        }
        
        System.out.println("═══════════════════════════════════════════════════════════");
    }
}

