package com.ethlo.gs1.epcis.errors;

/**
 * A generic exception thrown by the implementation for reasons that are
 * implementation-specific. This exception contains one additional element: a
 * severity member whose values are either ERROR or SEVERE. ERROR indicates that
 * the EPCIS implementation is left in the same state it had before the
 * operation was attempted. SEVERE indicates that the EPCIS implementation is
 * left in an indeterminate state.
 * 
 * @author Morten Haraldsen
 * 
 */
public class EpcisImplementationException extends RuntimeException 
{
	private static final long serialVersionUID = -185045185725377889L;
	private String queryName;

	public EpcisImplementationException(String msg, Throwable cause)
	{
		this(null, msg, cause);
	}
	
	public EpcisImplementationException(String queryName, String msg, Throwable cause)
	{
		super(msg, cause);
		this.queryName = queryName;
	}

	public EpcisImplementationException(String msg)
	{
		super(msg);
	}

	public String getQueryname() 
	{
		return this.queryName;
	}
}
