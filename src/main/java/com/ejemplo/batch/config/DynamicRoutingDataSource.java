package com.ejemplo.batch.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * DataSource dinámico que redirige las conexiones según el contexto.
 * Implementa AbstractRoutingDataSource para enrutar automáticamente
 * entre múltiples datasources basándose en el DataSourceContext.
 */
public class DynamicRoutingDataSource extends AbstractRoutingDataSource {

    /**
     * Determina qué datasource usar basándose en el contexto actual.
     * Este método es llamado automáticamente por Spring para cada operación de BD.
     * 
     * IMPORTANTE: Este método se llama CADA VEZ que se necesita una conexión,
     * por lo que el contexto debe estar establecido ANTES de que se llame.
     *
     * @return la clave del datasource (ej: "DB_A", "DB_B")
     */
    @Override
    protected Object determineCurrentLookupKey() {
        String key = DataSourceContext.getDataSourceKey();
        System.out.println("🔍 DynamicRoutingDataSource.determineCurrentLookupKey() llamado - retornando: " + key);
        System.out.println("🔍 Thread: " + Thread.currentThread().getName());
        return key;
    }

    /**
     * Obtiene la conexión del DataSource seleccionado.
     * Aquí podemos agregar logging para ver qué URL se está usando.
     */
    @Override
    public Connection getConnection() throws SQLException {
        String key = DataSourceContext.getDataSourceKey();
        Connection connection = super.getConnection();
        
        // Obtener información de la conexión
        try {
            String url = connection.getMetaData().getURL();
            String username = connection.getMetaData().getUserName();
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            boolean autoCommit = connection.getAutoCommit();
            
            System.out.println("═══════════════════════════════════════════════════════════");
            System.out.println("🔗 CONEXIÓN A BASE DE DATOS ESTABLECIDA:");
            System.out.println("   📌 DataSource Key: " + key);
            System.out.println("   🌐 URL: " + url);
            System.out.println("   👤 Usuario: " + username);
            System.out.println("   🗄️  Base de Datos: " + databaseProductName);
            System.out.println("   🔄 AutoCommit: " + autoCommit + " (debe ser false para transacciones)");
            System.out.println("   🧵 Thread: " + Thread.currentThread().getName());
            System.out.println("═══════════════════════════════════════════════════════════");
            
            // IMPORTANTE: Deshabilitar autoCommit para todas las bases de datos
            // Tanto MySQL como PostgreSQL requieren que autoCommit esté en false para transacciones JPA
            if (autoCommit) {
                System.out.println("⚠️ WARNING: AutoCommit está habilitado para " + databaseProductName + ", deshabilitándolo...");
                connection.setAutoCommit(false);
                System.out.println("✅ AutoCommit deshabilitado correctamente");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al obtener información de la conexión: " + e.getMessage());
            e.printStackTrace();
        }
        
        return connection;
    }
}
