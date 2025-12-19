package com.ejemplo.batch.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
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
     * @return DataSource configurado para MySQL
     */
    @Bean(name = "dataSourceA")
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
     * Este es el DataSource principal que usa la aplicación.
     * Redirige automáticamente entre dataSourceA y dataSourceB
     * según el contexto definido en DataSourceContext.
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
     * Crea el PlatformTransactionManager para transacciones distribuidas.
     * Necesario para Spring Batch y operaciones transaccionales.
     * Usa DataSourceTransactionManager que funciona directamente con los DataSources.
     *
     * @return Transaction Manager configurado para enrutamiento de datasources
     */
    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(routingDataSource());
    }

    /**
     * Crea el EntityManagerFactory para JPA.
     * Necesario para Spring Batch cuando usa JpaItemWriter.
     * Usa el datasource de enrutamiento dinámico.
     *
     * @return EntityManagerFactory configurado para múltiples datasources
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        
        // Usar el datasource de enrutamiento dinámico
        em.setDataSource(routingDataSource());
        
        // Especificar el paquete donde están las entidades
        em.setPackagesToScan("com.ejemplo.batch.model");
        
        // Configurar el vendor adapter de Hibernate
        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        
        // Propiedades de Hibernate
        Map<String, Object> properties = new HashMap<>();
        // Usar un dialect genérico que funcione con múltiples BD
        // En ambiente local se permite 'update' para crear las tablas automáticamente
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.show_sql", "false");
        properties.put("hibernate.format_sql", "false");
        properties.put("hibernate.jdbc.batch_size", "10");
        
        em.setJpaPropertyMap(properties);
        
        return em;
    }
}
