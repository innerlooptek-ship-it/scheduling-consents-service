package com.cvshealth.digital.microservice.consents.config;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GetConsentConfigLoader {

    private static final String CLASS_NAME = "GetConsentConfigLoader";
    private static final Logger logger = LoggerFactory.getLogger(CLASS_NAME);

    @Autowired
    DHSSchedulingConfigs dhsSchedulingConfigs;

    @Getter
    private Map<String, List<ConsentConfig>> consentDataMap = Collections.emptyMap();

    @Getter
    private Map<String, ConsentTempConfig> consentByContextMap = Collections.emptyMap();

    @PostConstruct
    private void cacheConsentConfigData() {
        Map<String, List<ConsentConfig>> tempDataMap = new ConcurrentHashMap<>();
        Map<String, ConsentTempConfig> tempDataByContextMap = new ConcurrentHashMap<>();

        String consentConfigPath = dhsSchedulingConfigs.getConsentsConfig();

        File consentConfigFile = new File(consentConfigPath);
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(consentConfigFile))) {
            byte[] bdata = FileCopyUtils.copyToByteArray(inputStream);
            String data = new String(bdata, StandardCharsets.UTF_8);

            List<ConsentConfig> consentConfigList = com.cvshealth.digital.microservice.consents.utils.DHSSchedulerUtils.fromJSON(
                    data, new TypeReference<List<ConsentConfig>>() {});

            logger.debug("Raw data {}", com.cvshealth.digital.microservice.consents.utils.DHSSchedulerUtils.toJSON(consentConfigList, true));

            if (consentConfigList != null && !consentConfigList.isEmpty()) {
                tempDataMap.put("default", Collections.unmodifiableList(consentConfigList));
            }

            if (!CollectionUtils.isEmpty(consentConfigList)) {
                consentConfigList.forEach(consentConfig -> {
                    consentConfig.getConsents().forEach(consentConfig1 -> {
                        consentConfig1.getConsent().getConsents().forEach(consentConfig2 -> {
                            tempDataByContextMap.put(
                                    consentConfig2.getConsentName(),
                                    ConsentTempConfig.builder()
                                            .consentContext(consentConfig1.getConsentContext())
                                            .consents(consentConfig2)
                                            .build()
                            );
                        });
                    });
                });
            }

        } catch (FileNotFoundException e) {
            logger.error("FileNotFoundException....", e);
        } catch (IOException e) {
            logger.error("IOException....", e);
        }

        consentDataMap = Collections.unmodifiableMap(tempDataMap);
        consentByContextMap = Collections.unmodifiableMap(tempDataByContextMap);

        logger.debug("Exiting postConstruct method of cacheConsentConfigData");
    }
}