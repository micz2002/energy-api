package com.codibly.energyapi.controller;

import com.codibly.energyapi.dto.EnergyMixResponse;
import com.codibly.energyapi.service.EnergyMixService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/energy-mix")
@RequiredArgsConstructor
public class EnergyMixController {

    private final EnergyMixService energyMixService;

    @GetMapping
    public EnergyMixResponse getEnergyMix() {
        return energyMixService.getEnergyMix();
    }
}