package com.aims.aims_backend;

import com.aims.config.ShippingProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.aims")
@EntityScan(basePackages = "com.aims.entity")
@EnableJpaRepositories(basePackages = "com.aims.repository")
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(ShippingProperties.class)
public class AimsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AimsBackendApplication.class, args);
    }
}
