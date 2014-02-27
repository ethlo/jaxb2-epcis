package com.ethlo.gs1.epcis.errors;

import java.util.Set;

import com.ethlo.gs1.epcis.query.EpcisParam;

/**
 * An attempt to execute a query resulted in more data than the service was
 * willing to provide or more than the "maxEventCount" parameter in the query
 * 
 * @author Morten Haraldsen
 * 
 */
public class EpcisQueryTooLargeException extends EpcisQueryException 
{
	private static final long serialVersionUID = 2616720770575024889L;

	public EpcisQueryTooLargeException(Set<EpcisParam<?>> queryParams) 
	{
		super("Query is too large", queryParams);
	}
	
	public EpcisQueryTooLargeException(int max, Set<EpcisParam<?>> queryParams) 
    {
        super("Query is too large. Maximum was " + max, queryParams);
    }
}
