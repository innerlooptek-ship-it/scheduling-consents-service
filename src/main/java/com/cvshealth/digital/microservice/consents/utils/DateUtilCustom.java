package com.cvshealth.digital.microservice.consents.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtilCustom {
    private static final Logger logger = LoggerFactory.getLogger(DateUtilCustom.class);
    
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String MM_DD_YYYY = "MM/dd/yyyy";

    /**
     * Calculate age based on birth date.
     *
     * @param birthDate the birth date in yyyy-MM-dd format
     * @return the age in years
     */
    public static int calculateAge(String birthDate) {
        int age = 0;
        try {
            age = Period.between(LocalDate.parse(birthDate), LocalDate.now()).getYears();
        } catch (Exception e) {
            logger.info("Error parsing date in calculateAge: {}", e.getMessage());
        }
        return age;
    }

    /**
     * Format date for MC API.
     *
     * @param inputDate the input date in yyyy-MM-dd format
     * @return the formatted date in MM/dd/yyyy format
     */
    public static String formatDateForMCAPI(String inputDate) {
        if (StringUtils.isBlank(inputDate)) {
            return null;
        }
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat(YYYY_MM_DD);
            Date date = inputFormat.parse(inputDate);
            SimpleDateFormat outputFormat = new SimpleDateFormat(MM_DD_YYYY);
            return outputFormat.format(date);
        } catch (ParseException e) {
            logger.error("Error parsing date: {}", inputDate, e);
            return null;
        }
    }
}
