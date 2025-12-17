package com.ejemplo.batch;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@SpringBootApplication
public class BatchApplication {
    // Obtener el logger para esta clase
    private static final Logger logger = LogManager.getLogger(BatchApplication.class);
	public static void main(String[] args) {
        logger.info("Iniciando el método Demo Batch-Java25."); // Nivel INFO	
		SpringApplication.run(BatchApplication.class, args);
		logger.info("Operación completada exitosamente. Demo Batch-Java25."); // Nivel INFO
		logger.info("Método hacerAlgo finalizado. Demo Batch-Java25.");
	}

}
