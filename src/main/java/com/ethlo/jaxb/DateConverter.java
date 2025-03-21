package com.ethlo.jaxb;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class DateConverter {
    private static boolean xsdStandard;

    private DateConverter() {
    }

    /**
     * If set to true, the dateTime values will only contain millis if they are non-zero.
     * If set to false, the dateTime will always output millis, even if they are all zero.
     *
     * @param xsdStandard True if you want to omit millis if they are all zero (default is true)
     */
    public static void setUseXsdStandard(boolean xsdStandard) {
        DateConverter.xsdStandard = xsdStandard;
    }

    public static LocalDate parseDate(String date) {
        return Optional.ofNullable(date).map(LocalDate::parse).orElse(null);
    }

    public static OffsetDateTime parseDateTime(String dateTime) {
        return Optional.ofNullable(dateTime).map(OffsetDateTime::parse).orElse(null);
    }

    // Print LocalDate to String (XML Schema 1.1 format: yyyy-MM-dd)
    public static String printDate(LocalDate date) {
        return Optional.ofNullable(date)
                .map(d -> d.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .orElse(null);
    }

    // Print OffsetDateTime to String (XML Schema 1.1 format: yyyy-MM-dd'T'HH:mm:ss.SSSXXX)
    public static String printDateTime(OffsetDateTime dateTime) {
        return xsdStandard ? xsdStandard(dateTime) : alwaysMillis(dateTime);
    }

    // Print OffsetDateTime to String (XML Schema 1.1 format: yyyy-MM-dd'T'HH:mm:ss.SSSXXX)
    private static String xsdStandard(OffsetDateTime dateTime) {
        return Optional.ofNullable(dateTime)
                .map(d -> d.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .orElse(null);
    }

    // Print OffsetDateTime to String with always showing milliseconds (XML Schema 1.1 format: yyyy-MM-dd'T'HH:mm:ss.SSSXXX)
    private static String alwaysMillis(OffsetDateTime dateTime) {
        return Optional.ofNullable(dateTime)
                .map(d -> d.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")))
                .orElse(null);
    }
}
