package com.ethlo.gs1.epcis.errors;


import java.util.Collection;

import com.ethlo.gs1.epcis.query.EpcisParam;

/**
 * 
 * @author mha
 *
 */
public class EpcisQueryException extends RuntimeException 
{
	private static final long serialVersionUID = -8995860550920055338L;
	protected Collection<EpcisParam<?>> queryparams;

	public EpcisQueryException(String msg, Collection<EpcisParam<?>> queryParams) 
	{
		super(msg);
		this.queryparams = queryParams;
	}

	public Collection<EpcisParam<?>> getQueryparams() 
	{
		return queryparams;
	}
}
