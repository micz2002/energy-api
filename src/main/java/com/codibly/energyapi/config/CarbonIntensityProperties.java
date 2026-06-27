package com.codibly.energyapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;

@ConfigurationProperties(prefix = "carbon-intensity.api")
public record CarbonIntensityProperties(
        URI baseUrl,
        Duration timeout
) {
}