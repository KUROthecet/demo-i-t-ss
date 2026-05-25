// Cohesion Level: Temporal Cohesion
// Reason Why: Bootstraps multiple independent components, configurations and contexts concurrently at system startup

package com.aims.aims_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * AIMS — An Internet Media Store
 * Spring Boot 3.2.5 Application Entry Point.
 *
 * Architecture:
 * - entity/          : JPA domain objects (OOP hierarchy with rich domain logic)
 * - repository/      : Spring Data JPA repositories
 * - service/         : Business logic (interface + impl separation)
 * - controller/      : REST API controllers
 * - dto/             : Data Transfer Objects (request/response)
 * - security/        : JWT authentication filter + UserDetailsService
 * - config/          : Spring configuration beans
 * - strategy/        : Strategy pattern (Shipping calculation)
 * - adapter/         : Adapter pattern (Payment gateways)
 * - exception/       : Custom exceptions + Global handler
 */
@SpringBootApplication(scanBasePackages = "com.aims")
@EntityScan(basePackages = "com.aims.entity")
@EnableJpaRepositories(basePackages = "com.aims.repository")
public class AimsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AimsBackendApplication.class, args);
    }
}
