package com.cvshealth.digital.microservice.consents.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleQuestionnaireInput {
    private String storeId;
    private String patientId;
    private List<QuestionnaireDataInput> questionnaireDataInput;
    
    public String getStoreId() {
        return storeId;
    }
    
    public List<QuestionnaireDataInput> getQuestionnaireDataInput() {
        return questionnaireDataInput;
    }
}
