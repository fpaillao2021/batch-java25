package com.ejemplo.batch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DisplayName("Pruebas de integración para BatchApplication")
class BatchApplicationTest {

    @Autowired(required = false)
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Debe cargar el contexto de Spring Boot")
    void testContextLoads() {
        assertNotNull(applicationContext, "El contexto de Spring debe cargarse");
    }

    @Test
    @DisplayName("Debe ser una aplicación Spring Boot válida")
    void testApplicationAnnotation() {
        assertTrue(BatchApplication.class.isAnnotationPresent(
            org.springframework.boot.autoconfigure.SpringBootApplication.class),
            "BatchApplication debe tener @SpringBootApplication");
    }

    @Test
    @DisplayName("Debe tener método main")
    void testMainMethodExists() {
        try {
            BatchApplication.class.getMethod("main", String[].class);
            assertTrue(true, "Debe tener método main");
        } catch (NoSuchMethodException e) {
            fail("Método main no encontrado");
        }
    }

    @Test
    @DisplayName("Debe contener beans necesarios después de cargar contexto")
    void testBeansLoaded() {
        if (applicationContext != null) {
            assertNotNull(applicationContext.getBean("batchController"), 
                         "BatchController bean debe estar cargado");
        }
    }
}
