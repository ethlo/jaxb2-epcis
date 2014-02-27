package com.ethlo.gs1.epcis.errors;

/**
 * The specified subscriptionID does not exist.
 * 
 * @author Morten Haraldsen
 * 
 */
public class EpcisNoSuchSubscriptionException extends RuntimeException 
{
	private static final long serialVersionUID = 4673650281039003479L;
	private String paramName;

	public EpcisNoSuchSubscriptionException(String paramName, String msg) 
	{
		super(msg);
		this.paramName = paramName;
	}

	public String getParamName() 
	{
		return paramName;
	}
}
