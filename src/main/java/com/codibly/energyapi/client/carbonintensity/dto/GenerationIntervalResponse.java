package com.codibly.energyapi.client.carbonintensity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;

public record GenerationIntervalResponse(
        OffsetDateTime from,
        OffsetDateTime to,

        @JsonProperty("generationmix")
        List<FuelMixResponse> generationMix
) {
}