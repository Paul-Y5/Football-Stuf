package com.football.config;

import org.springframework.boot.test.context.TestConfiguration;

/**
 * Test configuration for integration tests.
 * The AutoUpdateService is automatically disabled via
 * application-test.properties
 * by setting football.autoupdate.enabled=false
 */
@TestConfiguration
public class TestConfig {
    // Configuration is handled via application-test.properties
    // AutoUpdateService is disabled when football.autoupdate.enabled=false
}
