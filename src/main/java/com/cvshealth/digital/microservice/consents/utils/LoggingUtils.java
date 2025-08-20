package com.cvshealth.digital.microservice.consents.utils;

import com.cvshealth.digital.microservice.consents.constants.ConsentsConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class LoggingUtils {
    
    @Value( "${custom.logHttpHeaders}" )
  	private List<String> logHttpHeaders;

    /**
     * Entry event logging.
     *
     * @param logger  the logger
     * @param pParams the params
     */
    public void entryEventLogging(Logger logger, Map<String, Object> pParams) {
        if (pParams == null) {
            return;
        }
        Map<String, Object> logMessage = new LinkedHashMap<>();
        logMessage.put("CVSEVENT", "ENTRY");
        logMessage.putAll(pParams);
        
        logger.info(logMessage.toString());
    }

    /**
     * Exit event logging.
     *
     * @param logger  the logger
     * @param pParams the params
     */
    public void exitEventLogging(Logger logger, Map<String, Object> pParams) {
        if (pParams == null) {
            return;
        }
        Map<String, Object> logMessage = new LinkedHashMap<>();
        logMessage.put("CVSEVENT", "EXIT");
        logMessage.putAll(pParams);
        
        logger.info(logMessage.toString());

    }

	/**
	 * INFO event logging.
	 *
	 * @param logger  the logger
	 * @param pParams the params
	 */
	public void infoEventLogging(Logger logger, Map<String, Object> pParams) {
		if (pParams == null) {
			return;
		}
		Map<String, Object> logMessage = new LinkedHashMap<>();
		logMessage.put("CVSEVENT", "INFO");
		logMessage.putAll(pParams);

		logger.info(logMessage.toString());

	}

    /**
     * Error event logging.
     *
     * @param logger  the logger
     * @param pParams the params
     */
    public void errorEventLogging(Logger logger, Map<String, Object> pParams) {
        if (pParams == null) {
            return;
        }
        Map<String, Object> logMessage = new LinkedHashMap<>();
        logMessage.put("CVSEVENT", "ERROR");
        logMessage.putAll(pParams);
        
        logger.info(logMessage.toString());
    }



    /**
     * Populate event map.
     *
     * @param className the class name
     * @param serviceName the service name
     * @param methodName the method name
     * @param serviceDesc the service desc

     * @return the map
     */
	public static Map<String, Object> populateEventMap(String className, String methodName, String serviceName, String serviceDesc,
													   Map<String, String> reqHdrMap) {
		Map<String, Object> eventMap = new LinkedHashMap<>();
		eventMap.put(ConsentsConstants.CLASS_NAME, className);
		eventMap.put(ConsentsConstants.SERVICE_NAME, serviceName);
		eventMap.put(ConsentsConstants.METHOD_NAME, methodName);
		eventMap.put(ConsentsConstants.SERVICE_DESC, serviceDesc);
		eventMap.put(ConsentsConstants.OPERATION_NAME, methodName);

		if(reqHdrMap!=null) {
			populateHeaderInfo(eventMap, reqHdrMap);
		}

		return eventMap;
	}


	public static Map<String, Object> populateHeaderInfo(Map<String, Object> eventMap, Map<String, String> reqHdrMap) {

		if (null != reqHdrMap && !CollectionUtils.isEmpty(reqHdrMap)) {

			if (StringUtils.isNotBlank(reqHdrMap.get(ConsentsConstants.CONST_SRC_LOC_CD))) {
				eventMap.put(ConsentsConstants.CONST_SRC_LOC_CD, reqHdrMap.get(ConsentsConstants.CONST_SRC_LOC_CD));
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(ConsentsConstants.CONST_ORIGIN))) {
				eventMap.put(ConsentsConstants.CONST_ORIGIN, reqHdrMap.get(ConsentsConstants.CONST_ORIGIN));
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(ConsentsConstants.CONST_USER_ID))) {
				eventMap.put(ConsentsConstants.CONST_USER_ID, reqHdrMap.get(ConsentsConstants.CONST_USER_ID));
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(ConsentsConstants.CONST_MSG_SRC_CD))) {
				eventMap.put(ConsentsConstants.CONST_MSG_SRC_CD, reqHdrMap.get(ConsentsConstants.CONST_MSG_SRC_CD));
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(ConsentsConstants.CONST_CATEGORY))) {
				eventMap.put(ConsentsConstants.CONST_CATEGORY, reqHdrMap.get(ConsentsConstants.CONST_CATEGORY)!=null?reqHdrMap.get(ConsentsConstants.CONST_CATEGORY):ConsentsConstants.DEFAULT_CAT);
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(ConsentsConstants.CONST_APP_NAME))) {
				eventMap.put(ConsentsConstants.CONST_CHAN_PLAT, reqHdrMap.get(ConsentsConstants.CONST_APP_NAME));
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(ConsentsConstants.CONST_DEVICE_TYPE))) {
				eventMap.put(ConsentsConstants.CONST_DEVICE_TYPE, reqHdrMap.get(ConsentsConstants.CONST_DEVICE_TYPE));
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(ConsentsConstants.CONST_APP_VERSION))) {
				eventMap.put(ConsentsConstants.CONST_REQ_APP_VERSION, reqHdrMap.get(ConsentsConstants.CONST_APP_VERSION));
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(ConsentsConstants.CONST_REQ_ORIGIN))) {
				eventMap.put(ConsentsConstants.CONST_REQ_ORIGIN, reqHdrMap.get(ConsentsConstants.CONST_REQ_ORIGIN));
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(ConsentsConstants.CONST_AKAMAI_CLIENT_IP))) {
				eventMap.put(ConsentsConstants.CONST_CLIENT_IP, reqHdrMap.get(ConsentsConstants.CONST_AKAMAI_CLIENT_IP));
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(ConsentsConstants.CONST_X_B3_PARENTSPANID))) {
				eventMap.put(ConsentsConstants.CONST_X_B3_PARENTSPANID,
						reqHdrMap.get(ConsentsConstants.CONST_X_B3_PARENTSPANID));
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(ConsentsConstants.CONST_X_B3_SAMPLED))) {
				eventMap.put(ConsentsConstants.CONST_X_B3_SAMPLED, reqHdrMap.get(ConsentsConstants.CONST_X_B3_SAMPLED));
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(ConsentsConstants.CONST_X_B3_SPANID))) {
				eventMap.put(ConsentsConstants.CONST_X_B3_SPANID, reqHdrMap.get(ConsentsConstants.CONST_X_B3_SPANID));
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(ConsentsConstants.CONST_X_B3_TRACEID))) {
				eventMap.put(ConsentsConstants.CONST_X_B3_TRACEID, reqHdrMap.get(ConsentsConstants.CONST_X_B3_TRACEID));
			}



			if (StringUtils.isNotBlank(reqHdrMap.get(ConsentsConstants.CONST_ENV))) {
				eventMap.put(ConsentsConstants.CONST_ENV, reqHdrMap.get(ConsentsConstants.CONST_ENV));
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(ConsentsConstants.CONST_USER_AGENT))) {
				eventMap.put(ConsentsConstants.CONST_USER_AGENT, reqHdrMap.get(ConsentsConstants.CONST_USER_AGENT));
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(ConsentsConstants.CONST_REFERER))) {
				eventMap.put(ConsentsConstants.CONST_REFERER, reqHdrMap.get(ConsentsConstants.CONST_REFERER));
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(ConsentsConstants.CONST_CATEGORY))) {
				eventMap.put(ConsentsConstants.CONST_CATEGORY, reqHdrMap.get(ConsentsConstants.CONST_CATEGORY));
			} else {
				eventMap.put(ConsentsConstants.CONST_CATEGORY, ConsentsConstants.CONST_DEFAULT_CATEGORY);
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(ConsentsConstants.CONST_GRID))) {
				eventMap.put(ConsentsConstants.CONST_GRID, reqHdrMap.get(ConsentsConstants.CONST_GRID));
			} else {
				eventMap.put(ConsentsConstants.CONST_GRID, "default-" + java.util.UUID.randomUUID());
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(ConsentsConstants.CONST_X_GRID))) {
				eventMap.put(ConsentsConstants.CONST_X_GRID, reqHdrMap.get(ConsentsConstants.CONST_X_GRID));
			} else {
				eventMap.put(ConsentsConstants.CONST_X_GRID, "default-" + java.util.UUID.randomUUID());
			}

			if (StringUtils.isNotBlank(reqHdrMap.get(ConsentsConstants.CONST_EXP_ID))) {
				eventMap.put(ConsentsConstants.CONST_EXP_ID, reqHdrMap.get(ConsentsConstants.CONST_EXP_ID));
			}
			if (StringUtils.isNotBlank(reqHdrMap.get(ConsentsConstants.EXPERIENCE_ID))) {
				eventMap.put(ConsentsConstants.EXPERIENCE_ID, reqHdrMap.get(ConsentsConstants.EXPERIENCE_ID));
			}
			if (StringUtils.isNotBlank(reqHdrMap.get(ConsentsConstants.EXPERIENCE_ID_SMALL_CASE))) {
				eventMap.put(ConsentsConstants.EXPERIENCE_ID, reqHdrMap.get(ConsentsConstants.EXPERIENCE_ID_SMALL_CASE));
			}


		}
		return eventMap;
	}
	/**
	 * Populate header info.
	 * 
	 * @param eventMap  the event map
	 * @param reqHdrMap the request header map
	 * @return the event map
	 */

}
