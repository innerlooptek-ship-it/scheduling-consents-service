package com.cvshealth.digital.microservice.consents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentRequest {
    
    @NotBlank(message = "Flow is required")
    private String flow;
    
    @NotBlank(message = "LOB is required")
    private String lob;
    
    @NotBlank(message = "Modality is required")
    private String modality;
    
    @NotBlank(message = "Brand is required")
    private String brand;
    
    @NotBlank(message = "Clinic ID is required")
    private String clinicId;
    
    @NotBlank(message = "State is required")
    private String state;
    
    @NotBlank(message = "Auth type is required")
    private String authType;
    
    @NotEmpty(message = "Patients list cannot be empty")
    @Valid
    private List<Patient> patients;
    
    @NotEmpty(message = "Contexts list cannot be empty")
    @Valid
    private List<Context> contexts;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Patient {
        @NotBlank(message = "Patient reference ID is required")
        private String patientReferenceId;
        
        @NotBlank(message = "Date of birth is required")
        private String dateOfBirth;
        
        @NotBlank(message = "Patient ID is required")
        private String patientId;
        
        @NotBlank(message = "Relationship to patient is required")
        private String relationshipToPatient;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Context {
        @NotBlank(message = "Context name is required")
        private String name;
        
        @NotNull(message = "Summarize results flag is required")
        private Boolean summarizeResults;
    }
}
