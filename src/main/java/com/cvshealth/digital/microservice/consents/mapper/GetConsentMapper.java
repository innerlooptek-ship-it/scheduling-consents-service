package com.cvshealth.digital.microservice.consents.mapper;

import com.cvshealth.digital.microservice.consents.config.ConsentConfig;
import com.cvshealth.digital.microservice.consents.model.ConsentData;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
@MapperConfig(unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface GetConsentMapper {

    default ConsentData toConsentData(ConsentConfig consents, String patientReferenceId) {
        if (consents == null) {
            return null;
        }
        
        return ConsentData.builder()
                .consents(toConsents(consents.getConsents()))
                .patientReferenceId(patientReferenceId)
                .isSummarized(false)
                .build();
    }

    List<ConsentData.Consent> toConsents(List<ConsentConfig.Consent> consent);
}
