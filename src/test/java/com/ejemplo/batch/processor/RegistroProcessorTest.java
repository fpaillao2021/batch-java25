package com.ejemplo.batch.processor;

import com.ejemplo.batch.model.RegistroCSV;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias para RegistroProcessor")
class RegistroProcessorTest {

    private RegistroProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new RegistroProcessor();
    }

    @Test
    @DisplayName("Debe procesar un registro válido correctamente")
    void testProcessarRegistroValido() throws Exception {
        // Arrange
        RegistroCSV item = new RegistroCSV();
        item.setNombre("juan perez");
        item.setEdad(30);
        item.setEmail("juan@example.com");

        // Act
        RegistroCSV resultado = processor.process(item);

        // Assert
        assertNotNull(resultado, "El registro procesado no debe ser null");
        assertEquals("JUAN PEREZ", resultado.getNombre(), "El nombre debe convertirse a mayúsculas");
        assertEquals(30, resultado.getEdad(), "La edad debe permanecer igual");
        assertEquals("juan@example.com", resultado.getEmail(), "El email debe permanecer igual");
        assertNotNull(resultado.getFechaProceso(), "Debe agregarse fecha de proceso");
    }

    @Test
    @DisplayName("Debe procesar múltiples registros correctamente")
    void testProcesarVariosRegistros() throws Exception {
        // Arrange
        RegistroCSV[] registros = {
            crearRegistro("alice smith", 25, "alice@example.com"),
            crearRegistro("bob johnson", 35, "bob@example.com"),
            crearRegistro("carol williams", 28, "carol@example.com")
        };

        // Act & Assert
        for (RegistroCSV registro : registros) {
            RegistroCSV resultado = processor.process(registro);
            assertNotNull(resultado);
            assertEquals(registro.getNombre().toUpperCase(), resultado.getNombre());
            assertNotNull(resultado.getFechaProceso());
        }
    }

    @Test
    @DisplayName("Debe mantener valores null si existen en el registro original")
    void testProcesarRegistroConValoresNull() throws Exception {
        // Arrange
        RegistroCSV item = new RegistroCSV();
        item.setNombre("test");
        item.setEdad(0); // int no puede ser null
        item.setEmail(null);

        // Act
        RegistroCSV resultado = processor.process(item);

        // Assert
        assertNotNull(resultado);
        assertEquals(0, resultado.getEdad());
        assertNull(resultado.getEmail(), "Email null debe mantenerse");
    }

    @Test
    @DisplayName("Debe convertir nombres con caracteres especiales a mayúsculas")
    void testProcesarNombresConCaracteresEspeciales() throws Exception {
        // Arrange
        RegistroCSV item = crearRegistro("josé maría garcía", 40, "jose@example.com");

        // Act
        RegistroCSV resultado = processor.process(item);

        // Assert
        assertNotNull(resultado);
        assertEquals("JOSÉ MARÍA GARCÍA", resultado.getNombre());
    }

    @Test
    @DisplayName("Debe agregar timestamp diferente en cada procesamiento")
    void testFechaProcesoDiferente() throws Exception {
        // Arrange
        RegistroCSV item1 = crearRegistro("usuario1", 20, "user1@example.com");
        RegistroCSV item2 = crearRegistro("usuario2", 25, "user2@example.com");

        // Act
        RegistroCSV resultado1 = processor.process(item1);
        Thread.sleep(10); // Pequeña pausa
        RegistroCSV resultado2 = processor.process(item2);

        // Assert
        assertNotEquals(resultado1.getFechaProceso(), resultado2.getFechaProceso(),
            "Cada registro debe tener fecha de proceso diferente");
    }

    // Métodos auxiliares
    private RegistroCSV crearRegistro(String nombre, int edad, String email) {
        RegistroCSV registro = new RegistroCSV();
        registro.setNombre(nombre);
        registro.setEdad(edad);
        registro.setEmail(email);
        return registro;
    }
}
