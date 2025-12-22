package com.ejemplo.batch.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory que crea y destruye todos los componentes de base de datos
 * para cada ejecución, evitando cualquier problema de caché o estado compartido.
 */
@Component
public class DatabaseConnectionFactory {

    @Value("${spring.datasource-a.url}")
    private String dataSourceAUrl;
    @Value("${spring.datasource-a.username}")
    private String dataSourceAUsername;
    @Value("${spring.datasource-a.password}")
    private String dataSourceAPassword;
    @Value("${spring.datasource-a.driver-class-name}")
    private String dataSourceADriver;

    @Value("${spring.datasource-b.url}")
    private String dataSourceBUrl;
    @Value("${spring.datasource-b.username}")
    private String dataSourceBUsername;
    @Value("${spring.datasource-b.password}")
    private String dataSourceBPassword;
    @Value("${spring.datasource-b.driver-class-name}")
    private String dataSourceBDriver;

    /**
     * Crea un nuevo DataSource para la base de datos especificada
     */
    public DataSource createDataSource(String database) {
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("🏭 DatabaseConnectionFactory.createDataSource:");
        System.out.println("   🎯 Database: " + database);
        System.out.println("   🔄 Creando nueva instancia de DataSource...");
        
        if ("DB_A".equals(database)) {
            // CRÍTICO: Configurar HikariDataSource con autoCommit=false para MySQL
            HikariConfig config = new HikariConfig();
            config.setDriverClassName(dataSourceADriver);
            config.setJdbcUrl(dataSourceAUrl);
            config.setUsername(dataSourceAUsername);
            config.setPassword(dataSourceAPassword);
            config.setAutoCommit(false); // CRÍTICO: Deshabilitar autoCommit para transacciones
            config.setMaximumPoolSize(5);
            config.setMinimumIdle(1);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            
            HikariDataSource dataSource = new HikariDataSource(config);
            System.out.println("   ✅ DataSource creado para MySQL (DB_A)");
            System.out.println("   🌐 URL: " + dataSourceAUrl);
            System.out.println("   🔄 AutoCommit configurado: false");
            System.out.println("═══════════════════════════════════════════════════════════");
            return dataSource;
        } else {
            // CRÍTICO: Configurar HikariDataSource con autoCommit=false para PostgreSQL
            HikariConfig config = new HikariConfig();
            config.setDriverClassName(dataSourceBDriver);
            config.setJdbcUrl(dataSourceBUrl);
            config.setUsername(dataSourceBUsername);
            config.setPassword(dataSourceBPassword);
            config.setAutoCommit(false); // CRÍTICO: Deshabilitar autoCommit para transacciones
            config.setMaximumPoolSize(5);
            config.setMinimumIdle(1);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            
            HikariDataSource dataSource = new HikariDataSource(config);
            System.out.println("   ✅ DataSource creado para PostgreSQL (DB_B)");
            System.out.println("   🌐 URL: " + dataSourceBUrl);
            System.out.println("   🔄 AutoCommit configurado: false");
            System.out.println("═══════════════════════════════════════════════════════════");
            return dataSource;
        }
    }

    /**
     * Crea un nuevo EntityManagerFactory para la base de datos especificada
     */
    public EntityManagerFactory createEntityManagerFactory(DataSource dataSource, String database) {
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("🏭 DatabaseConnectionFactory.createEntityManagerFactory:");
        System.out.println("   🎯 Database: " + database);
        System.out.println("   🔄 Creando nueva instancia de EntityManagerFactory...");
        
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.ejemplo.batch.model");

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> properties = new HashMap<>();
        // CRÍTICO: Especificar el dialect según la base de datos
        // En Hibernate 7.x, los dialectos correctos son:
        // - MySQLDialect (no MySQL8Dialect)
        // - PostgreSQLDialect
        if ("DB_A".equals(database)) {
            properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        } else {
            properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        }
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.show_sql", "false");
        properties.put("hibernate.format_sql", "false");
        properties.put("hibernate.jdbc.batch_size", "10");
        properties.put("hibernate.connection.provider_disables_autocommit", "true");
        properties.put("hibernate.connection.autocommit", "false");
        properties.put("hibernate.physical_naming_strategy",
                "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");

        em.setJpaPropertyMap(properties);
        
        try {
            em.afterPropertiesSet();
            EntityManagerFactory entityManagerFactory = em.getObject();
            System.out.println("   ✅ EntityManagerFactory creado para " + database);
            System.out.println("═══════════════════════════════════════════════════════════");
            return entityManagerFactory;
        } catch (Exception e) {
            System.err.println("═══════════════════════════════════════════════════════════");
            System.err.println("❌ ERROR al crear EntityManagerFactory para " + database + ":");
            System.err.println("   💥 Error: " + e.getMessage());
            System.err.println("═══════════════════════════════════════════════════════════");
            e.printStackTrace();
            throw new RuntimeException("Error al crear EntityManagerFactory para " + database + ": " + e.getMessage(), e);
        }
    }

    /**
     * Crea un nuevo TransactionManager para la base de datos especificada
     */
    public PlatformTransactionManager createTransactionManager(EntityManagerFactory entityManagerFactory) {
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("🏭 DatabaseConnectionFactory.createTransactionManager:");
        System.out.println("   🔄 Creando nueva instancia de JpaTransactionManager...");
        
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        
        System.out.println("   ✅ JpaTransactionManager creado");
        System.out.println("═══════════════════════════════════════════════════════════");
        return transactionManager;
    }

    /**
     * Cierra y destruye todos los componentes de base de datos
     */
    public void closeDatabaseComponents(EntityManagerFactory entityManagerFactory, DataSource dataSource) {
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("🏭 DatabaseConnectionFactory.closeDatabaseComponents:");
        System.out.println("   🔄 Cerrando y destruyendo componentes de base de datos...");
        
        try {
            if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
                entityManagerFactory.close();
                System.out.println("   ✅ EntityManagerFactory cerrado");
            }
        } catch (Exception e) {
            System.err.println("   ⚠️ Error al cerrar EntityManagerFactory: " + e.getMessage());
        }
        
        // CRÍTICO: Cerrar el DataSource si es HikariDataSource para liberar conexiones
        if (dataSource instanceof HikariDataSource) {
            try {
                ((HikariDataSource) dataSource).close();
                System.out.println("   ✅ HikariDataSource cerrado correctamente");
            } catch (Exception e) {
                System.err.println("   ⚠️ Error al cerrar HikariDataSource: " + e.getMessage());
            }
        }
        
        System.out.println("   ✅ Componentes cerrados y listos para ser destruidos");
        System.out.println("═══════════════════════════════════════════════════════════");
    }
}

