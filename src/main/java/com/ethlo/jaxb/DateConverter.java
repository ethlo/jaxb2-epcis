package com.ethlo.jaxb;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

public class DateConverter
{
    private DateConverter()
    {
    }

    public static LocalDate parseDate(String date)
    {
        return Optional.ofNullable(date).map(LocalDate::parse).orElse(null);
    }

    public static OffsetDateTime parseDateTime(String dateTime)
    {
        return Optional.ofNullable(dateTime).map(OffsetDateTime::parse).orElse(null);
    }

    public static String printDate(LocalDate date)
    {
        return Optional.ofNullable(date).map(LocalDate::toString).orElse(null);
    }

    public static String printDateTime(OffsetDateTime dateTime)
    {
        return Optional.ofNullable(dateTime).map(OffsetDateTime::toString).orElse(null);
    }
}
