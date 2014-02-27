package com.ethlo.gs1.epcis.errors;

/**
 * 
 * @author Morten Haraldsen
 *
 */
public class EpcisInvalidURIException extends RuntimeException 
{
	private static final long serialVersionUID = 4291240023852148197L;
	
	public EpcisInvalidURIException(String msg) 
	{
		super(msg);
	}
}