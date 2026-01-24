package com.football.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Application startup initializer.
 * Note: Sample data is only loaded in test environments.
 * In production, use the API to load rankings.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Override
    public void run(String... args) {
        log.info("Football Java started. Load rankings from API using the web interface.");
    }
}
