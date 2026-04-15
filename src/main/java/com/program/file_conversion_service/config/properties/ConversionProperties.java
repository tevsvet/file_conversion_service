package com.program.file_conversion_service.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.conversion")
public record ConversionProperties(
        boolean deleteSourceAfterConversion,
        String resultPrefix
) { }
