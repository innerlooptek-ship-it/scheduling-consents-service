package com.cvshealth.digital.microservice.consents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentResponse {
    
    private ConsentData data;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsentData {
        private GetSchedulingConsents getSchedulingConsents;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetSchedulingConsents {
        private String statusCode;
        private String statusDescription;
        private List<PatientConsent> consentsData;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatientConsent {
        private String patientReferenceId;
        private List<ConsentContext> consents;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsentContext {
        private String text;
        private String consentContext;
        private ConsentDetails consent;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsentDetails {
        private List<ConsentItem> consents;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsentItem {
        private String text;
        private String subText;
        private String consentType;
        private Boolean required;
        private String consentName;
        private String consentLink;
        private String consentLinkText;
        private Boolean isHidden;
        private String value;
        private String valueType;
        private ConsentDetails consent;
    }
}
