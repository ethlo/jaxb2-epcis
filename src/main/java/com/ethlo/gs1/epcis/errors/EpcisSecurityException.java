package com.ethlo.gs1.epcis.errors;

/** 
 * The operation was not permitted due to an access control violation or other
 * security concern. This includes the case where the service wishes to deny
 * authorization to execute a particular operation based on the authenticated
 * client identity. The specific circumstances that may cause this exception are
 * implementation-specific, and outside the scope of this specification.
 * 
 * @author Morten Haraldsen
 * 
 */
public class EpcisSecurityException extends RuntimeException 
{
	private static final long serialVersionUID = 2645788980101384294L;
	
	public EpcisSecurityException(String msg, Throwable cause) 
	{
		super(msg, cause);
	}
}
