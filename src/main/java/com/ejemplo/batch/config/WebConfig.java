package com.ejemplo.batch.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de Web MVC para registrar interceptors.
 * Registra el DataSourceInterceptor para que procese todos los requests HTTP.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private DataSourceInterceptor dataSourceInterceptor;

    /**
     * Registra el interceptor de DataSource.
     * El interceptor se ejecutará para todas las solicitudes HTTP.
     *
     * @param registry el registry de interceptors
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(dataSourceInterceptor);
    }
}
