package com.ethlo.gs1.epcis.errors;

/**
 * The specified subscription controls was invalid; e.g., the schedule
 * parameters were out of range, the trigger URI could not be parsed or did not
 * name a recognized trigger, etc.
 * 
 * @author Morten Haraldsen
 * 
 */
public class EpcisSubscriptionControlsException extends RuntimeException
{
	private static final long serialVersionUID = 4673650281039003479L;
	private String paramName;

	public EpcisSubscriptionControlsException(String paramName, String msg) 
	{
		super(msg);
		this.paramName = paramName;
	}

	public String getParamName() 
	{
		return paramName;
	}
}
