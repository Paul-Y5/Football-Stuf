package com.football;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class FootballApplication {

    public static void main(String[] args) {
        SpringApplication.run(FootballApplication.class, args);
    }
}
