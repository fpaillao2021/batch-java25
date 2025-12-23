package com.ejemplo.batch.config;

import com.evertecinc.entitydto.app.utils.MessagesLocales;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(MessagesLocales.MensajeLocal.API_TITULO)
                        .version(MessagesLocales.MensajeLocal.API_VERSION)
                        .description(MessagesLocales.MensajeLocal.API_DESCRIPCION));
    }
}
