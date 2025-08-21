package com.cvshealth.digital.microservice.consents.service;

import com.cvshealth.digital.microservice.consents.dto.ConsentRequest;
import com.cvshealth.digital.microservice.consents.dto.ConsentResponse;
import com.cvshealth.digital.microservice.consents.exception.CvsException;
import com.cvshealth.digital.microservice.consents.model.GetConsent;
import com.cvshealth.digital.microservice.consents.model.GetConsentInput;
import com.cvshealth.digital.microservice.consents.model.ConsentData;
import com.cvshealth.digital.microservice.consents.utils.LoggingUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

import static com.cvshealth.digital.microservice.consents.constants.ConsentsConstants.*;

@Service
@RequiredArgsConstructor
public class ConsentsService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private final ConsentsRestService consentsRestService;
    private final LoggingUtils loggingUtils;

    public Mono<GetConsent> getSchedulingConsents(String id, String idType, GetConsentInput consentInput, Map<String, Object> tags, Map<String, String> headerMap) throws CvsException {
        logger.debug("Entering getSchedulingConsents method of ConsentsService (GraphQL wrapper)....");

        Map<String,Object> eventMap = new HashMap<>();
        eventMap.putAll(tags);
        tags.put("eventMap", eventMap);

        ConsentRequest restRequest = convertToRestRequest(consentInput);
        
        return consentsRestService.getConsents(restRequest, eventMap, headerMap)
                .map(this::convertToGraphQLResponse)
                .doOnSuccess(response -> logger.debug("Successfully converted REST response to GraphQL format"))
                .doOnError(error -> {
                    logger.error("Error in GraphQL wrapper service: {}", error.getMessage());
                    eventMap.put("error", error.getMessage());
                    loggingUtils.errorEventLogging(logger, eventMap);
                });
    }
    
    private ConsentRequest convertToRestRequest(GetConsentInput graphqlInput) {
        List<ConsentRequest.Patient> patients = graphqlInput.getConsentsDataInput().stream()
                .map(consentDataInput -> ConsentRequest.Patient.builder()
                        .patientReferenceId(consentDataInput.getPatientReferenceId())
                        .dateOfBirth(consentDataInput.getDateOfBirth())
                        .patientId(consentDataInput.getEncMCPatientId())
                        .relationshipToPatient(consentDataInput.getRelation())
                        .build())
                .collect(Collectors.toList());
        
        List<ConsentRequest.Context> contexts = graphqlInput.getConsentContextInput().stream()
                .map(contextInput -> ConsentRequest.Context.builder()
                        .name(contextInput.getConsentContext())
                        .summarizeResults(contextInput.isSummarize())
                        .build())
                .collect(Collectors.toList());
        
        return ConsentRequest.builder()
                .flow(graphqlInput.getFlow())
                .lob(graphqlInput.getLob())
                .modality(graphqlInput.getModality())
                .brand(graphqlInput.getBrand())
                .clinicId(graphqlInput.getClinicId())
                .state(graphqlInput.getState())
                .authType(graphqlInput.getAuthType())
                .patients(patients)
                .contexts(contexts)
                .build();
    }
    
    private GetConsent convertToGraphQLResponse(ConsentResponse restResponse) {
        List<ConsentData> consentDataList = restResponse.getData().getGetSchedulingConsents().getConsentsData().stream()
                .map(patientConsent -> {
                    List<ConsentData.Consent> consents = patientConsent.getConsents().stream()
                            .map(this::convertToGraphQLConsent)
                            .collect(Collectors.toList());
                    
                    return ConsentData.builder()
                            .patientReferenceId(patientConsent.getPatientReferenceId())
                            .consents(consents)
                            .build();
                })
                .collect(Collectors.toList());
        
        return GetConsent.builder()
                .statusCode(restResponse.getData().getGetSchedulingConsents().getStatusCode())
                .statusDescription(restResponse.getData().getGetSchedulingConsents().getStatusDescription())
                .consentsData(consentDataList)
                .build();
    }
    
    private ConsentData.Consent convertToGraphQLConsent(ConsentResponse.ConsentContext restConsent) {
        ConsentData.ConsentDetailsInfo consentDetails = null;
        if (restConsent.getConsent() != null && restConsent.getConsent().getConsents() != null) {
            List<ConsentData.Consent> nestedConsents = restConsent.getConsent().getConsents().stream()
                    .map(this::convertToGraphQLConsentItem)
                    .collect(Collectors.toList());
            
            consentDetails = ConsentData.ConsentDetailsInfo.builder()
                    .type("combined")
                    .consents(nestedConsents)
                    .build();
        }
        
        return ConsentData.Consent.builder()
                .text(restConsent.getText())
                .consentContext(restConsent.getConsentContext())
                .consent(consentDetails)
                .build();
    }
    
    private ConsentData.Consent convertToGraphQLConsentItem(ConsentResponse.ConsentItem restItem) {
        ConsentData.ConsentDetailsInfo nestedConsent = null;
        if (restItem.getConsent() != null && restItem.getConsent().getConsents() != null) {
            List<ConsentData.Consent> nestedConsents = restItem.getConsent().getConsents().stream()
                    .map(this::convertToGraphQLConsentItem)
                    .collect(Collectors.toList());
            
            nestedConsent = ConsentData.ConsentDetailsInfo.builder()
                    .type("related")
                    .consents(nestedConsents)
                    .build();
        }
        
        return ConsentData.Consent.builder()
                .text(restItem.getText())
                .subText(restItem.getSubText())
                .consentType(restItem.getConsentType())
                .required(restItem.getRequired())
                .consentName(restItem.getConsentName())
                .consentLink(restItem.getConsentLink())
                .consentLinkText(restItem.getConsentLinkText())
                .value(restItem.getValue())
                .valueType(restItem.getValueType())
                .isHidden(restItem.getIsHidden())
                .consent(nestedConsent)
                .build();
    }
}
