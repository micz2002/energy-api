package com.codibly.energyapi.dto;

import java.math.BigDecimal;

public record EnergySourceShareResponse(
        String source,
        BigDecimal percentage
) {
}