package com.cvshealth.digital.microservice.consents.utils;

import org.springframework.stereotype.Component;

@Component
public class SchedulingUtils {
    
    public static boolean isValidDate(String date) {
        return date != null && !date.trim().isEmpty();
    }
    
    public static boolean isValidString(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
