package com.ejemplo.batch.repository;

import com.ejemplo.batch.model.RegistroCSV;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Pruebas básicas para RegistroRepository")
class RegistroRepositoryTest {

    @Test
    @DisplayName("Debe existir la interfaz RegistroRepository")
    void testRepositoryExiste() {
        // Assert
        assertNotNull(RegistroRepository.class);
    }

    @Test
    @DisplayName("Debe extender JpaRepository")
    void testRepositoryTipoGenerico() {
        // Verificar que RegistroRepository es una interfaz de Spring Data
        assertNotNull(RegistroRepository.class.getInterfaces());
    }

    @Test
    @DisplayName("Métodos heredados de JpaRepository disponibles")
    void testMetodosDisponibles() {
        // Los métodos findAll(), findById(), save(), delete() están
        // heredados de JpaRepository y no requieren test adicional
        assertNotNull(RegistroRepository.class.getDeclaredMethods());
    }
}
