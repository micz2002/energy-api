package com.codibly.energyapi.client.carbonintensity;

import com.codibly.energyapi.client.carbonintensity.dto.GenerationApiResponse;
import com.codibly.energyapi.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class CarbonIntensityClient {

    private static final DateTimeFormatter API_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'");

    private final RestClient carbonIntensityRestClient;

    public GenerationApiResponse getGenerationMix(OffsetDateTime from, OffsetDateTime to) {
        try {
            GenerationApiResponse response = carbonIntensityRestClient.get()
                    .uri("/generation/{from}/{to}", formatForApi(from), formatForApi(to))
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