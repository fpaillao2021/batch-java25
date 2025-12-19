package com.ejemplo.batch.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

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
     * @return la clave del datasource (ej: "DB_A", "DB_B")
     */
    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceContext.getDataSourceKey();
    }
}
