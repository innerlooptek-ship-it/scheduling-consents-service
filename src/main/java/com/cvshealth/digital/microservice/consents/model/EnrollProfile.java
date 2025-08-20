package com.cvshealth.digital.microservice.consents.model;

import com.cvshealth.digital.microservice.consents.enums.ProfileIdTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnrollProfile {

    private String id;
    private ProfileIdTypeEnum idType;
}
