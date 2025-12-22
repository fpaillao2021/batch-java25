package com.ejemplo.batch.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import jakarta.persistence.EntityManagerFactory;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuración de múltiples DataSources.
 * Define dos datasources: uno para MySQL (DB_A) y otro para PostgreSQL (DB_B).
 */
@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource-a.url}")
    private String primaryUrl;

    @Value("${spring.datasource-a.username}")
    private String primaryUsername;

    @Value("${spring.datasource-a.password}")
    private String primaryPassword;

    @Value("${spring.datasource-a.driver-class-name}")
    private String primaryDriver;

    @Value("${spring.datasource-b.url}")
    private String secondaryUrl;

    @Value("${spring.datasource-b.username}")
    private String secondaryUsername;

    @Value("${spring.datasource-b.password}")
    private String secondaryPassword;

    @Value("${spring.datasource-b.driver-class-name}")
    private String secondaryDriver;

    /**
     * Crea el DataSource primario (MySQL - DB_A).
     * 
     * IMPORTANTE: Marcado como @Primary para que Spring Batch JobRepository
     * SIEMPRE use este DataSource para almacenar los metadatos (BATCH_* tables).
     * Esto evita conflictos cuando ambas bases de datos están activas.
     * 
     * Los datos de aplicación seguirán usando el routingDataSource según el contexto.
     *
     * @return DataSource configurado para MySQL
     */
    @Bean(name = "dataSourceA")
    @Primary
    public DataSource dataSourceA() {
        return DataSourceBuilder.create()
                .driverClassName(primaryDriver)
                .url(primaryUrl)
                .username(primaryUsername)
                .password(primaryPassword)
                .build();
    }

    /**
     * Crea el DataSource secundario (PostgreSQL - DB_B).
     *
     * @return DataSource configurado para PostgreSQL
     */
    @Bean(name = "dataSourceB")
    public DataSource dataSourceB() {
        return DataSourceBuilder.create()
                .driverClassName(secondaryDriver)
                .url(secondaryUrl)
                .username(secondaryUsername)
                .password(secondaryPassword)
                .build();
    }

    /**
     * Crea el DataSource de enrutamiento dinámico.
     * Este DataSource se usa SOLO para los datos de aplicación (tablas registrocsv).
     * Redirige automáticamente entre dataSourceA y dataSourceB
     * según el contexto definido en DataSourceContext.
     * 
     * IMPORTANTE: NO está marcado como @Primary porque el JobRepository de Spring Batch
     * debe usar SIEMPRE dataSourceA para los metadatos (BATCH_* tables).
     * Solo el EntityManagerFactory usa este routingDataSource para los datos de aplicación.
     *
     * @return DataSource dinámico de enrutamiento
     */
    @Bean
    public DataSource routingDataSource() {
        DynamicRoutingDataSource routingDataSource = new DynamicRoutingDataSource();

        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put("DB_A", dataSourceA());
        dataSourceMap.put("DB_B", dataSourceB());

        routingDataSource.setTargetDataSources(dataSourceMap);
        routingDataSource.setDefaultTargetDataSource(dataSourceA()); // DB_A por defecto

        return routingDataSource;
    }

    /**
     * Crea el PlatformTransactionManager para transacciones JPA.
     * Necesario para Spring Batch cuando se usa JpaItemWriter.
     * 
     * IMPORTANTE: JpaItemWriter requiere JpaTransactionManager, no DataSourceTransactionManager.
     * El JpaTransactionManager maneja las transacciones del EntityManager correctamente.
     *
     * @param entityManagerFactory El EntityManagerFactory para JPA
     * @return Transaction Manager configurado para JPA
     */
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }

    /**
     * Crea el EntityManagerFactory para JPA.
     * IMPORTANTE: Este EntityManagerFactory NO se usa durante la ejecución de jobs.
     * Se usa solo para inicialización de Spring Data JPA.
     * Los jobs usan el EntityManagerFactory creado dinámicamente por DatabaseConnectionFactory.
     *
     * @return EntityManagerFactory configurado para múltiples datasources
     */
    @Bean
    @Lazy
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        
        // CRÍTICO: Usar dataSourceA como default durante el inicio
        // El routingDataSource se usará solo cuando se ejecute un job
        // Durante el inicio, no hay contexto establecido, por lo que usamos el DataSource por defecto
        em.setDataSource(dataSourceA());
        
        // Especificar el paquete donde están las entidades
        em.setPackagesToScan("com.ejemplo.batch.model");
        
        // Configurar el vendor adapter de Hibernate
        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        
        // Propiedades de Hibernate
        Map<String, Object> properties = new HashMap<>();
        // CRÍTICO: Especificar el dialect explícitamente para evitar que Hibernate intente conectarse durante el inicio
        // Usamos MySQLDialect como default porque dataSourceA es MySQL
        // En Hibernate 7.x, el dialect correcto es org.hibernate.dialect.MySQLDialect (no MySQL8Dialect)
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        // CRÍTICO: Usar 'none' para evitar que Hibernate intente conectarse durante el inicio
        // Los EntityManagerFactory dinámicos creados por DatabaseConnectionFactory usarán 'update'
        properties.put("hibernate.hbm2ddl.auto", "none");
        // CRÍTICO: Deshabilitar la validación de schema durante el inicio
        properties.put("hibernate.temp.use_jdbc_metadata_defaults", "false");
        // CRÍTICO: No intentar obtener metadata de JDBC durante el inicio
        properties.put("hibernate.jdbc.use_get_generated_keys", "false");
        properties.put("hibernate.show_sql", "false");
        properties.put("hibernate.format_sql", "false");
        properties.put("hibernate.jdbc.batch_size", "10");
        // IMPORTANTE: Configuración para transacciones JPA
        // Tanto MySQL como PostgreSQL requieren estas propiedades para funcionar correctamente con JPA
        properties.put("hibernate.connection.provider_disables_autocommit", "true");
        properties.put("hibernate.connection.autocommit", "false");
        // Estrategia de naming: RegistroCSV -> registro_csv, fechaProceso -> fecha_proceso
        // Esto asegura que ambas BD (MySQL y PostgreSQL) usen los mismos nombres de tablas
        properties.put("hibernate.physical_naming_strategy", 
                "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
        
        em.setJpaPropertyMap(properties);
        
        // CRÍTICO: No inicializar el EntityManagerFactory durante el inicio
        // Esto evitará que Hibernate intente conectarse a la base de datos
        em.setBootstrapExecutor(null);
        
        return em;
    }
}
