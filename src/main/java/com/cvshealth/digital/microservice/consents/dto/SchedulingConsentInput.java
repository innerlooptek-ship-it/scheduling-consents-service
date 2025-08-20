package com.cvshealth.digital.microservice.consents.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder=true)
@AllArgsConstructor
@NoArgsConstructor
public class SchedulingConsentInput {
    private String patientId;
    private String consentType;
    private String consentValue;
    private String appointmentId;
}
