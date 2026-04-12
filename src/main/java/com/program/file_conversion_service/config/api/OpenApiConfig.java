package com.program.file_conversion_service.config.api;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fileConversionOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("File Conversion Service API")
                        .description("Asynchronous service for uploading files, submitting conversion tasks," +
                                " and tracking PDF conversion results.")
                        .version("1.0.0")
                        .contact(new Contact().name("File Conversion Service"))
                        )
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local environment")
                ));
    }
}
