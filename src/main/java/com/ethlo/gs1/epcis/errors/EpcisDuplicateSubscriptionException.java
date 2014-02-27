package com.ethlo.gs1.epcis.errors;

/**
 * The specified subscriptionID is identical to a previous subscription that was
 * created and not yet unsubscribed.
 * 
 * @author Morten Haraldsen
 * 
 */
public class EpcisDuplicateSubscriptionException extends RuntimeException 
{
	private static final long serialVersionUID = 4673650281039003479L;
	private String paramName;

	public EpcisDuplicateSubscriptionException(String paramName, String msg) 
	{
		super(msg);
		this.paramName = paramName;
	}

	public String getParamName() 
	{
		return paramName;
	}
}
