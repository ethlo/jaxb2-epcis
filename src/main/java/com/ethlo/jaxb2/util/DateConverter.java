package com.ethlo.jaxb2.util;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.bind.DatatypeConverter;

/**
 * 
 * @author Morten Haraldsen
 *
 */
public class DateConverter
{

    public static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("GMT");

    public static Date parseDate(String lexicalXSDDate)
    {
        return DatatypeConverter.parseDate(lexicalXSDDate).getTime();
    }

    public static Date parseDateTime(String lexicalXSDDateTime)
    {
        return DatatypeConverter.parseDateTime(lexicalXSDDateTime).getTime();
    }

    public static String printDate(Date val)
    {
        if (val == null)
        {
            return null;
        }
        return DatatypeConverter.printDate(toCalendar(val));
    }

    public static String printDateTime(Date val)
    {
        if (val == null)
        {
            return null;
        }
        return DatatypeConverter.printDateTime(toCalendar(val));
    }

    private static GregorianCalendar toCalendar(Date val)
    {
        final GregorianCalendar cal = new GregorianCalendar(UTC_TIME_ZONE);
        cal.setTime(val);
        return cal;
    }
}
