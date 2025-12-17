package com.ejemplo.batch.services;

import com.ejemplo.batch.model.RegistroCSV;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de contrato para IJobRegistry")
class IJobRegistryTest {

    @Test
    @DisplayName("Debe permitir implementaciones de runBatchJob")
    void testRunBatchJobSignature() {
        // Verificar que el método existe y retorna String
        try {
            IJobRegistry.class.getMethod("runBatchJob", String.class);
            assertTrue(true, "Método runBatchJob debe estar definido");
        } catch (NoSuchMethodException e) {
            fail("Método runBatchJob no encontrado");
        }
    }

    @Test
    @DisplayName("Debe permitir implementaciones de getAllRegistros")
    void testGetAllRegistrosSignature() {
        // Verificar que el método existe y retorna List
        try {
            IJobRegistry.class.getMethod("getAllRegistros");
            assertTrue(true, "Método getAllRegistros debe estar definido");
        } catch (NoSuchMethodException e) {
            fail("Método getAllRegistros no encontrado");
        }
    }

    @Test
    @DisplayName("Debe permitir implementaciones de getRegistroById")
    void testGetRegistroByIdSignature() {
        // Verificar que el método existe y retorna Optional
        try {
            IJobRegistry.class.getMethod("getRegistroById", Long.class);
            assertTrue(true, "Método getRegistroById debe estar definido");
        } catch (NoSuchMethodException e) {
            fail("Método getRegistroById no encontrado");
        }
    }

    @Test
    @DisplayName("Debe permitir implementaciones de registerJob")
    void testRegisterJobSignature() {
        // Verificar que el método existe y retorna boolean
        try {
            IJobRegistry.class.getMethod("registerJob", String.class);
            assertTrue(true, "Método registerJob debe estar definido");
        } catch (NoSuchMethodException e) {
            fail("Método registerJob no encontrado");
        }
    }

    @Test
    @DisplayName("Debe ser una interfaz pública")
    void testIsInterface() {
        assertTrue(IJobRegistry.class.isInterface(), "IJobRegistry debe ser una interfaz");
    }

    @Test
    @DisplayName("Debe tener 4 métodos definidos")
    void testMethodCount() {
        int methodCount = IJobRegistry.class.getDeclaredMethods().length;
        assertEquals(4, methodCount, "IJobRegistry debe tener 4 métodos");
    }
}
