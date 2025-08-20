package com.cvshealth.digital.microservice.consents.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Map;

@Configuration
@PropertySource("classpath:dhs-scheduling-messages.yaml")
@ConfigurationProperties(prefix = "error")
@Data
public class MessageConfig {

    private Map<String,String> messages ;
    
    public Map<String, String> getMessages() { 
        return messages; 
    }

}     