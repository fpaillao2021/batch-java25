package com.ejemplo.batch.processor;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.stereotype.Component;

/**
 * Listener para verificar que el FileReader se está usando correctamente
 * y detectar problemas cuando el archivo no se lee.
 */
@Component
public class FileReaderListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        String fileInput = stepExecution.getJobParameters().getString("file.input");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("📖 FileReaderListener.beforeStep:");
        System.out.println("   📁 Archivo a procesar: " + fileInput);
        System.out.println("   🧵 Thread: " + Thread.currentThread().getName());
        System.out.println("   ⏰ Timestamp: " + java.time.LocalDateTime.now());
        System.out.println("═══════════════════════════════════════════════════════════");
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("📖 FileReaderListener.afterStep:");
        System.out.println("   📊 Read Count: " + stepExecution.getReadCount());
        System.out.println("   📊 Write Count: " + stepExecution.getWriteCount());
        System.out.println("   📊 Filter Count: " + stepExecution.getFilterCount());
        System.out.println("   📊 Commit Count: " + stepExecution.getCommitCount());
        System.out.println("   📊 Rollback Count: " + stepExecution.getRollbackCount());
        System.out.println("   📁 Archivo procesado: " + stepExecution.getJobParameters().getString("file.input"));
        System.out.println("   🏁 Exit Status: " + stepExecution.getExitStatus().getExitCode());
        
        // CRÍTICO: Verificar si el archivo se leyó correctamente
        if (stepExecution.getReadCount() == 0) {
            System.err.println("═══════════════════════════════════════════════════════════");
            System.err.println("❌ ERROR CRÍTICO: Read Count es 0!");
            System.err.println("   ❌ Esto indica que el archivo NO se leyó correctamente");
            System.err.println("   📁 Archivo esperado: " + stepExecution.getJobParameters().getString("file.input"));
            System.err.println("   🏁 Exit Status actual: " + stepExecution.getExitStatus().getExitCode());
            System.err.println("   🔍 Posibles causas:");
            System.err.println("      1. El archivo no existe en la ruta especificada");
            System.err.println("      2. El reader no se reinicializó correctamente después de una ejecución anterior");
            System.err.println("      3. El nombre del archivo contiene caracteres especiales que causan problemas");
            System.err.println("      4. El archivo está vacío o tiene formato incorrecto");
            System.err.println("      5. El reader no se inicializó correctamente antes del step");
            System.err.println("═══════════════════════════════════════════════════════════");
            
            // CRÍTICO: Si el read_count es 0 y el step completó, cambiar el exit status a FAILED
            // Esto evitará que Spring Batch considere el job como completado cuando no se leyó nada
            if ("COMPLETED".equals(stepExecution.getExitStatus().getExitCode())) {
                System.err.println("   ⚠️ Cambiando Exit Status de COMPLETED a FAILED porque no se leyó ningún registro");
                return ExitStatus.FAILED;
            }
        }
        
        System.out.println("═══════════════════════════════════════════════════════════");
        return stepExecution.getExitStatus();
    }
}

