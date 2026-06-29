package com.codibly.energyapi.dto;

import java.util.List;

public record EnergyMixResponse(
        List<DailyEnergyMixResponse> days
) {
}