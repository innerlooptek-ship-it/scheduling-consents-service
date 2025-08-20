package com.cvshealth.digital.microservice.consents.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder=true)
@AllArgsConstructor
@NoArgsConstructor
public class ConsentTempConfig {
    private String consentContext;
    private ConsentConfig.Consent consents;
    private String consentType;
    private String consentValue;
}
