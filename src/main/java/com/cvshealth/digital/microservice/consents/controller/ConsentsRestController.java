package com.cvshealth.digital.microservice.consents.controller;

import com.cvshealth.digital.microservice.consents.dto.ConsentRequest;
import com.cvshealth.digital.microservice.consents.dto.ConsentResponse;
import com.cvshealth.digital.microservice.consents.exception.CvsException;
import com.cvshealth.digital.microservice.consents.service.ConsentsRestService;
import com.cvshealth.digital.microservice.consents.utils.LoggingUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static com.cvshealth.digital.microservice.consents.utils.LoggingUtils.populateEventMap;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ConsentsRestController {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ConsentsRestService consentsRestService;
    private final LoggingUtils logUtils;
    
    @PostMapping("/consents")
    public Mono<ResponseEntity<ConsentResponse>> getConsents(
            @Valid @RequestBody ConsentRequest request,
            HttpServletRequest httpRequest
    ) throws CvsException {
        long startTime = System.currentTimeMillis();
        
        Map<String, String> headerMap = extractHeaders(httpRequest);
        
        Map<String, Object> eventMap = populateEventMap(
                "ConsentsRestController",
                "getConsents",
                "getConsents",
                "getConsents",
                headerMap
        );
        
        logUtils.entryEventLogging(logger, eventMap);
        
        return consentsRestService.getConsents(request, eventMap, headerMap)
                .map(ResponseEntity::ok)
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
    
    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headerMap = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headerMap.put(headerName, request.getHeader(headerName));
        }
        return headerMap;
    }
}
