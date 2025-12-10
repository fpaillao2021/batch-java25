-- ============================================================
-- SCRIPT DE CREACIÓN DE BASE DE DATOS
-- ============================================================
-- Este script crea la base de datos y las tablas necesarias
-- para el proyecto Spring Batch Boot.
--
-- Ejecutar como usuario root o con privilegios de administrador.
-- ============================================================

-- 1. Crear la base de datos si no existe
CREATE DATABASE IF NOT EXISTS spring_batch_db 
    DEFAULT CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

-- 2. Usar la base de datos
USE spring_batch_db;

-- 3. Crear tabla de clientes
-- Esta es la tabla de entrada para el procesamiento batch
CREATE TABLE IF NOT EXISTS registrocsv (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Identificador único del cliente',
    nombre VARCHAR(100) NOT NULL COMMENT 'Nombre completo del cliente',
    edad INTEGER NOT NULL COMMENT 'Edad numerica del cliente',
    email VARCHAR(150) NOT NULL UNIQUE COMMENT 'Correo electrónico único',
    fechaproceso DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha de creación del registro',
    fecha_proceso DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha de creación del registro'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Tabla de registros procesados desde CSV';

-- 6. Mostrar resumen
SELECT 'Tablas creadas exitosamente' AS Mensaje;
SELECT COUNT(*) AS 'Clientes insertados' FROM registrocsv;