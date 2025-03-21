package com.ethlo.jaxb;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class DateConverterTest {

    @Test
    void printDate() {
        assertThat(DateConverter.printDate(LocalDate.of(2000, 1, 6))).isEqualTo("2000-01-06");
    }

    @Test
    void printDateTimeNonZeroSeconds() {
        final OffsetDateTime input = OffsetDateTime.of(2000, 1, 6, 23, 0, 0, 33_000_000, ZoneOffset.UTC);
        DateConverter.setUseXsdStandard(true);
        assertThat(DateConverter.printDateTime(input)).isEqualTo("2000-01-06T23:00:00.033Z");
        DateConverter.setUseXsdStandard(false);
        assertThat(DateConverter.printDateTime(input)).isEqualTo("2000-01-06T23:00:00.033Z");
    }

    @Test
    void printDateTimeZeroSeconds() {
        final OffsetDateTime input = OffsetDateTime.of(2000, 1, 6, 23, 0, 0, 0, ZoneOffset.UTC);
        DateConverter.setUseXsdStandard(true);
        assertThat(DateConverter.printDateTime(input)).isEqualTo("2000-01-06T23:00:00Z");
        DateConverter.setUseXsdStandard(false);
        assertThat(DateConverter.printDateTime(input)).isEqualTo("2000-01-06T23:00:00.000Z");
    }

    @Test
    void printDateTimeMillis() {
        assertThat(DateConverter.printDateTime(OffsetDateTime.of(2000, 1, 6, 23, 0, 0, 1_000_000, ZoneOffset.UTC))).isEqualTo("2000-01-06T23:00:00.001Z");
    }
}