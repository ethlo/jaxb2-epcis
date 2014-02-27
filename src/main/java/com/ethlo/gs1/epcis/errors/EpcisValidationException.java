package com.ethlo.gs1.epcis.errors;

/**
 * The input to the operation was not syntactically valid according to the
 * syntax defined by the binding. Each binding specifies the particular
 * circumstances under which this exception is raised.
 * 
 * @author Morten Haraldsen
 * 
 */
public class EpcisValidationException extends RuntimeException 
{
	private static final long serialVersionUID = 4673650281039003479L;
	private String paramName;

	public EpcisValidationException(String paramName, String msg) 
	{
		super(msg);
		this.paramName = paramName;
	}

	public String getParamName() 
	{
		return paramName;
	}
}
