package com.codibly.energyapi.client.carbonintensity.dto;

import java.math.BigDecimal;

public record FuelMixResponse(
        String fuel,
        BigDecimal perc
) {
}