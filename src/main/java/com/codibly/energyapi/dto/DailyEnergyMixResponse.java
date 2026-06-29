package com.codibly.energyapi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DailyEnergyMixResponse(
        LocalDate date,
        BigDecimal cleanEnergyPercentage,
        List<EnergySourceShareResponse> sources
) {
}