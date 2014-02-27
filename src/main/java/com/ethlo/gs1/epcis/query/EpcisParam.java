package com.ethlo.gs1.epcis.query;

/**
 * 
 * @author mha
 *
 */
public abstract class EpcisParam<T>
{
	private T value;

	public EpcisParam(T value)
	{
		this.value = value;
	}
	
	public T getValue()
	{
		return this.value;
	}
	
	/**
	 * A string describing this query parameter
	 */
	@Override
	public abstract String toString();
	
	/**
	 * Compare this parameter with the specified parameter to check whether it can co-exist within the same query
	 * @param paramB The parameter to compare with
	 * @return True if this is considered being the same parameter, otherwise false
	 */
	public abstract boolean isSameParameter(EpcisParam<?> paramB);
}
