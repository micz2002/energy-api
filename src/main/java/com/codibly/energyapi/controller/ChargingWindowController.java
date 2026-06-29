package com.codibly.energyapi.controller;

import com.codibly.energyapi.dto.ChargingWindowResponse;
import com.codibly.energyapi.service.ChargingWindowService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/charging-window")
@RequiredArgsConstructor
public class ChargingWindowController {

    private final ChargingWindowService chargingWindowService;

    @GetMapping
    public ChargingWindowResponse findBestChargingWindow(
            @RequestParam
            @Min(1)
            @Max(6)
            int durationHours
    ) {
        return chargingWindowService.findBestChargingWindow(durationHours);
    }
}