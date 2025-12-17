package com.ejemplo.batch.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas unitarias para RegistroCSV")
class RegistroCSVTest {

    private RegistroCSV registro;

    @BeforeEach
    void setUp() {
        registro = new RegistroCSV();
    }

    @Test
    @DisplayName("Debe crear un registro con valores válidos")
    void testCrearRegistroValido() {
        // Arrange
        String nombre = "Juan Pérez";
        Integer edad = 30;
        String email = "juan@example.com";

        // Act
        registro.setNombre(nombre);
        registro.setEdad(edad);
        registro.setEmail(email);

        // Assert
        assertEquals(nombre, registro.getNombre());
        assertEquals(edad, registro.getEdad());
        assertEquals(email, registro.getEmail());
    }

    @Test
    @DisplayName("Debe permitir setear y obtener ID")
    void testSetGetId() {
        // Arrange
        Long id = 1L;

        // Act
        registro.setId(id);

        // Assert
        assertEquals(id, registro.getId());
    }

    @Test
    @DisplayName("Debe permitir setear y obtener fecha de proceso")
    void testSetGetFechaProceso() {
        // Arrange
        LocalDateTime ahora = LocalDateTime.now();

        // Act
        registro.setFechaProceso(ahora);

        // Assert
        assertEquals(ahora, registro.getFechaProceso());
    }

    @Test
    @DisplayName("Debe permitir valores null")
    void testValoresNull() {
        // Act
        registro.setNombre(null);
        registro.setEmail(null);

        // Assert
        assertNull(registro.getNombre());
        assertNull(registro.getEmail());
        // Nota: edad es int (primitivo), no puede ser null
    }

    @Test
    @DisplayName("Debe trabajar con nombre vacío")
    void testNombreVacio() {
        // Act
        registro.setNombre("");

        // Assert
        assertEquals("", registro.getNombre());
    }

    @Test
    @DisplayName("Debe permitir edad 0")
    void testEdadCero() {
        // Act
        registro.setEdad(0);

        // Assert
        assertEquals(0, registro.getEdad());
    }

    @Test
    @DisplayName("Debe permitir edad negativa (para validación posterior)")
    void testEdadNegativa() {
        // Act
        registro.setEdad(-1);

        // Assert
        assertEquals(-1, registro.getEdad());
    }

    @Test
    @DisplayName("Debe permitir edades altas")
    void testEdadAlta() {
        // Act
        registro.setEdad(150);

        // Assert
        assertEquals(150, registro.getEdad());
    }

    @Test
    @DisplayName("Debe trabajar con emails inválidos (validación posterior)")
    void testEmailInvalido() {
        // Act
        registro.setEmail("no_es_email");

        // Assert
        assertEquals("no_es_email", registro.getEmail());
    }

    @Test
    @DisplayName("Debe permitir múltiples asignaciones")
    void testMultiplesAsignaciones() {
        // Act
        registro.setNombre("Juan");
        registro.setEdad(25);
        registro.setEmail("juan@example.com");
        
        // Reasignar
        registro.setNombre("María");
        registro.setEdad(30);
        registro.setEmail("maria@example.com");

        // Assert
        assertEquals("María", registro.getNombre());
        assertEquals(30, registro.getEdad());
        assertEquals("maria@example.com", registro.getEmail());
    }

    @Test
    @DisplayName("Debe tener constructor por defecto")
    void testConstructorPorDefecto() {
        // Assert
        assertNotNull(new RegistroCSV());
    }
}
