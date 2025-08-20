package com.cvshealth.digital.microservice.consents.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder=true)
public class DomainApiError {
    private String statusCode;
    private String statusDescription;
    private Fault fault;
    
    @Data
    @SuperBuilder(toBuilder=true)
    public static class Fault {
        private String faultCode;
        private String faultString;
        private String detail;
        private String type;
        private String title;
        private String moreInfo;
        private java.util.List<Error> errors;
        
        @Data
        @SuperBuilder(toBuilder=true)
        public static class Error {
            private String code;
            private String message;
            private String type;
            private String title;
            private String field;
        }
    }
}
