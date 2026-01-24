package com.football;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Football Java Application
 * 
 * Features:
 * - Club Rankings Dashboard with API updates from football-data.org
 * - UEFA Draw Simulator with backtracking algorithm
 * - REST API with OpenAPI documentation
 * - JPA persistence with H2/PostgreSQL
 * - TDD approach with test pyramid (unit/integration/e2e)
 */
@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableJpaRepositories
public class FootballApplication {

    public static void main(String[] args) {
        SpringApplication.run(FootballApplication.class, args);
    }
}
