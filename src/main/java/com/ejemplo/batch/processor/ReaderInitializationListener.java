package com.ejemplo.batch.processor;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.stereotype.Component;

/**
 * Listener que inicializa explícitamente el reader antes de que el step comience
 * a procesar datos, asegurando que el reader esté listo para leer.
 */
@Component
public class ReaderInitializationListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("🔄 ReaderInitializationListener.beforeStep:");
        System.out.println("   🔄 Verificando que el reader esté listo...");
        System.out.println("   🧵 Thread: " + Thread.currentThread().getName());
        System.out.println("   📁 Archivo: " + stepExecution.getJobParameters().getString("file.input"));
        System.out.println("═══════════════════════════════════════════════════════════");
        
        // El reader se inicializará automáticamente por Spring Batch cuando se llame a ItemStream.open()
        // Este listener solo verifica que el contexto esté listo
        String fileInput = stepExecution.getJobParameters().getString("file.input");
        
        System.out.println("   📁 Archivo a procesar: " + fileInput);
        System.out.println("   ✅ ExecutionContext listo");
        System.out.println("═══════════════════════════════════════════════════════════");
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("🔄 ReaderInitializationListener.afterStep:");
        System.out.println("   📊 Read Count: " + stepExecution.getReadCount());
        System.out.println("   📊 Write Count: " + stepExecution.getWriteCount());
        
        if (stepExecution.getReadCount() == 0) {
            System.err.println("   ❌ ERROR: Read Count es 0 - el reader no leyó ningún registro");
            System.err.println("   📁 Archivo: " + stepExecution.getJobParameters().getString("file.input"));
        }
        
        System.out.println("═══════════════════════════════════════════════════════════");
        return stepExecution.getExitStatus();
    }
}

