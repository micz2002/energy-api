package com.codibly.energyapi.domain;

import java.util.Set;

public final class EnergySources {

    public static final Set<String> CLEAN_ENERGY_SOURCES = Set.of(
            "biomass",
            "nuclear",
            "hydro",
            "wind",
            "solar"
    );

    private EnergySources() {
    }

    public static boolean isClean(String fuel) {
        return CLEAN_ENERGY_SOURCES.contains(fuel);
    }
}