package com.cvshealth.digital.microservice.consents.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class CVSJSONMapper {
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
    
    public static String toJSON(Object object, boolean prettyPrint) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            if (prettyPrint) {
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
            } else {
                return mapper.writeValueAsString(object);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert to JSON", e);
        }
    }
}
