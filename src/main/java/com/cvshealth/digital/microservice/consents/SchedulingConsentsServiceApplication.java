package com.cvshealth.digital.microservice.consents;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SchedulingConsentsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SchedulingConsentsServiceApplication.class, args);
    }
}
