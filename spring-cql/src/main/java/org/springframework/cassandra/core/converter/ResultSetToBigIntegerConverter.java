package org.springframework.cassandra.core.converter;

import java.math.BigInteger;

public class ResultSetToBigIntegerConverter extends AbstractResultSetToBasicFixedTypeConverter<BigInteger> {

	/* (non-Javadoc)
	 * @see org.springframework.cassandra.core.converter.AbstractResultSetConverter#doConvertSingleValue(java.lang.Object)
	 */
	@Override
	protected BigInteger doConvertSingleValue(Object object) {
		return CONVERTER.convert(object, BigInteger.class);
	}

	/* (non-Javadoc)
	 * @see org.springframework.cassandra.core.converter.AbstractResultSetConverter#getType()
	 */
	@Override
	protected Class<?> getType() {
		return BigInteger.class;
	}
}
