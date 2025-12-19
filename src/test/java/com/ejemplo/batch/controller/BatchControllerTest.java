package com.ejemplo.batch.controller;

import com.ejemplo.batch.model.RegistroCSV;
import com.ejemplo.batch.services.IJobRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias para BatchController")
class BatchControllerTest {

    @Mock
    private IJobRegistry jobRegistry;

    @InjectMocks
    private BatchController controller;

    @BeforeEach
    void setUp() {
        // Inicialización si es necesaria
    }

    @Test
    @DisplayName("Debe ejecutar batch con archivo válido")
    void testRunBatchJobExitoso() {
        // Arrange
        String filename = "registros.csv";
        String database = "DB_A";
        String mensajeExito = "✓ Batch ejecutado exitosamente";

        when(jobRegistry.runBatchJob(filename, "DB_A")).thenReturn(mensajeExito);

        // Act
        String response = controller.runBatchJob(filename, database);

        // Assert
        assertEquals(mensajeExito, response);
        verify(jobRegistry, times(1)).runBatchJob(filename, "DB_A");
    }

    @Test
    @DisplayName("Debe retornar error cuando archivo no existe")
    void testRunBatchJobArchivoNoExiste() {
        // Arrange
        String filename = "inexistente.csv";
        String database = "DB_A";
        String mensajeError = "✗ ERROR: El archivo no existe";

        when(jobRegistry.runBatchJob(filename, "DB_A")).thenReturn(mensajeError);

        // Act
        String response = controller.runBatchJob(filename, database);

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("ERROR"));
        verify(jobRegistry, times(1)).runBatchJob(filename, "DB_A");
    }

    @Test
    @DisplayName("Debe obtener todos los registros")
    void testGetAllRegistros() {
        // Arrange
        String database = "DB_A";
        List<RegistroCSV> registros = new ArrayList<>();
        registros.add(crearRegistro(1L, "Juan", 30, "juan@example.com"));
        registros.add(crearRegistro(2L, "María", 25, "maria@example.com"));

        when(jobRegistry.getAllRegistros()).thenReturn(registros);

        // Act
        List<RegistroCSV> response = controller.getAllRegistros(database);

        // Assert
        assertEquals(2, response.size());
        verify(jobRegistry, times(1)).getAllRegistros();
    }

    @Test
    @DisplayName("Debe retornar lista vacía si no hay registros")
    void testGetAllRegistrosVacio() {
        // Arrange
        String database = "DB_A";
        when(jobRegistry.getAllRegistros()).thenReturn(new ArrayList<>());

        // Act
        List<RegistroCSV> response = controller.getAllRegistros(database);

        // Assert
        assertTrue(response.isEmpty());
        verify(jobRegistry, times(1)).getAllRegistros();
    }

    @Test
    @DisplayName("Debe obtener registro por ID")
    void testGetRegistroById() {
        // Arrange
        Long id = 1L;
        String database = "DB_A";
        RegistroCSV registro = crearRegistro(id, "Juan", 30, "juan@example.com");

        when(jobRegistry.getRegistroById(id)).thenReturn(Optional.of(registro));

        // Act
        Optional<RegistroCSV> response = controller.getRegistroById(id, database);

        // Assert
        assertTrue(response.isPresent());
        assertEquals("Juan", response.get().getNombre());
        verify(jobRegistry, times(1)).getRegistroById(id);
    }

    @Test
    @DisplayName("Debe retornar Optional vacío si registro no existe")
    void testGetRegistroByIdNoExiste() {
        // Arrange
        Long id = 999L;
        String database = "DB_A";
        when(jobRegistry.getRegistroById(id)).thenReturn(Optional.empty());

        // Act
        Optional<RegistroCSV> response = controller.getRegistroById(id, database);

        // Assert
        assertFalse(response.isPresent());
        verify(jobRegistry, times(1)).getRegistroById(id);
    }

    @Test
    @DisplayName("Debe manejar excepciones en batch")
    void testRunBatchJobConExcepcion() {
        // Arrange
        String filename = "error.csv";
        String database = "DB_A";
        when(jobRegistry.runBatchJob(filename, database))
            .thenThrow(new RuntimeException("Error procesando archivo"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            controller.runBatchJob(filename, database);
        });
    }

    @Test
    @DisplayName("Debe validar controller está inyectado correctamente")
    void testControllerInyectado() {
        // Assert
        assertNotNull(controller);
        assertNotNull(jobRegistry);
    }

    // Métodos auxiliares
    private RegistroCSV crearRegistro(Long id, String nombre, Integer edad, String email) {
        RegistroCSV registro = new RegistroCSV();
        registro.setId(id);
        registro.setNombre(nombre);
        registro.setEdad(edad);
        registro.setEmail(email);
        return registro;
    }
}
