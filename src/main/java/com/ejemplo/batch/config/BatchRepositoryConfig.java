package com.ejemplo.batch.config;

import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * Configuración de JobRepository separados para cada base de datos.
 * Cada JobRepository almacena sus metadatos en su respectiva base de datos.
 */
@Configuration
public class BatchRepositoryConfig {

    /**
     * JobRepository para DB_A (MySQL).
     * Almacena los metadatos de Spring Batch en MySQL.
     * Este es el JobRepository por defecto que usa @EnableBatchProcessing.
     */
    @Bean(name = "jobRepository")
    @Primary
    public JobRepository jobRepository(
            @Qualifier("dataSourceA") DataSource dataSourceA,
            @Qualifier("transactionManagerDB_A") PlatformTransactionManager transactionManager) throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSourceA);
        factory.setTransactionManager(transactionManager);
        // CRÍTICO: Especificar "MYSQL" para forzar el uso de tablas de secuencia
        // Esto evita que Spring Batch intente usar secuencias nativas
        factory.setDatabaseType("MYSQL");
        factory.setTablePrefix("BATCH_");
        // CRÍTICO: Usar ISOLATION_DEFAULT para que Spring Batch maneje el nivel de aislamiento automáticamente
        // Esto evita conflictos de versión optimista al crear JobExecution
        factory.setIsolationLevelForCreate("ISOLATION_DEFAULT");
        factory.setMaxVarCharLength(1000);
        // CRÍTICO: Deshabilitar validación de estado de transacción para evitar OptimisticLockingFailureException
        factory.setValidateTransactionState(false);
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    /**
     * JobRepository para DB_A (MySQL) - alias específico.
     * Almacena los metadatos de Spring Batch en MySQL.
     */
    @Bean(name = "jobRepositoryDB_A")
    public JobRepository jobRepositoryDB_A(
            @Qualifier("jobRepository") JobRepository jobRepository) {
        return jobRepository;
    }

    /**
     * JobRepository para DB_B (PostgreSQL).
     * Almacena los metadatos de Spring Batch en PostgreSQL.
     */
    @Bean(name = "jobRepositoryDB_B")
    public JobRepository jobRepositoryDB_B(
            @Qualifier("dataSourceB") DataSource dataSourceB,
            @Qualifier("transactionManagerDB_B") PlatformTransactionManager transactionManager) throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSourceB);
        factory.setTransactionManager(transactionManager);
        // CRÍTICO: Usar "POSTGRES" (no "POSTGRESQL") para que Spring Batch use secuencias nativas de PostgreSQL
        // Las secuencias nativas se crean en init-postgres.sql
        factory.setDatabaseType("POSTGRES");
        factory.setTablePrefix("BATCH_");
        // CRÍTICO: Usar ISOLATION_DEFAULT para que Spring Batch maneje el nivel de aislamiento automáticamente
        // Esto evita conflictos de versión optimista al crear JobExecution
        factory.setIsolationLevelForCreate("ISOLATION_DEFAULT");
        factory.setMaxVarCharLength(1000);
        // CRÍTICO: Deshabilitar validación de estado de transacción para evitar OptimisticLockingFailureException
        factory.setValidateTransactionState(false);
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    /**
     * TransactionManager para DB_A (MySQL).
     */
    @Bean(name = "transactionManagerDB_A")
    public PlatformTransactionManager transactionManagerDB_A(@Qualifier("dataSourceA") DataSource dataSourceA) {
        return new org.springframework.jdbc.datasource.DataSourceTransactionManager(dataSourceA);
    }

    /**
     * TransactionManager para DB_B (PostgreSQL).
     */
    @Bean(name = "transactionManagerDB_B")
    public PlatformTransactionManager transactionManagerDB_B(@Qualifier("dataSourceB") DataSource dataSourceB) {
        return new org.springframework.jdbc.datasource.DataSourceTransactionManager(dataSourceB);
    }

    /**
     * JobLauncher para DB_A (MySQL).
     * Usa el JobRepository de MySQL para ejecutar jobs de DB_A.
     * CRÍTICO: Usar SyncTaskExecutor en lugar de SimpleAsyncTaskExecutor para evitar
     * OptimisticLockingFailureException causado por problemas de concurrencia.
     */
    @Bean(name = "jobLauncherDB_A")
    public JobLauncher jobLauncherDB_A(@Qualifier("jobRepositoryDB_A") JobRepository jobRepositoryDB_A) throws Exception {
        TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepositoryDB_A);
        // CRÍTICO: Usar SyncTaskExecutor para ejecutar el job de forma síncrona en el mismo thread
        // Esto evita problemas de concurrencia y OptimisticLockingFailureException
        jobLauncher.setTaskExecutor(new SyncTaskExecutor());
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

    /**
     * JobLauncher para DB_B (PostgreSQL).
     * Usa el JobRepository de PostgreSQL para ejecutar jobs de DB_B.
     * CRÍTICO: Usar SyncTaskExecutor en lugar de SimpleAsyncTaskExecutor para evitar
     * OptimisticLockingFailureException causado por problemas de concurrencia.
     */
    @Bean(name = "jobLauncherDB_B")
    public JobLauncher jobLauncherDB_B(@Qualifier("jobRepositoryDB_B") JobRepository jobRepositoryDB_B) throws Exception {
        TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepositoryDB_B);
        // CRÍTICO: Usar SyncTaskExecutor para ejecutar el job de forma síncrona en el mismo thread
        // Esto evita problemas de concurrencia y OptimisticLockingFailureException
        jobLauncher.setTaskExecutor(new SyncTaskExecutor());
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }
}

