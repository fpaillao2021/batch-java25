package com.ejemplo.batch.services.impl;

import com.ejemplo.batch.model.RegistroCSV;
import com.ejemplo.batch.repository.RegistroRepository;
import com.ejemplo.batch.services.IJobRegistry;
import com.ejemplo.batch.utils.MessagesLocales;
import com.ejemplo.batch.config.DataSourceContext;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Optional;

@Service
public class JobRegistryImpl implements IJobRegistry {

    @Autowired
    private JobOperator jobLauncher; // Mantener para compatibilidad, pero no usarlo

    @Autowired
    @Qualifier("jobLauncherDB_A")
    private JobLauncher jobLauncherDB_A;

    @Autowired
    @Qualifier("jobLauncherDB_B")
    private JobLauncher jobLauncherDB_B;

    @Autowired
    @Qualifier("jobRepositoryDB_A")
    private JobRepository jobRepositoryDB_A;

    @Autowired
    @Qualifier("jobRepositoryDB_B")
    private JobRepository jobRepositoryDB_B;

    @Autowired
    @Qualifier("importUserJobDB_A")
    private Job importUserJobDB_A;

    @Autowired
    @Qualifier("importUserJobDB_B")
    private Job importUserJobDB_B;

    @Autowired
    private RegistroRepository registroRepository;

    @Value("${file.data.path}")
    private String dataPath;

    /**
     * Ejecuta el proceso batch con el archivo especificado
     * 
     * SOLUCIÓN CORRECTA: 
     * - Setea el DataSourceContext para que esté disponible en el batch thread
     * - NO limpia el contexto aquí (el batch se ejecuta de forma asíncrona)
     * - JobRegistryImpl solo limpia en caso de error de validación
     * 
     * @param filename Nombre del archivo CSV (ej: registros.csv)
     * @param database Base de datos destino: DB_A (MySQL) o DB_B (PostgreSQL)
     * @return Mensaje de resultado o error
     */
    @Override
    public String runBatchJob(String filename, String database) {
        try {
            // Validar y setear la base de datos
            if (database == null || (!database.equals("DB_A") && !database.equals("DB_B"))) {
                database = "DB_A"; // Default a MySQL
            }
            System.out.println("🔄 Ejecutando Batch en: " + database);
            
            // CRÍTICO: Establecer el contexto ANTES de cualquier operación que use el DataSource
            // Esto asegura que el JobRepository use la base de datos correcta para almacenar metadatos
            DataSourceContext.setDataSourceKey(database);
            System.out.println("✅ DataSourceContext establecido a: " + DataSourceContext.getDataSourceKey());
            
            // CRÍTICO: Pequeño delay para asegurar que el contexto se propague y que cualquier reader anterior se cierre
            // Esto es especialmente importante cuando se ejecutan múltiples jobs rápidamente con diferentes archivos
            try {
                Thread.sleep(100); // 100ms para asegurar que el reader anterior se cierre completamente
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // validar nombre y ruta del archivo
            String validationError = validateFile(filename);
            if (validationError != null) {
                DataSourceContext.clear(); // Limpiar contexto en caso de error
                return validationError;
            }

            // crear la ruta completa del archivo
            String filepath = dataPath + "/" + filename;
            
            // CRÍTICO: Verificar que la ruta del archivo es correcta
            File fileCheck = new File(filepath);
            System.out.println("═══════════════════════════════════════════════════════════");
            System.out.println("📁 VERIFICACIÓN DE RUTA DE ARCHIVO:");
            System.out.println("   📂 Directorio de datos (dataPath): " + dataPath);
            System.out.println("   📄 Nombre del archivo: " + filename);
            System.out.println("   📁 Ruta completa (filepath): " + filepath);
            System.out.println("   ✅ Archivo existe: " + fileCheck.exists());
            System.out.println("   ✅ Archivo se puede leer: " + fileCheck.canRead());
            System.out.println("   📏 Tamaño del archivo: " + (fileCheck.exists() ? fileCheck.length() + " bytes" : "N/A"));
            System.out.println("   🧵 Thread: " + Thread.currentThread().getName());
            System.out.println("   📍 Directorio de trabajo actual: " + System.getProperty("user.dir"));
            System.out.println("═══════════════════════════════════════════════════════════");
            
            // CRÍTICO: Si el archivo no existe, intentar con ruta absoluta
            if (!fileCheck.exists()) {
                String absolutePath = new File(dataPath, filename).getAbsolutePath();
                File absoluteFile = new File(absolutePath);
                System.out.println("⚠️ WARNING: Archivo no encontrado en ruta relativa, intentando ruta absoluta:");
                System.out.println("   📁 Ruta absoluta: " + absolutePath);
                System.out.println("   ✅ Archivo existe (absoluta): " + absoluteFile.exists());
                if (absoluteFile.exists()) {
                    filepath = absolutePath;
                    System.out.println("✅ Usando ruta absoluta: " + filepath);
                }
            }

            // Crear parámetros del job incluyendo la BD
            // IMPORTANTE: Usar UUID + timestamp + nanoTime para garantizar unicidad absoluta
            // El JobID debe ser único cada vez, incluso si se ejecuta el mismo archivo en la misma BD
            // Generar UUID único para CADA ejecución
            // CRÍTICO: El UUID debe ser completamente único y ser el parámetro identificador principal
            // Spring Batch genera el JOB_KEY usando un hash de TODOS los parámetros identificadores
            // Por lo tanto, el UUID debe ser diferente en cada ejecución para garantizar unicidad
            // Generar identificador único compuesto para CADA ejecución
            // CRÍTICO: Combinar Database + UUID + Random + Timestamp + NanoTime para garantizar unicidad absoluta
            // Spring Batch genera el JOB_KEY usando TODOS los parámetros identificadores
            // IMPORTANTE: El database DEBE estar en el identificador porque ejecutar el mismo archivo
            // en DB_A vs DB_B son instancias diferentes de job
            // SOLUCIÓN: Usar Database + UUID + Random + Timestamp + NanoTime como parámetro identificador único
            String uuid = java.util.UUID.randomUUID().toString();
            long timestamp = System.currentTimeMillis();
            long nanoTime = System.nanoTime();
            int randomValue = (int)(Math.random() * Integer.MAX_VALUE); // Random adicional para mayor seguridad
            // Combinar Database + UUID + Random + Timestamp + NanoTime para crear un identificador único compuesto
            // El database al inicio garantiza que DB_A y DB_B sean instancias diferentes
            String uniqueIdentifier = database + "_" + uuid + "_" + randomValue + "_" + timestamp + "_" + nanoTime;
            String uniqueJobId = database + "_" + timestamp + "_" + nanoTime + "_" + uuid.replace("-", "") + "_" + randomValue;
            
            System.out.println("🆔 JobID generado: " + uniqueJobId);
            System.out.println("🔑 Identificador único compuesto: " + uniqueIdentifier);
            System.out.println("🎲 Random adicional: " + randomValue);
            System.out.println("🗄️  Database incluido en identificador: " + database);
            
            // Crear JobParameters
            // ESTRATEGIA: Usar el identificador compuesto (Database + UUID + Random + Timestamp + NanoTime) como parámetro identificador único
            // Esto garantiza:
            // 1. Que DB_A y DB_B sean instancias diferentes (database al inicio)
            // 2. Que cada ejecución sea única (UUID + Random + Timestamp + NanoTime)
            JobParameters jobParameters = new JobParametersBuilder()
                .addString("execution.uuid", uniqueIdentifier, true)  // ⭐ ÚNICO parámetro identificador - Database + UUID + Random + Timestamp + NanoTime
                .addString("JobID", uniqueJobId, false)  // Informativo - NO identificador
                .addString("file.input", filepath, false)  // Informativo - NO identificador
                .addString("database", database, false)  // Informativo - NO identificador (ya está en execution.uuid)
                .addLong("execution.timestamp", timestamp, false)  // Informativo - NO identificador
                .addLong("execution.nanotime", nanoTime, false)  // Informativo - NO identificador
                .addLong("execution.random", (long)randomValue, false)  // Informativo - NO identificador
                .toJobParameters();
            
            System.out.println("📋 JobParameters creados:");
            System.out.println("   ⭐ execution.uuid (IDENTIFICADOR): " + uniqueIdentifier);
            System.out.println("   📄 JobID (informativo): " + uniqueJobId);
            System.out.println("   📁 file.input (informativo): " + filepath);
            System.out.println("   🗄️  database (informativo): " + database);
            System.out.println("   ⏰ execution.timestamp (informativo): " + timestamp);
            System.out.println("   ⏱️  execution.nanotime (informativo): " + nanoTime);
            System.out.println("   🎲 execution.random (informativo): " + randomValue);
            
            // CRÍTICO: Verificar que el contexto sigue establecido antes de ejecutar el job
            String currentContext = DataSourceContext.getDataSourceKey();
            System.out.println("🔍 Verificando contexto antes de ejecutar job: " + currentContext);
            if (!currentContext.equals(database)) {
                System.out.println("⚠️ WARNING: Contexto no coincide, reestableciendo...");
                DataSourceContext.setDataSourceKey(database);
            }

            // Seleccionar el job y JobLauncher correctos según la base de datos
            Job selectedJob;
            JobLauncher selectedJobLauncher;
            if ("DB_A".equals(database)) {
                selectedJob = importUserJobDB_A;
                selectedJobLauncher = jobLauncherDB_A;
                System.out.println("═══════════════════════════════════════════════════════════");
                System.out.println("🚀 EJECUTANDO JOB PARA DB_A (MySQL)");
                System.out.println("   📋 Job: " + selectedJob.getName());
                System.out.println("   🔧 JobLauncher: jobLauncherDB_A (usa JobRepository de MySQL)");
                System.out.println("   🗄️  Metadatos se almacenarán en: MySQL");
                System.out.println("═══════════════════════════════════════════════════════════");
            } else {
                selectedJob = importUserJobDB_B;
                selectedJobLauncher = jobLauncherDB_B;
                System.out.println("═══════════════════════════════════════════════════════════");
                System.out.println("🚀 EJECUTANDO JOB PARA DB_B (PostgreSQL)");
                System.out.println("   📋 Job: " + selectedJob.getName());
                System.out.println("   🔧 JobLauncher: jobLauncherDB_B (usa JobRepository de PostgreSQL)");
                System.out.println("   🗄️  Metadatos se almacenarán en: PostgreSQL");
                System.out.println("═══════════════════════════════════════════════════════════");
            }
            
            // Ejecutar el job seleccionado con su JobLauncher correspondiente
            // Cada JobLauncher usa su propio JobRepository que almacena metadatos en su respectiva BD
            System.out.println("🚀 Iniciando ejecución del job con contexto: " + DataSourceContext.getDataSourceKey());
            System.out.println("📋 JobParameters: " + jobParameters);
            System.out.println("🔑 execution.uuid (identificador único): " + uniqueIdentifier);
            
            // CRÍTICO: El JobLauncher.run() retorna un JobExecution que contiene el estado del job
            // El JobLauncher es asíncrono, por lo que necesitamos esperar a que termine
            // Si el job ya existe y está completo, Spring Batch NO lo ejecutará nuevamente
            System.out.println("═══════════════════════════════════════════════════════════");
            System.out.println("🚀 ANTES DE EJECUTAR JOB:");
            System.out.println("   🎯 Database: " + database);
            System.out.println("   🔑 execution.uuid: " + uniqueIdentifier);
            System.out.println("   📋 Job Name: " + selectedJob.getName());
            System.out.println("   🔧 JobLauncher: " + (database.equals("DB_A") ? "jobLauncherDB_A" : "jobLauncherDB_B"));
            System.out.println("   🧵 Thread: " + Thread.currentThread().getName());
            System.out.println("   🔍 Contexto DataSource antes de run(): " + DataSourceContext.getDataSourceKey());
            System.out.println("═══════════════════════════════════════════════════════════");
            
            var jobExecution = selectedJobLauncher.run(selectedJob, jobParameters);
            
            System.out.println("═══════════════════════════════════════════════════════════");
            System.out.println("📊 ESTADO INICIAL DEL JOB (RETORNADO POR run()):");
            System.out.println("   🆔 JobExecution ID: " + jobExecution.getId());
            System.out.println("   📋 JobInstance ID: " + jobExecution.getJobInstance().getInstanceId());
            System.out.println("   ✅ Estado: " + jobExecution.getStatus());
            System.out.println("   🏁 Exit Status: " + jobExecution.getExitStatus());
            System.out.println("   ⏰ Start Time: " + jobExecution.getStartTime());
            System.out.println("   ⏰ End Time: " + jobExecution.getEndTime());
            System.out.println("   🔍 ¿Es ejecución nueva? (EndTime == null): " + (jobExecution.getEndTime() == null ? "SÍ" : "NO (ejecución anterior)"));
            
            // CRÍTICO: Verificar si Spring Batch retornó una ejecución anterior
            if (jobExecution.getEndTime() != null) {
                System.err.println("═══════════════════════════════════════════════════════════");
                System.err.println("❌ ERROR CRÍTICO: Spring Batch retornó una ejecución anterior!");
                System.err.println("   ⚠️  El job NO se está ejecutando, está retornando una ejecución previa");
                System.err.println("   ⏰ End Time: " + jobExecution.getEndTime());
                long timeDiff = java.time.Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime()).toMillis();
                System.err.println("   ⏱️  Tiempo de ejecución: " + timeDiff + "ms");
                System.err.println("   🔑 execution.uuid usado: " + uniqueIdentifier);
                System.err.println("   💡 SOLUCIÓN: El UUID debe ser único. Verifica que se está generando correctamente.");
                System.err.println("═══════════════════════════════════════════════════════════");
                DataSourceContext.clear();
                return MessagesLocales.ErrorMensajeLocal.ERROR_EJECUTAR_JOB + 
                    "Spring Batch retornó una ejecución anterior en lugar de ejecutar una nueva. " +
                    "JobExecution ID: " + jobExecution.getId() + ", EndTime: " + jobExecution.getEndTime();
            }
            
            System.out.println("✅ JobExecution es nuevo (EndTime == null), el job se está ejecutando");
            System.out.println("═══════════════════════════════════════════════════════════");
            
            // CRÍTICO: Con SyncTaskExecutor, el job se ejecuta de forma síncrona en el mismo thread
            // Por lo tanto, cuando run() retorna, el job ya ha terminado (o fallado)
            // No necesitamos esperar ni verificar el estado en un loop
            System.out.println("✅ Job ejecutado de forma síncrona (SyncTaskExecutor)");
            System.out.println("   El job ya ha terminado cuando run() retorna");
            
            System.out.println("═══════════════════════════════════════════════════════════");
            System.out.println("📊 ESTADO FINAL DEL JOB:");
            System.out.println("   🆔 JobExecution ID: " + jobExecution.getId());
            System.out.println("   📋 JobInstance ID: " + jobExecution.getJobInstance().getInstanceId());
            System.out.println("   ✅ Estado: " + jobExecution.getStatus());
            System.out.println("   🏁 Exit Status: " + jobExecution.getExitStatus());
            System.out.println("   ⏰ Start Time: " + jobExecution.getStartTime());
            System.out.println("   ⏰ End Time: " + jobExecution.getEndTime());
            System.out.println("═══════════════════════════════════════════════════════════");
            
            // CRÍTICO: Verificar el estado del job después de esperar
            // El JobExecution retornado por run() puede no estar actualizado, así que verificamos
            // el estado actual desde el repositorio si es posible
            
            // Verificar si el job realmente se ejecutó o si Spring Batch retornó una ejecución anterior
            if (jobExecution.getStatus().isUnsuccessful()) {
                System.err.println("═══════════════════════════════════════════════════════════");
                System.err.println("❌ ERROR: El job falló con estado: " + jobExecution.getStatus());
                System.err.println("   🆔 JobExecution ID: " + jobExecution.getId());
                System.err.println("   🏁 Exit Status: " + jobExecution.getExitStatus());
                
                boolean isOptimisticLockingError = false;
                if (jobExecution.getFailureExceptions() != null && !jobExecution.getFailureExceptions().isEmpty()) {
                    System.err.println("   📋 Excepciones encontradas:");
                    for (Throwable ex : jobExecution.getFailureExceptions()) {
                        System.err.println("      💥 Tipo: " + ex.getClass().getName());
                        System.err.println("      💥 Mensaje: " + ex.getMessage());
                        
                        // Verificar si es OptimisticLockingFailureException
                        if (ex.getMessage() != null && ex.getMessage().contains("wrong version")) {
                            isOptimisticLockingError = true;
                            System.err.println("      ⚠️  DETECTADO: OptimisticLockingFailureException en metadatos de Spring Batch");
                            System.err.println("      ℹ️  Esto puede ocurrir cuando múltiples threads intentan actualizar el mismo JobExecution");
                            System.err.println("      ✅ Los datos de aplicación pueden haberse guardado correctamente a pesar del error");
                        }
                        
                        if (ex.getCause() != null) {
                            System.err.println("      💥 Causa: " + ex.getCause().getClass().getName() + " - " + ex.getCause().getMessage());
                            if (ex.getCause().getMessage() != null && ex.getCause().getMessage().contains("wrong version")) {
                                isOptimisticLockingError = true;
                            }
                        }
                        ex.printStackTrace();
                    }
                }
                
                System.err.println("═══════════════════════════════════════════════════════════");
                
                // Si es un error de OptimisticLockingFailureException, verificar si los datos se guardaron
                if (isOptimisticLockingError) {
                    System.err.println("⚠️  ADVERTENCIA: Error de OptimisticLockingFailureException detectado");
                    System.err.println("   ℹ️  Este error ocurre en los metadatos de Spring Batch, no en los datos de aplicación");
                    System.err.println("   ✅ Los datos pueden haberse guardado correctamente a pesar del error");
                    System.err.println("   💡 Verifica manualmente en la base de datos si los datos se guardaron");
                    // No retornar error inmediatamente, permitir que el usuario verifique los datos
                }
                
                DataSourceContext.clear();
                return MessagesLocales.ErrorMensajeLocal.ERROR_EJECUTAR_JOB + 
                    "Estado: " + jobExecution.getStatus() + ", ExitStatus: " + jobExecution.getExitStatus() +
                    (isOptimisticLockingError ? " (OptimisticLockingFailureException - verifica si los datos se guardaron)" : "");
            }
            
            // Con SyncTaskExecutor, el job se ejecuta de forma síncrona, por lo que cuando run() retorna,
            // el job ya ha terminado (o fallado). No necesitamos verificar si aún está ejecutándose.
            if (jobExecution.getEndTime() != null) {
                System.out.println("✅ Job completado exitosamente");
                System.out.println("   ⏰ Tiempo de ejecución: " + 
                    java.time.Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime()).toMillis() + "ms");
            } else {
                System.out.println("⚠️ WARNING: El job no tiene EndTime, puede haber un problema");
                System.out.println("   Estado actual: " + jobExecution.getStatus());
            }
            
            // CRÍTICO: NO limpiar el contexto aquí porque el job puede aún estar ejecutándose
            // El contexto se limpiará automáticamente cuando el thread termine
            // Limpiar aquí puede causar problemas en ejecuciones posteriores
            // DataSourceContext.clear(); // COMENTADO: No limpiar aquí
            
            return MessagesLocales.MensajeLocal.BATCH_EJECUTADO_EXITOSAMENTE + filename + " en " + database + " (JobExecution ID: " + jobExecution.getId() + ")";
            
        } catch (IllegalArgumentException e) {
            // Error de validación del archivo
            DataSourceContext.clear();
            return MessagesLocales.ErrorMensajeLocal.ERROR_VALIDACION_ARCHIVO + e.getMessage();
        } catch (Exception e) {
            // Otros errores durante la ejecución
            DataSourceContext.clear();
            e.printStackTrace();
            return MessagesLocales.ErrorMensajeLocal.ERROR_EJECUTAR_JOB + e.getMessage();
        }
        // ✅ NO hay finally que limpie
        // El contexto será usado mientras el batch procesa datos
    }

    /**
     * Obtiene todos los registros procesados de la BD actual (según DataSourceContext)
     * Si se usa en API, el interceptor habrá establecido la BD correcta
     */
    @Override
    public List<RegistroCSV> getAllRegistros() {
        try {
            return registroRepository.findAll();
        } finally {
            // Limpiar contexto después de consultas de API
            DataSourceContext.clear();
        }
    }

    /**
     * Obtiene un registro por su ID de la BD actual (según DataSourceContext)
     * Si se usa en API, el interceptor habrá establecido la BD correcta
     */
    @Override
    public Optional<RegistroCSV> getRegistroById(Long id) {
        try {
            return registroRepository.findById(id);
        } finally {
            // Limpiar contexto después de consultas de API
            DataSourceContext.clear();
        }
    }

    /**
     * Registra un job en el sistema
     */
    @Override
    public boolean registerJob(String jobName) {
        // Lógica para registrar el job
        System.out.println(MessagesLocales.MensajeLocal.JOB_REGISTRADO + jobName);
        return true;
    }

    /**
     * Valida la existencia y permisos del archivo
     */
    private String validateFile(String fileName) {
      try {
            // Validar que el filename no esté vacío
            if (fileName == null || fileName.trim().isEmpty()) {
                return MessagesLocales.ErrorMensajeLocal.ERROR_FILENAME_VACIO;
            }
            
            String filepath = dataPath + "/" + fileName;
            
            // Validar que el archivo existe ANTES de crear los parámetros
            File file = new File(filepath);
            if (!file.exists()) {
                return MessagesLocales.ErrorMensajeLocal.ERROR_ARCHIVO_NO_EXISTE + fileName + 
                       MessagesLocales.ErrorMensajeLocal.ERROR_ARCHIVO_NO_EXISTE_RUTA + dataPath + 
                       MessagesLocales.ErrorMensajeLocal.ERROR_ARCHIVO_NO_EXISTE_SUFIJO;
            }
            
            if (!file.canRead()) {
                return MessagesLocales.ErrorMensajeLocal.ERROR_PERMISOS_LECTURA + fileName + 
                       MessagesLocales.ErrorMensajeLocal.ERROR_PERMISOS_LECTURA_SUFIJO;
            }
            return null;
      } catch (Exception e) {
            return MessagesLocales.ErrorMensajeLocal.ERROR_INESPERADO_VALIDAR + e.getMessage();
      }
    } 
}
