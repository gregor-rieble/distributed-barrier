package de.gregblog.barrier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(BarrierProperties.class)
public class BarrierApplication {
    static void main(String[] args) {
        SpringApplication.run(BarrierApplication.class, args);
    }

    @Bean
    static BarrierService barrierService(BarrierProperties properties, ConfigurableApplicationContext applicationContext) {
        return new BarrierService(properties, applicationContext);
    }
}
