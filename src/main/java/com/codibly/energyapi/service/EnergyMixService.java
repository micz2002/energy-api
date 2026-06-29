package com.codibly.energyapi.service;

import com.codibly.energyapi.client.carbonintensity.CarbonIntensityClient;
import com.codibly.energyapi.client.carbonintensity.dto.FuelMixResponse;
import com.codibly.energyapi.client.carbonintensity.dto.GenerationIntervalResponse;
import com.codibly.energyapi.domain.EnergySources;
import com.codibly.energyapi.dto.DailyEnergyMixResponse;
import com.codibly.energyapi.dto.EnergyMixResponse;
import com.codibly.energyapi.dto.EnergySourceShareResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnergyMixService {

    private static final ZoneId UK_ZONE = ZoneId.of("Europe/London");
    private static final int FORECAST_DAYS = 3;
    private static final int PERCENTAGE_SCALE = 2;

    private final CarbonIntensityClient carbonIntensityClient;
    private final Clock clock;

    public EnergyMixResponse getEnergyMix() {
        LocalDate today = LocalDate.now(clock.withZone(UK_ZONE));
        LocalDate endDate = today.plusDays(FORECAST_DAYS);

        OffsetDateTime from = today.atStartOfDay(UK_ZONE).toOffsetDateTime();
        OffsetDateTime to = endDate.atStartOfDay(UK_ZONE).toOffsetDateTime();

        List<GenerationIntervalResponse> intervals = carbonIntensityClient
                .getGenerationMix(from, to)
                .data();

        Map<LocalDate, List<GenerationIntervalResponse>> intervalsByDate = intervals.stream()
                .filter(interval -> isInExpectedRange(interval, today, endDate))
                .collect(Collectors.groupingBy(this::toUkDate));

        List<DailyEnergyMixResponse> days = today.datesUntil(endDate)
                .map(date -> toDailyEnergyMix(date, intervalsByDate.getOrDefault(date, List.of())))
                .toList();

        return new EnergyMixResponse(days);
    }

    private DailyEnergyMixResponse toDailyEnergyMix(LocalDate date, List<GenerationIntervalResponse> intervals) {
        List<EnergySourceShareResponse> sources = intervals.stream()
                .flatMap(interval -> interval.generationMix().stream())
                .collect(Collectors.groupingBy(
                        FuelMixResponse::fuel,
                        Collectors.mapping(FuelMixResponse::perc, Collectors.toList())
                ))
                .entrySet()
                .stream()
                .map(entry -> new EnergySourceShareResponse(entry.getKey(), average(entry.getValue())))
                .sorted(Comparator.comparing(EnergySourceShareResponse::source))
                .toList();

        BigDecimal cleanEnergyPercentage = sources.stream()
                .filter(source -> EnergySources.isClean(source.source()))
                .map(EnergySourceShareResponse::percentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP);

        return new DailyEnergyMixResponse(date, cleanEnergyPercentage, sources);
    }

    private BigDecimal average(List<BigDecimal> values) {
        if (values.isEmpty()) {
            return BigDecimal.ZERO.setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP);
        }

        BigDecimal sum = values.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(BigDecimal.valueOf(values.size()), PERCENTAGE_SCALE, RoundingMode.HALF_UP);
    }

    private boolean isInExpectedRange(GenerationIntervalResponse interval, LocalDate startDate, LocalDate endDate) {
        LocalDate intervalDate = toUkDate(interval);
        return !intervalDate.isBefore(startDate) && intervalDate.isBefore(endDate);
    }

    private LocalDate toUkDate(GenerationIntervalResponse interval) {
        return interval.from()
                .atZoneSameInstant(UK_ZONE)
                .toLocalDate();
    }
}