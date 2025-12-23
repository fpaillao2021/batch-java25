package com.ejemplo.batch.processor;

import com.evertecinc.entitydto.app.batch.model.dto.RegistroCSVDTO;
import com.evertecinc.entitydto.app.batch.model.entity.RegistroCSV;
import com.evertecinc.entitydto.app.utils.EntityDTOMapper;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Writer personalizado que guarda los registros usando la API REST de batch-dl-data-mysql
 * en lugar de guardar directamente en la base de datos.
 * 
 * Implementa ItemWriter de Spring Batch 6.0.0 que usa Chunk internamente.
 * 
 * NOTA: Esta clase NO debe ser @Component porque se crea como @Bean en BatchConfig
 */
public class RestItemWriter implements ItemWriter<RegistroCSV> {

    private final RestTemplate restTemplate;
    private final String apiUrl;

    public RestItemWriter(RestTemplate restTemplate, String apiBaseUrl) {
        this.restTemplate = restTemplate;
        // Construir la URL completa del endpoint de guardado
        this.apiUrl = apiBaseUrl + "/save/mandato";
    }

    @Override
    public void write(Chunk<? extends RegistroCSV> chunk) throws Exception {
        List<? extends RegistroCSV> items = chunk.getItems();
        if (items == null || items.isEmpty()) {
            return;
        }

        try {
            // Convertir entidades RegistroCSV a DTOs RegistroCSVDTO
            List<RegistroCSVDTO> dtos = items.stream()
                    .map(EntityDTOMapper::toDTO)
                    .collect(Collectors.toList());

            // Configurar headers HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Crear la petición HTTP
            HttpEntity<List<RegistroCSVDTO>> request = new HttpEntity<>(dtos, headers);

            // Hacer la llamada POST a la API de batch-dl-data-mysql
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ Guardados " + items.size() + " registro(s) vía API: " + response.getBody());
            } else {
                throw new RuntimeException("Error al guardar registros vía API. Status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.err.println("❌ Error al guardar registros vía API REST: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-lanzar para que Spring Batch maneje el error
        }
    }
}
