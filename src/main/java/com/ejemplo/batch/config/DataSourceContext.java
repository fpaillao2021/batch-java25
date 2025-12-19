package com.ejemplo.batch.config;

/**
 * ThreadLocal holder para el contexto de selección de DataSource.
 * Permite cambiar dinámicamente entre diferentes bases de datos.
 */
public class DataSourceContext {
    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();

    /**
     * Establece la clave del datasource actual.
     *
     * @param key la clave del datasource (ej: "DB_A", "DB_B")
     */
    public static void setDataSourceKey(String key) {
        contextHolder.set(key);
    }

    /**
     * Obtiene la clave del datasource actual.
     *
     * @return la clave del datasource, o "DB_A" por defecto
     */
    public static String getDataSourceKey() {
        return contextHolder.get() != null ? contextHolder.get() : "DB_A";
    }

    /**
     * Limpia el contexto del ThreadLocal.
     * Debe llamarse después de completar la operación.
     */
    public static void clear() {
        contextHolder.remove();
    }
}
