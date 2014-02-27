package com.ethlo.gs1.epcis.errors;

/**
 * One or more query parameters are invalid, including any of the following
 * situations: 
 * <ul>
 * 	<li>the parameter name is not a recognized parameter for the specified query</li> 
 *  <li>the value of a parameter is of the wrong type or out of range</li> 
 *  <li>two or more query parameters have the same parameter name</li>
 * </ul>
 * 
 * @author Morten Haraldsen
 * 
 */
public class EpcisQueryParameterException extends RuntimeException 
{
	private static final long serialVersionUID = 4673650281039003479L;
	private String paramName;

	public EpcisQueryParameterException(String paramName, String msg) 
	{
		super(msg);
		this.paramName = paramName;
	}

	public String getParamName() 
	{
		return paramName;
	}
}
