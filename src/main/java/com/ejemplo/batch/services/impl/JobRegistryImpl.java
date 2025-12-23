package com.ejemplo.batch.services.impl;

import com.evertecinc.entitydto.app.batch.model.dto.RegistroCSVDTO;
import com.evertecinc.entitydto.app.batch.model.entity.RegistroCSV;
import com.evertecinc.entitydto.app.utils.EntityDTOMapper;
import com.evertecinc.entitydto.app.utils.MessagesLocales;
import com.ejemplo.batch.services.IJobRegistry;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JobRegistryImpl implements IJobRegistry {

    @Autowired
    private JobOperator jobLauncher;

    @Autowired
    private Job importUserJob;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${file.data.path}")
    private String dataPath;

    @Value("${batch.dl.data.mysql.api.url:http://localhost:8585/api/mysql/dl}")
    private String apiBaseUrl;

    /**
     * Ejecuta el proceso batch con el archivo especificado
     * @param filename Nombre del archivo CSV (ej: registros.csv)
     * @return Mensaje de resultado o error
     */
    @Override
    public String runBatchJob(String filename) {
        try {

            // validar nombre y ruta del archivo
            String validationError = validateFile(filename);
            if (validationError != null) {
                return validationError;
            }

            // crear la ruta completa del archivo
            String filepath = dataPath + "/" + filename;

            // Crear parámetros del job
            JobParameters jobParameters = new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .addString("file.input", filepath)
                .toJobParameters();

            // Ejecutar el job
            jobLauncher.run(importUserJob, jobParameters);
            
            return MessagesLocales.MensajeLocal.BATCH_EJECUTADO_EXITOSAMENTE + filename;
            
        } catch (IllegalArgumentException e) {
            // Error de validación del archivo
            return MessagesLocales.ErrorMensajeLocal.ERROR_VALIDACION_ARCHIVO + e.getMessage();
        } catch (Exception e) {
            // Otros errores durante la ejecución
            e.printStackTrace();
            return MessagesLocales.ErrorMensajeLocal.ERROR_EJECUTAR_JOB + e.getMessage();
        }
    }

    /**
     * Obtiene todos los registros procesados desde la API de batch-dl-data-mysql
     */
    @Override
    public List<RegistroCSV> getAllRegistros() {
        try {
            String url = apiBaseUrl + "/mandato/registros";
            ResponseEntity<List<RegistroCSVDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<RegistroCSVDTO>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Convertir DTOs a entidades
                return response.getBody().stream()
                    .map(EntityDTOMapper::toNewEntity)
                    .collect(Collectors.toList());
            }
            return List.of();
        } catch (Exception e) {
            System.err.println("❌ Error al obtener registros desde API: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Obtiene un registro por su ID desde la API de batch-dl-data-mysql
     */
    @Override
    public Optional<RegistroCSV> getRegistroById(Long id) {
        try {
            String url = apiBaseUrl + "/mandato/registro/" + id;
            ResponseEntity<RegistroCSVDTO> response = restTemplate.getForEntity(url, RegistroCSVDTO.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Convertir DTO a entidad
                RegistroCSV registro = EntityDTOMapper.toNewEntity(response.getBody());
                return Optional.of(registro);
            }
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("❌ Error al obtener registro por ID desde API: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Registra un job en el sistema
     */
    @Override
    public boolean registerJob(String jobName) {
        // Lógica para registrar el job
        System.out.println(MessagesLocales.MensajeLocal.JOB_REGISTRADO + jobName);
        return true;
    }

    /**
     * Valida la existencia y permisos del archivo
    */
    private String validateFile(String fileName) {
      try {
            // Validar que el filename no esté vacío
            if (fileName == null || fileName.trim().isEmpty()) {
                return MessagesLocales.ErrorMensajeLocal.ERROR_FILENAME_VACIO;
            }
            
            String filepath = dataPath + "/" + fileName;
            
            // Validar que el archivo existe ANTES de crear los parámetros
            File file = new File(filepath);
            if (!file.exists()) {
                return MessagesLocales.ErrorMensajeLocal.ERROR_ARCHIVO_NO_EXISTE + fileName + 
                       MessagesLocales.ErrorMensajeLocal.ERROR_ARCHIVO_NO_EXISTE_RUTA + dataPath + 
                       MessagesLocales.ErrorMensajeLocal.ERROR_ARCHIVO_NO_EXISTE_SUFIJO;
            }
            
            if (!file.canRead()) {
                return MessagesLocales.ErrorMensajeLocal.ERROR_PERMISOS_LECTURA + fileName + 
                       MessagesLocales.ErrorMensajeLocal.ERROR_PERMISOS_LECTURA_SUFIJO;
            }
            return null;
      } catch (Exception e) {
            return MessagesLocales.ErrorMensajeLocal.ERROR_INESPERADO_VALIDAR + e.getMessage();
      }
    } 
}
