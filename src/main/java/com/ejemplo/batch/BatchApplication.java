package com.ejemplo.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Las entidades de negocio se manejan en batch-dl-data-mysql vía API REST.
 * Spring Batch usa JDBC para sus metadatos (no requiere escaneo de entidades).
 * Ya no hay RegistroRepository local - se eliminó para usar API REST.
 */
@SpringBootApplication
public class BatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(BatchApplication.class, args);
	}

}
