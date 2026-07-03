package com.codibly.energyapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HomeController {

    @GetMapping("/")
    public ApiInfoResponse getApiInfo() {
        return new ApiInfoResponse(
                "Energy API",
                "Backend service for Great Britain energy mix and EV charging optimization.",
                List.of(
                        "GET /actuator/health",
                        "GET /api/energy-mix",
                        "GET /api/charging-window?durationHours=3"
                )
        );
    }

    public record ApiInfoResponse(
            String name,
            String description,
            List<String> endpoints
    ) {
    }
}