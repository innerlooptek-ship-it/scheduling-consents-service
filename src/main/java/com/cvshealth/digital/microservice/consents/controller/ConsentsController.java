package com.cvshealth.digital.microservice.consents.controller;

import com.cvshealth.digital.microservice.consents.exception.CvsException;
import com.cvshealth.digital.microservice.consents.model.EnrollProfile;
import com.cvshealth.digital.microservice.consents.model.GetConsent;
import com.cvshealth.digital.microservice.consents.model.GetConsentInput;
import com.cvshealth.digital.microservice.consents.service.ConsentsService;
import com.cvshealth.digital.microservice.consents.utils.LoggingUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.cvshealth.digital.microservice.consents.utils.LoggingUtils.populateEventMap;

@Controller
@RequiredArgsConstructor
public class ConsentsController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ConsentsService consentsService;
    private final LoggingUtils logUtils;

    @QueryMapping
    public Mono<GetConsent> getSchedulingConsents(
            @Argument String id,
            @Argument String idType,
            @Argument GetConsentInput consentInput,
            @Argument EnrollProfile profile,
            @ContextValue("headers") Map<String, String> headerMap
    ) throws CvsException {
        long startTime = System.currentTimeMillis();

        Map<String, Object> eventMap = populateEventMap(
                "ConsentsController",
                "getSchedulingConsents",
                "getSchedulingConsents",
                "getSchedulingConsents",
                headerMap
        );

        logUtils.entryEventLogging(logger, eventMap);

        return consentsService.getSchedulingConsents(id, idType, consentInput, eventMap, headerMap)
                .doOnSuccess(response -> {
                    long endTime = System.currentTimeMillis();
                    eventMap.put("responseTime", endTime - startTime);
                    logUtils.exitEventLogging(logger, eventMap);
                })
                .doOnError(error -> {
                    long endTime = System.currentTimeMillis();
                    eventMap.put("responseTime", endTime - startTime);
                    eventMap.put("error", error.getMessage());
                    logUtils.errorEventLogging(logger, eventMap);
                });
    }
}
