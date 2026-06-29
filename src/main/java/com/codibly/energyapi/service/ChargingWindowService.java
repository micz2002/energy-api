package com.codibly.energyapi.service;

import com.codibly.energyapi.client.carbonintensity.CarbonIntensityClient;
import com.codibly.energyapi.client.carbonintensity.dto.FuelMixResponse;
import com.codibly.energyapi.client.carbonintensity.dto.GenerationIntervalResponse;
import com.codibly.energyapi.domain.EnergySources;
import com.codibly.energyapi.dto.ChargingWindowResponse;
import com.codibly.energyapi.exception.InsufficientGenerationDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChargingWindowService {

    private static final ZoneId UK_ZONE = ZoneId.of("Europe/London");
    private static final int INTERVALS_PER_HOUR = 2;
    private static final int FORECAST_HOURS = 48;
    private static final int PERCENTAGE_SCALE = 2;

    private final CarbonIntensityClient carbonIntensityClient;
    private final Clock clock;

    public ChargingWindowResponse findBestChargingWindow(int durationHours) {
        int requiredIntervals = durationHours * INTERVALS_PER_HOUR;

        ZonedDateTime windowSearchStart = roundUpToNextHalfHour(ZonedDateTime.now(clock.withZone(UK_ZONE)));
        ZonedDateTime windowSearchEnd = windowSearchStart.plusHours(FORECAST_HOURS);

        OffsetDateTime from = windowSearchStart.toOffsetDateTime();
        OffsetDateTime to = windowSearchEnd.toOffsetDateTime();

        List<GenerationIntervalResponse> intervals = carbonIntensityClient
                .getGenerationMix(from, to)
                .data()
                .stream()
                .filter(interval -> isInsideSearchRange(interval, from, to))
                .sorted(Comparator.comparing(GenerationIntervalResponse::from))
                .toList();

        if (intervals.size() < requiredIntervals) {
            throw new InsufficientGenerationDataException("Not enough generation data to calculate charging window.");
        }

        List<BigDecimal> cleanEnergyPercentages = intervals.stream()
                .map(this::calculateCleanEnergyPercentage)
                .toList();

        int bestStartIndex = findBestWindowStartIndex(cleanEnergyPercentages, requiredIntervals);
        BigDecimal bestAverage = calculateWindowAverage(cleanEnergyPercentages, bestStartIndex, requiredIntervals);

        GenerationIntervalResponse firstInterval = intervals.get(bestStartIndex);
        GenerationIntervalResponse lastInterval = intervals.get(bestStartIndex + requiredIntervals - 1);

        return new ChargingWindowResponse(
                toUkOffsetDateTime(firstInterval.from()),
                toUkOffsetDateTime(lastInterval.to()),
                bestAverage
        );
    }

    private int findBestWindowStartIndex(List<BigDecimal> cleanEnergyPercentages, int requiredIntervals) {
        BigDecimal currentSum = cleanEnergyPercentages.subList(0, requiredIntervals)
                .stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal bestSum = currentSum;
        int bestStartIndex = 0;

        for (int startIndex = 1; startIndex <= cleanEnergyPercentages.size() - requiredIntervals; startIndex++) {
            currentSum = currentSum
                    .subtract(cleanEnergyPercentages.get(startIndex - 1))
                    .add(cleanEnergyPercentages.get(startIndex + requiredIntervals - 1));

            if (currentSum.compareTo(bestSum) > 0) {
                bestSum = currentSum;
                bestStartIndex = startIndex;
            }
        }

        return bestStartIndex;
    }

    private BigDecimal calculateWindowAverage(
            List<BigDecimal> cleanEnergyPercentages,
            int startIndex,
            int requiredIntervals
    ) {
        BigDecimal sum = cleanEnergyPercentages.subList(startIndex, startIndex + requiredIntervals)
                .stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(BigDecimal.valueOf(requiredIntervals), PERCENTAGE_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateCleanEnergyPercentage(GenerationIntervalResponse interval) {
        return interval.generationMix()
                .stream()
                .filter(source -> EnergySources.isClean(source.fuel()))
                .map(FuelMixResponse::perc)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean isInsideSearchRange(
            GenerationIntervalResponse interval,
            OffsetDateTime from,
            OffsetDateTime to
    ) {
        return !interval.from().isBefore(from) && !interval.to().isAfter(to);
    }

    private OffsetDateTime toUkOffsetDateTime(OffsetDateTime dateTime) {
        return dateTime
                .atZoneSameInstant(UK_ZONE)
                .toOffsetDateTime();
    }

    private ZonedDateTime roundUpToNextHalfHour(ZonedDateTime dateTime) {
        ZonedDateTime rounded = dateTime.withSecond(0).withNano(0);

        if (dateTime.getSecond() > 0 || dateTime.getNano() > 0) {
            rounded = rounded.plusMinutes(1);
        }

        int minute = rounded.getMinute();

        if (minute == 0 || minute == 30) {
            return rounded;
        }

        if (minute < 30) {
            return rounded.withMinute(30);
        }

        return rounded.plusHours(1).withMinute(0);
    }
}