package com.ethlo.gs1.epcis.errors;

/**
 * The specified query name may not be used with subscribe, only with poll.
 * 
 * @author Morten Haraldsen
 * 
 */
public class EpcisSubscribeNotPermittedException extends RuntimeException 
{
	private static final long serialVersionUID = -2607188185789587243L;
	
	public EpcisSubscribeNotPermittedException(String msg) 
	{
		super(msg);
	}
}
