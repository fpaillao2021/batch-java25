package com.ejemplo.batch.repository;

import com.ejemplo.batch.model.RegistroCSV;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroRepository extends JpaRepository<RegistroCSV, Long> {
}