package com.codibly.energyapi.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ChargingWindowResponse(
        OffsetDateTime startDateTime,
        OffsetDateTime endDateTime,
        BigDecimal cleanEnergyPercentage
) {
}