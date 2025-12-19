package com.ejemplo.batch.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor que captura el header X-Database de cada request HTTP
 * y configura el contexto de DataSource correspondiente.
 * 
 * Flujo:
 * 1. preHandle: Captura header X-Database y lo guarda en DataSourceContext
 * 2. afterCompletion: Limpia el contexto para evitar memory leaks
 */
@Component
public class DataSourceInterceptor implements HandlerInterceptor {

    /**
     * Se ejecuta ANTES de procesar el request.
     * Extrae el header X-Database y lo guarda en el contexto.
     *
     * @param request  HttpServletRequest actual
     * @param response HttpServletResponse
     * @param handler  el handler que va a procesar el request
     * @return true para continuar el procesamiento
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String databaseKey = request.getHeader("X-Database");

        // Validar y establecer el datasource
        if (databaseKey != null && !databaseKey.isEmpty()) {
            if ("DB_A".equals(databaseKey) || "DB_B".equals(databaseKey)) {
                DataSourceContext.setDataSourceKey(databaseKey);
            } else {
                // Si el header es inválido, usar el default
                DataSourceContext.setDataSourceKey("DB_A");
            }
        } else {
            // Si no hay header, usar el default
            DataSourceContext.setDataSourceKey("DB_A");
        }

        return true;
    }

    /**
     * Se ejecuta DESPUÉS de procesar el request, incluso si hay excepciones.
     * Limpia el contexto ThreadLocal para evitar contaminar futuras solicitudes.
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @param handler  el handler
     * @param ex       excepción si la hubo
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        DataSourceContext.clear();
    }
}
