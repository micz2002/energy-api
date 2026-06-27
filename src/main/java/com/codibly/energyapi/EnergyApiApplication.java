package com.codibly.energyapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@ConfigurationPropertiesScan
@SpringBootApplication
public class EnergyApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnergyApiApplication.class, args);
    }

}
