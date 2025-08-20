package com.cvshealth.digital.microservice.consents.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder(toBuilder=true)
public class QuestionnaireUIRequest {
    
    @Data
    @SuperBuilder(toBuilder=true)
    public static class ScheduleQuestionnaireInput {
        private List<QuestionnaireDataInput> questionnaireDataInput;
        
        public List<QuestionnaireDataInput> getQuestionnaireDataInput() { return questionnaireDataInput; }
    }
    
    @Data
    @SuperBuilder(toBuilder=true)
    public static class QuestionnaireDataInput {
        private String patientId;
        private String dateOfBirth;
        private String relation;
    }
}
