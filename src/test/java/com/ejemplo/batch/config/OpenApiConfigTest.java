package com.ejemplo.batch.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas unitarias para OpenApiConfig")
class OpenApiConfigTest {

    private final OpenApiConfig config = new OpenApiConfig();

    @Test
    @DisplayName("Debe crear OpenAPI bean correctamente")
    void testCustomOpenAPI() {
        // Act
        OpenAPI openAPI = config.customOpenAPI();

        // Assert
        assertNotNull(openAPI, "OpenAPI debe crearse correctamente");
        assertNotNull(openAPI.getInfo(), "Info debe estar presente");
    }

    @Test
    @DisplayName("Debe configurar título correcto en OpenAPI")
    void testOpenAPITitle() {
        // Act
        OpenAPI openAPI = config.customOpenAPI();

        // Assert
        assertEquals("Batch API", openAPI.getInfo().getTitle());
    }

    @Test
    @DisplayName("Debe configurar versión correcta en OpenAPI")
    void testOpenAPIVersion() {
        // Act
        OpenAPI openAPI = config.customOpenAPI();

        // Assert
        assertEquals("v0.0.1", openAPI.getInfo().getVersion());
    }

    @Test
    @DisplayName("Debe configurar descripción correcta en OpenAPI")
    void testOpenAPIDescription() {
        // Act
        OpenAPI openAPI = config.customOpenAPI();

        // Assert
        assertEquals("Documentación OpenAPI para el proyecto batch", 
                    openAPI.getInfo().getDescription());
    }

    @Test
    @DisplayName("Debe crear instancia de OpenApiConfig")
    void testOpenApiConfigInstancia() {
        // Assert
        assertNotNull(new OpenApiConfig());
    }
}
