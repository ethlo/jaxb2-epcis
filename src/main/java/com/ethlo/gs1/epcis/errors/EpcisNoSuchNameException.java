package com.ethlo.gs1.epcis.errors;

/**
 * The specified query name does not exist.
 * 
 * @author Morten Haraldsen
 *
 */
public class EpcisNoSuchNameException extends RuntimeException 
{
	private static final long serialVersionUID = 4673650281039003479L;
	private String paramName;

	public EpcisNoSuchNameException(String paramName, String msg) 
	{
		super(msg);
		this.paramName = paramName;
	}

	public String getParamName() 
	{
		return paramName;
	}
}
