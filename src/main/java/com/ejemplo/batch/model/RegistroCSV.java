package com.ejemplo.batch.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data; // Usando Lombok para getters/setters

import java.time.LocalDateTime;

@Entity
@Data
public class RegistroCSV {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private int edad;
    private String email;
    private LocalDateTime fechaProceso; // Campo para registrar la fecha de procesamiento

    // Constructor vacío, getters y setters (generados por Lombok @Data)
}