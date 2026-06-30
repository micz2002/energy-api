package com.codibly.energyapi.client.carbonintensity;

import com.codibly.energyapi.client.carbonintensity.dto.GenerationApiResponse;
import com.codibly.energyapi.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class CarbonIntensityClient {

    private static final DateTimeFormatter API_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'");

    private final RestClient carbonIntensityRestClient;

    @Cacheable("generationMix")
    public GenerationApiResponse getGenerationMix(OffsetDateTime from, OffsetDateTime to) {
        String fromDateTime = formatForApi(from);
        String toDateTime = formatForApi(to);

        log.info("Fetching generation mix from {} to {}", fromDateTime, toDateTime);

        try {
            GenerationApiResponse response = carbonIntensityRestClient.get()
                    .uri("/generation/{from}/{to}", fromDateTime, toDateTime)
                    .retrieve()
                    .body(GenerationApiResponse.class);

            if (response == null || response.data() == null || response.data().isEmpty()) {
                throw new ExternalApiException("Carbon Intensity API returned no generation data.");
            }

            return response;
        } catch (RestClientException exception) {
            throw new ExternalApiException("Failed to fetch generation data from Carbon Intensity API.", exception);
        }
    }

    private String formatForApi(OffsetDateTime dateTime) {
        return dateTime
                .withOffsetSameInstant(ZoneOffset.UTC)
                .format(API_DATE_TIME_FORMATTER);
    }
}