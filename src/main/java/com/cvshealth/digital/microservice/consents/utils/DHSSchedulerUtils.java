package com.cvshealth.digital.microservice.consents.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class DHSSchedulerUtils {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    static {
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }
    
    public static <T> T fromJSON(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON", e);
        }
    }
    
    public static String toJSON(Object object, boolean prettyPrint) {
        try {
            if (prettyPrint) {
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
            } else {
                return objectMapper.writeValueAsString(object);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert to JSON", e);
        }
    }
}
