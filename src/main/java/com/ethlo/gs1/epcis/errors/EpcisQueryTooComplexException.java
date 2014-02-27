package com.ethlo.gs1.epcis.errors;

import java.util.Collection;
import java.util.Iterator;

import com.ethlo.gs1.epcis.query.EpcisParam;

/**
 * The specified query parameters, while otherwise valid, implied a query that
 * was more complex than the service was willing to execute.
 * 
 * @author Morten Haraldsen
 * 
 */
public class EpcisQueryTooComplexException extends EpcisQueryException
{
    private static final long serialVersionUID = 2616720770575024889L;

    public EpcisQueryTooComplexException(Collection<EpcisParam<?>> queryParams)
    {
        super("Query is too complex: " + collectionToDelimitedString(queryParams, " AND "), queryParams);
    }

    private static String collectionToDelimitedString(Collection<EpcisParam<?>> queryParams, String delimiter)
    {
        StringBuilder sb = new StringBuilder();
        Iterator<?> it = queryParams.iterator();
        while (it.hasNext())
        {
            sb.append(it.next());
            if (it.hasNext())
            {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }
}
