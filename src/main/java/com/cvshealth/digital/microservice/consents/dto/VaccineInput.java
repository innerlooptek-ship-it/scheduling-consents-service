package com.cvshealth.digital.microservice.consents.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder=true)
@AllArgsConstructor
@NoArgsConstructor
public class VaccineInput {
    private String vaccineType;
    private String dosage;
    private String code;
    private java.util.List<String> ndc;
    
    public String getCode() {
        return code;
    }
    
    public java.util.List<String> getNdc() {
        return ndc;
    }
}
