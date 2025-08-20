package com.cvshealth.digital.microservice.consents.dto;

import com.cvshealth.digital.microservice.consents.enums.QuestionnaireContextEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionnaireDataInput {
    private List<QuestionnaireContextEnum> requiredQuestionnaireContext;
    private List<VaccineInput> vaccines;
    private List<String> category;
    private String dateOfBirth;
    
    public List<QuestionnaireContextEnum> getRequiredQuestionnaireContext() {
        return requiredQuestionnaireContext;
    }
    
    public List<VaccineInput> getVaccines() {
        return vaccines;
    }
    
    public List<String> getCategory() {
        return category;
    }
    
    public String getDateOfBirth() {
        return dateOfBirth;
    }
}
