package com.codibly.energyapi.service;

import com.codibly.energyapi.client.carbonintensity.CarbonIntensityClient;
import com.codibly.energyapi.client.carbonintensity.dto.FuelMixResponse;
import com.codibly.energyapi.client.carbonintensity.dto.GenerationApiResponse;
import com.codibly.energyapi.client.carbonintensity.dto.GenerationIntervalResponse;
import com.codibly.energyapi.dto.ChargingWindowResponse;
import com.codibly.energyapi.exception.InsufficientGenerationDataException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChargingWindowServiceTest {

    private CarbonIntensityClient carbonIntensityClient;
    private ChargingWindowService chargingWindowService;

    @BeforeEach
    void setUp() {
        carbonIntensityClient = mock(CarbonIntensityClient.class);

        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-01-01T00:00:00Z"),
                ZoneOffset.UTC
        );

        chargingWindowService = new ChargingWindowService(carbonIntensityClient, fixedClock);
    }

    @Test
    void shouldFindBestChargingWindowUsingSlidingWindow() {
        GenerationApiResponse apiResponse = new GenerationApiResponse(List.of(
                interval("2026-01-01T00:00:00Z", "2026-01-01T00:30:00Z", "10"),
                interval("2026-01-01T00:30:00Z", "2026-01-01T01:00:00Z", "20"),
                interval("2026-01-01T01:00:00Z", "2026-01-01T01:30:00Z", "80"),
                interval("2026-01-01T01:30:00Z", "2026-01-01T02:00:00Z", "70")
        ));

        when(carbonIntensityClient.getGenerationMix(any(), any()))
                .thenReturn(apiResponse);

        ChargingWindowResponse response = chargingWindowService.findBestChargingWindow(1);

        assertThat(response.startDateTime()).isEqualTo(OffsetDateTime.parse("2026-01-01T01:00:00Z"));
        assertThat(response.endDateTime()).isEqualTo(OffsetDateTime.parse("2026-01-01T02:00:00Z"));
        assertThat(response.cleanEnergyPercentage()).isEqualByComparingTo("75.00");
    }

    @Test
    void shouldThrowExceptionWhenThereIsNotEnoughData() {
        GenerationApiResponse apiResponse = new GenerationApiResponse(List.of(
                interval("2026-01-01T00:00:00Z", "2026-01-01T00:30:00Z", "50")
        ));

        when(carbonIntensityClient.getGenerationMix(any(), any()))
                .thenReturn(apiResponse);

        assertThatThrownBy(() -> chargingWindowService.findBestChargingWindow(1))
                .isInstanceOf(InsufficientGenerationDataException.class)
                .hasMessage("Not enough generation data to calculate charging window.");
    }

    private GenerationIntervalResponse interval(String from, String to, String cleanEnergyPercentage) {
        BigDecimal clean = new BigDecimal(cleanEnergyPercentage);
        BigDecimal gas = BigDecimal.valueOf(100).subtract(clean);

        return new GenerationIntervalResponse(
                OffsetDateTime.parse(from),
                OffsetDateTime.parse(to),
                List.of(
                        new FuelMixResponse("wind", clean),
                        new FuelMixResponse("gas", gas)
                )
        );
    }
}