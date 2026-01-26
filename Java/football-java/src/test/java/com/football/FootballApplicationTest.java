package com.football;

import com.football.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Main application test - verifies Spring context loads successfully.
 * Uses test profile to disable scheduled tasks and external API calls.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class FootballApplicationTest {

    @Test
    void contextLoads() {
        // Verify Spring context loads successfully
    }
}
