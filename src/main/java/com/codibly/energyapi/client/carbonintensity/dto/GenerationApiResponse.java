package com.codibly.energyapi.client.carbonintensity.dto;

import java.util.List;

public record GenerationApiResponse(
        List<GenerationIntervalResponse> data
) {
}