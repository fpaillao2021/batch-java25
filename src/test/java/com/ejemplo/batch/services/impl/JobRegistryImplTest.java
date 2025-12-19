package com.ejemplo.batch.services.impl;

import com.ejemplo.batch.model.RegistroCSV;
import com.ejemplo.batch.repository.RegistroRepository;
import com.ejemplo.batch.services.IJobRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias para JobRegistryImpl")
class JobRegistryImplTest {

    @Mock
    private RegistroRepository registroRepository;

    @InjectMocks
    private JobRegistryImpl jobRegistry;

    private static final String TEST_DATA_PATH = "src/main/resources/data";

    @BeforeEach
    void setUp() {
        // Inyectar manualmente el dataPath
        ReflectionTestUtils.setField(jobRegistry, "dataPath", TEST_DATA_PATH);
    }

    @Test
    @DisplayName("Debe rechazar filename vacío")
    void testRunBatchJobConFilenameVacio() {
        // Act
        String resultado = jobRegistry.runBatchJob("", "DB_A");

        // Assert
        assertTrue(resultado.contains("✗ ERROR"), "Debe retornar error para filename vacío");
        assertTrue(resultado.contains("no puede estar vacío"), "Mensaje de error específico");
    }

    @Test
    @DisplayName("Debe rechazar filename null")
    void testRunBatchJobConFilenameNull() {
        // Act
        String resultado = jobRegistry.runBatchJob(null, "DB_A");

        // Assert
        assertTrue(resultado.contains("✗ ERROR"), "Debe retornar error para filename null");
    }

    @Test
    @DisplayName("Debe rechazar archivo que no existe")
    void testRunBatchJobArchivoNoExiste() {
        // Act
        String resultado = jobRegistry.runBatchJob("archivo_inexistente.csv", "DB_A");

        // Assert
        assertTrue(resultado.contains("✗ ERROR"), "Debe retornar error para archivo inexistente");
        assertTrue(resultado.contains("no existe"), "Debe indicar que no existe");
    }

    @Test
    @DisplayName("Debe retornar todos los registros")
    void testGetAllRegistros() {
        // Arrange
        List<RegistroCSV> registros = new ArrayList<>();
        registros.add(crearRegistro(1L, "Juan", 30, "juan@example.com"));
        registros.add(crearRegistro(2L, "María", 25, "maria@example.com"));

        when(registroRepository.findAll()).thenReturn(registros);

        // Act
        List<RegistroCSV> resultado = jobRegistry.getAllRegistros();

        // Assert
        assertEquals(2, resultado.size(), "Debe retornar 2 registros");
        assertEquals("Juan", resultado.get(0).getNombre());
        verify(registroRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe retornar registro por ID")
    void testGetRegistroById() {
        // Arrange
        Long id = 1L;
        RegistroCSV registro = crearRegistro(id, "Juan", 30, "juan@example.com");

        when(registroRepository.findById(id)).thenReturn(Optional.of(registro));

        // Act
        Optional<RegistroCSV> resultado = jobRegistry.getRegistroById(id);

        // Assert
        assertTrue(resultado.isPresent(), "Debe encontrar el registro");
        assertEquals("Juan", resultado.get().getNombre());
        verify(registroRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Debe retornar Optional vacío si registro no existe")
    void testGetRegistroByIdNoExiste() {
        // Arrange
        Long id = 999L;
        when(registroRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        Optional<RegistroCSV> resultado = jobRegistry.getRegistroById(id);

        // Assert
        assertFalse(resultado.isPresent(), "No debe encontrar el registro");
        verify(registroRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Debe retornar lista vacía si no hay registros")
    void testGetAllRegistrosVacio() {
        // Arrange
        when(registroRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<RegistroCSV> resultado = jobRegistry.getAllRegistros();

        // Assert
        assertTrue(resultado.isEmpty(), "Debe retornar lista vacía");
        verify(registroRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe validar que archivo sea legible")
    void testValidacionPermisoLectura() {
        // Arrange
        String filename = "registros.csv";
        String dataPath = TEST_DATA_PATH;
        String filepath = dataPath + "/" + filename;
        File file = new File(filepath);

        // Act & Assert
        if (file.exists()) {
            assertTrue(file.canRead(), "El archivo debe ser legible");
        }
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
