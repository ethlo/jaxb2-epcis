package com.ethlo.gs1.epcis.errors;
/**
 * 
 * @author Morten Haraldsen
 *
 */
public class EpcisDuplicateNameException extends RuntimeException 
{
    private static final long serialVersionUID = 6539749306183124282L;
 
    private String paramName;

	public EpcisDuplicateNameException(String paramName, String msg) 
	{
		super(msg);
		this.paramName = paramName;
	}

	public String getParamName() 
	{
		return paramName;
	}
}
