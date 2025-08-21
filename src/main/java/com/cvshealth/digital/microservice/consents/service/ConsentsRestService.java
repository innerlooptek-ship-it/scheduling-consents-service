package com.cvshealth.digital.microservice.consents.service;

import com.cvshealth.digital.microservice.consents.config.ConsentConfig;
import com.cvshealth.digital.microservice.consents.config.DHSSchedulingConfigs;
import com.cvshealth.digital.microservice.consents.config.GetConsentConfigLoader;
import com.cvshealth.digital.microservice.consents.config.MessageConfig;
import com.cvshealth.digital.microservice.consents.dto.ConsentRequest;
import com.cvshealth.digital.microservice.consents.dto.ConsentResponse;
import com.cvshealth.digital.microservice.consents.dto.MCITGetPatientConsentsResponse;
import com.cvshealth.digital.microservice.consents.enums.*;
import com.cvshealth.digital.microservice.consents.exception.CvsException;
import com.cvshealth.digital.microservice.consents.model.ConsentData;
import com.cvshealth.digital.microservice.consents.model.GetConsentInput;
import com.cvshealth.digital.microservice.consents.utils.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

import static com.cvshealth.digital.microservice.consents.constants.ConsentsConstants.*;

@Service
@RequiredArgsConstructor
public class ConsentsRestService {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private final ValidatorConsentsService validatorConsentsService;
    private final ConsentsMcitService mcitGetPatientConsentsService;
    private final GetConsentConfigLoader getConsentConfigLoader;
    private final ConsentServiceHelper consentServiceHelper;
    private final FeatureProperties featureProperties;
    private final DHSSchedulingConfigs dhsSchedulingConfigs;
    private final LoggingUtils loggingUtils;
    private final MessageConfig messageConfig;
    
    public Mono<ConsentResponse> getConsents(ConsentRequest request, Map<String, Object> tags, Map<String, String> headerMap) throws CvsException {
        logger.debug("Entering getConsents method of ConsentsRestService....");
        
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.putAll(tags);
        tags.put("eventMap", eventMap);
        
        if (!request.getFlow().equalsIgnoreCase("VACCINE")) {
            eventMap.put("methodName", "getConsents");
            eventMap.put(STATUS_CDE, BAD_REQUEST);
            eventMap.put(STATUS_MESSAGE, messageConfig.getMessages().get("getSchedulingConsents.INVALID_FLOW"));
            eventMap.put(STATUS_DESC, "Invalid flow provided in request");
            loggingUtils.errorEventLogging(logger, eventMap);
            return Mono.error(new CvsException(HttpStatus.BAD_REQUEST.value(),
                    "INVALID_FLOW", messageConfig.getMessages().get("getSchedulingConsents.INVALID_FLOW"),
                    "Invalid flow provided in request", "INVALID_FLOW"));
        }
        
        GetConsentInput consentInput = convertToGetConsentInput(request);
        
        validatorConsentsService.validateGetConsentsForScheduling(consentInput, eventMap);
        
        boolean isGroupAppointment = consentInput.getConsentsDataInput() != null && 
                !CollectionUtils.isEmpty(consentInput.getConsentsDataInput()) && 
                consentInput.getConsentsDataInput().size() > 1;
        
        String lob = StringUtils.isNotBlank(consentInput.getLob()) ? consentInput.getLob().toUpperCase() : LobEnum.CLINIC.name();
        String modality = StringUtils.isNotBlank(consentInput.getModality()) ? consentInput.getModality().toUpperCase() : ModalityEnum.BnMInPerson.name();
        String brand = StringUtils.isNotBlank(consentInput.getBrand()) ? consentInput.getBrand().toUpperCase() : BrandEnum.MC.name();
        
        return Flux.fromIterable(consentInput.getConsentsDataInput()).flatMap(consentDataInput -> {
            String patientId = null;
            try {
                patientId = CvsCrypto.decrypt(consentDataInput.getEncMCPatientId(), dhsSchedulingConfigs.getScheduleEncryptDecryptKey());
            } catch (Exception e) {
                tags.put("mrnEncryptionFailed", "true");
            }
            
            Mono<MCITGetPatientConsentsResponse> getPatientConsentsResponseMono = Mono.just(MCITGetPatientConsentsResponse.builder().build());
            if (StringUtils.isNotBlank(patientId)) {
                getPatientConsentsResponseMono = mcitGetPatientConsentsService.getMCITPatientConsents(
                        patientId, consentDataInput.getDateOfBirth(), consentInput.getLob(), 
                        consentInput.getState(), consentInput.getClinicId(), tags, headerMap);
            }
            
            return getPatientConsentsResponseMono.flatMap(mcitGetPatientConsentsResponse -> {
                boolean hasNopConsent = false;
                boolean hasNjiisConsent = false;
                if (mcitGetPatientConsentsResponse.getResponse() != null && 
                    mcitGetPatientConsentsResponse.getResponse().getStatusRec() != null && 
                    mcitGetPatientConsentsResponse.getResponse().getStatusRec().getStatusCode() == 0) {
                    hasNopConsent = mcitGetPatientConsentsResponse.getResponse().getGetPatientConsent2021Response()
                            .getConsentAcknowledgementList().stream()
                            .anyMatch(consentAcknowledgement -> consentAcknowledgement.getConsentKey().equalsIgnoreCase("nop") && consentAcknowledgement.isAcknowledged());
                    hasNjiisConsent = mcitGetPatientConsentsResponse.getResponse().getGetPatientConsent2021Response()
                            .getConsentAcknowledgementList().stream()
                            .anyMatch(consentAcknowledgement -> consentAcknowledgement.getConsentKey().equalsIgnoreCase("njiis") && consentAcknowledgement.isAcknowledged());
                }
                
                boolean isHidden = false;
                int age = DateUtilCustom.calculateAge(consentDataInput.getDateOfBirth());
                String authType = StringUtils.isNotBlank(consentInput.getAuthType()) ? consentInput.getAuthType().toUpperCase() : AuthTypeEnum.GUEST.name();
                if (AuthTypeEnum.LOA1.name().equalsIgnoreCase(authType) || 
                    AuthTypeEnum.LOA2.name().equalsIgnoreCase(authType) || 
                    (AuthTypeEnum.MFA.name().equalsIgnoreCase(authType) && age >= 18)) {
                    isHidden = true;
                }
                
                String relation = null;
                if (StringUtils.isNotBlank(consentDataInput.getRelation())) {
                    relation = ConsentRelationEnum.fromString(consentDataInput.getRelation()).getRelation();
                }
                
                Map<String, Object> variables = new HashMap<>();
                variables.put("state", StringUtils.isNotBlank(consentInput.getState()) ? consentInput.getState().toUpperCase() : null);
                variables.put("age", age);
                variables.put("relation", StringUtils.isNotBlank(relation) ? relation : ConsentRelationEnum.AUTH_REPRESENTATIVE.getRelation());
                variables.put("hasNopConsent", hasNopConsent);
                variables.put("hasNJIISConsent", hasNjiisConsent);
                variables.put("isHidden", isHidden);
                variables.put("isGroupAppointment", isGroupAppointment);
                
                String flow = consentInput.getFlow().toUpperCase();
                List<ConsentConfig> consentConfigList = new ArrayList<>(getConsentConfigLoader.getConsentDataMap().get(flow));
                
                ConsentConfig consents = consentConfigList.stream().filter(consentConfig -> {
                    boolean isLobMatch = consentConfig.getLob().equalsIgnoreCase(lob);
                    boolean isModalityMatch = consentConfig.getModality().equalsIgnoreCase(modality);
                    boolean isBrandMatch = consentConfig.getBrand().equalsIgnoreCase(brand);
                    return isLobMatch && isModalityMatch && isBrandMatch;
                }).findFirst().orElse(null);
                
                if (consentConfigList.isEmpty()) {
                    eventMap.put("methodName", "getConsents");
                    eventMap.put(STATUS_CDE, BAD_REQUEST);
                    eventMap.put(STATUS_MESSAGE, messageConfig.getMessages().get("getSchedulingConsents.NO_CONSENT_CONFIG"));
                    eventMap.put(STATUS_DESC, "No consent configuration found for the provided flow, lob, modality and brand");
                    loggingUtils.errorEventLogging(logger, eventMap);
                    return Mono.error(new CvsException(HttpStatus.BAD_REQUEST.value(),
                            "NO_CONSENT_CONFIG", messageConfig.getMessages().get("getSchedulingConsents.NO_CONSENT_CONFIG"),
                            "No consent configuration found for the provided flow, lob, modality and brand", "NO_CONSENT_CONFIG"));
                }
                
                List<ConsentConfig.Consent> filteredConsents = consents.getConsents().stream()
                        .map(ConsentConfig.Consent::new)
                        .filter(consentData -> StringUtils.isNotBlank(consentData.getConsentContext())
                                && consentDataInput.getConsentContext().stream()
                                .anyMatch(context -> context.equalsIgnoreCase(consentData.getConsentContext())))
                        .map(consentData -> consentServiceHelper.filterConsents(consentData, variables))
                        .peek(consentData -> {
                            if (consentData != null && consentData.getConsentContext().equalsIgnoreCase(ConsentContextEnum.REVIEW.name()) && !isGroupAppointment) {
                                consentData.setSubText(null);
                            }
                        })
                        .filter(filteredConsent -> filteredConsent != null
                                && filteredConsent.getConsent() != null
                                && !CollectionUtils.isEmpty(filteredConsent.getConsent().getConsents()))
                        .toList();
                
                ConsentData consentData = ConsentData.builder()
                        .consents(convertToConsentDataConsents(filteredConsents))
                        .patientReferenceId(consentDataInput.getPatientReferenceId())
                        .isSummarized(false)
                        .build();
                
                return Mono.just(consentData);
            });
        }).collectList()
        .flatMap(consentDataList -> {
            if (featureProperties.getLive().containsKey("mcGroupScheduling") && 
                Boolean.TRUE.equals(featureProperties.getLive().get("mcGroupScheduling"))) {
                return Mono.just(consentServiceHelper.summarizeConsentsForGroup(consentDataList, consentInput));
            } else {
                return Mono.just(consentDataList);
            }
        })
        .flatMap(consentDataList -> {
            ConsentResponse response = convertToConsentResponse(consentDataList);
            return Mono.just(response);
        });
    }
    
    private GetConsentInput convertToGetConsentInput(ConsentRequest request) {
        List<GetConsentInput.ConsentDataInput> consentsDataInputList = request.getPatients().stream()
                .map(patient -> GetConsentInput.ConsentDataInput.builder()
                        .patientReferenceId(patient.getPatientReferenceId())
                        .dateOfBirth(patient.getDateOfBirth())
                        .encMCPatientId(patient.getPatientId())
                        .relation(patient.getRelationshipToPatient())
                        .consentContext(request.getContexts().stream()
                                .map(ConsentRequest.Context::getName)
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
        
        List<GetConsentInput.ConsentContextInput> consentContextInputList = request.getContexts().stream()
                .map(context -> GetConsentInput.ConsentContextInput.builder()
                        .consentContext(context.getName())
                        .summarize(context.getSummarizeResults())
                        .build())
                .collect(Collectors.toList());
        
        return GetConsentInput.builder()
                .flow(request.getFlow())
                .lob(request.getLob())
                .modality(request.getModality())
                .brand(request.getBrand())
                .clinicId(request.getClinicId())
                .state(request.getState())
                .authType(request.getAuthType())
                .consentsDataInput(consentsDataInputList)
                .consentContextInput(consentContextInputList)
                .build();
    }
    
    private List<ConsentData.Consent> convertToConsentDataConsents(List<ConsentConfig.Consent> configConsents) {
        return configConsents.stream()
                .map(this::convertToConsentDataConsent)
                .collect(Collectors.toList());
    }
    
    private ConsentData.Consent convertToConsentDataConsent(ConsentConfig.Consent configConsent) {
        ConsentData.Consent.ConsentBuilder builder = ConsentData.Consent.builder()
                .text(configConsent.getText())
                .consentContext(configConsent.getConsentContext())
                .consentType(configConsent.getConsentType())
                .required(configConsent.getRequired())
                .consentName(configConsent.getConsentName())
                .consentLink(configConsent.getConsentLink())
                .consentLinkText(configConsent.getConsentLinkText())
                .isHidden(configConsent.getIsHidden())
                .value(configConsent.getValue())
                .valueType(configConsent.getValueType())
                .subText(configConsent.getSubText());
        
        if (configConsent.getConsent() != null && !CollectionUtils.isEmpty(configConsent.getConsent().getConsents())) {
            ConsentData.ConsentDetailsInfo consentDetails = ConsentData.ConsentDetailsInfo.builder()
                    .type(configConsent.getConsent().getType())
                    .consents(convertToConsentDataConsents(configConsent.getConsent().getConsents()))
                    .build();
            builder.consent(consentDetails);
        }
        
        return builder.build();
    }
    
    private ConsentResponse convertToConsentResponse(List<ConsentData> consentDataList) {
        List<ConsentResponse.PatientConsent> patientConsents = consentDataList.stream()
                .map(consentData -> ConsentResponse.PatientConsent.builder()
                        .patientReferenceId(consentData.getPatientReferenceId())
                        .consents(consentData.getConsents().stream()
                                .map(this::convertToResponseConsentContext)
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
        
        ConsentResponse.GetSchedulingConsents getSchedulingConsents = ConsentResponse.GetSchedulingConsents.builder()
                .statusCode(SUCCESS_MSG)
                .statusDescription(SUCCESS_MSG)
                .consentsData(patientConsents)
                .build();
        
        ConsentResponse.ConsentData data = ConsentResponse.ConsentData.builder()
                .getSchedulingConsents(getSchedulingConsents)
                .build();
        
        return ConsentResponse.builder()
                .data(data)
                .build();
    }
    
    private ConsentResponse.ConsentContext convertToResponseConsentContext(ConsentData.Consent consent) {
        ConsentResponse.ConsentDetails consentDetails = null;
        if (consent.getConsent() != null && !CollectionUtils.isEmpty(consent.getConsent().getConsents())) {
            List<ConsentResponse.ConsentItem> consentItems = consent.getConsent().getConsents().stream()
                    .map(this::convertToResponseConsentItem)
                    .collect(Collectors.toList());
            consentDetails = ConsentResponse.ConsentDetails.builder()
                    .consents(consentItems)
                    .build();
        }
        
        return ConsentResponse.ConsentContext.builder()
                .text(consent.getText())
                .consentContext(consent.getConsentContext())
                .consent(consentDetails)
                .build();
    }
    
    private ConsentResponse.ConsentItem convertToResponseConsentItem(ConsentData.Consent consent) {
        ConsentResponse.ConsentDetails nestedConsent = null;
        if (consent.getConsent() != null && !CollectionUtils.isEmpty(consent.getConsent().getConsents())) {
            List<ConsentResponse.ConsentItem> nestedItems = consent.getConsent().getConsents().stream()
                    .map(this::convertToResponseConsentItem)
                    .collect(Collectors.toList());
            nestedConsent = ConsentResponse.ConsentDetails.builder()
                    .consents(nestedItems)
                    .build();
        }
        
        return ConsentResponse.ConsentItem.builder()
                .text(consent.getText())
                .subText(consent.getSubText())
                .consentType(consent.getConsentType())
                .required(consent.getRequired())
                .consentName(consent.getConsentName())
                .consentLink(consent.getConsentLink())
                .consentLinkText(consent.getConsentLinkText())
                .isHidden(consent.getIsHidden())
                .value(consent.getValue())
                .valueType(consent.getValueType())
                .consent(nestedConsent)
                .build();
    }
}
