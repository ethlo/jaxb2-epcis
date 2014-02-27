package com.ethlo.gs1.epcis.errors;

/**
 * (Not implemented in EPCIS 1.0) 
 * 
 * The specified query is invalid; e.g., it
 * contains a syntax error.
 * 
 * @author Morten Haraldsen
 * 
 */
public class EpcisQueryValidationException extends RuntimeException 
{
	private static final long serialVersionUID = 4673650281039003479L;
	private String paramName;

	public EpcisQueryValidationException(String paramName, String msg) 
	{
		super(msg);
		this.paramName = paramName;
	}

	public String getParamName() 
	{
		return paramName;
	}
}
