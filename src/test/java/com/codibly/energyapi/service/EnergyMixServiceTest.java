package com.codibly.energyapi.service;

import com.codibly.energyapi.client.carbonintensity.CarbonIntensityClient;
import com.codibly.energyapi.client.carbonintensity.dto.FuelMixResponse;
import com.codibly.energyapi.client.carbonintensity.dto.GenerationApiResponse;
import com.codibly.energyapi.client.carbonintensity.dto.GenerationIntervalResponse;
import com.codibly.energyapi.dto.DailyEnergyMixResponse;
import com.codibly.energyapi.dto.EnergyMixResponse;
import com.codibly.energyapi.dto.EnergySourceShareResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EnergyMixServiceTest {

    private CarbonIntensityClient carbonIntensityClient;
    private EnergyMixService energyMixService;

    @BeforeEach
    void setUp() {
        carbonIntensityClient = mock(CarbonIntensityClient.class);

        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-07-01T10:00:00Z"),
                ZoneOffset.UTC
        );

        energyMixService = new EnergyMixService(carbonIntensityClient, fixedClock);
    }

    @Test
    void shouldReturnDailyEnergyMixForThreeDays() {
        GenerationApiResponse apiResponse = new GenerationApiResponse(List.of(
                interval("2026-07-01T00:00:00+01:00", "2026-07-01T00:30:00+01:00",
                        fuel("wind", "40"), fuel("solar", "10"), fuel("gas", "50")),
                interval("2026-07-01T00:30:00+01:00", "2026-07-01T01:00:00+01:00",
                        fuel("wind", "60"), fuel("solar", "20"), fuel("gas", "20")),

                interval("2026-07-02T00:00:00+01:00", "2026-07-02T00:30:00+01:00",
                        fuel("nuclear", "30"), fuel("hydro", "5"), fuel("gas", "65")),

                interval("2026-07-03T00:00:00+01:00", "2026-07-03T00:30:00+01:00",
                        fuel("biomass", "10"), fuel("wind", "20"), fuel("coal", "70"))
        ));

        when(carbonIntensityClient.getGenerationMix(any(), any()))
                .thenReturn(apiResponse);

        EnergyMixResponse response = energyMixService.getEnergyMix();

        assertThat(response.days()).hasSize(3);

        DailyEnergyMixResponse firstDay = response.days().getFirst();

        assertThat(firstDay.date().toString()).isEqualTo("2026-07-01");
        assertThat(firstDay.cleanEnergyPercentage()).isEqualByComparingTo("65.00");
        assertThat(findPercentage(firstDay, "wind")).isEqualByComparingTo("50.00");
        assertThat(findPercentage(firstDay, "solar")).isEqualByComparingTo("15.00");
        assertThat(findPercentage(firstDay, "gas")).isEqualByComparingTo("35.00");
    }

    private BigDecimal findPercentage(DailyEnergyMixResponse day, String source) {
        return day.sources()
                .stream()
                .filter(energySource -> energySource.source().equals(source))
                .map(EnergySourceShareResponse::percentage)
                .findFirst()
                .orElseThrow();
    }

    private GenerationIntervalResponse interval(
            String from,
            String to,
            FuelMixResponse... fuelMix
    ) {
        return new GenerationIntervalResponse(
                OffsetDateTime.parse(from),
                OffsetDateTime.parse(to),
                List.of(fuelMix)
        );
    }

    private FuelMixResponse fuel(String fuel, String percentage) {
        return new FuelMixResponse(fuel, new BigDecimal(percentage));
    }
}