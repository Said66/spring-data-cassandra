package org.springframework.cassandra.core.converter;

import java.math.BigDecimal;

public class ResultSetToBigDecimalConverter extends AbstractResultSetToBasicFixedTypeConverter<BigDecimal> {

	/* (non-Javadoc)
	 * @see org.springframework.cassandra.core.converter.AbstractResultSetConverter#doConvertSingleValue(java.lang.Object)
	 */
	@Override
	protected BigDecimal doConvertSingleValue(Object object) {
		return CONVERTER.convert(object, BigDecimal.class);
	}

	/* (non-Javadoc)
	 * @see org.springframework.cassandra.core.converter.AbstractResultSetConverter#getType()
	 */
	@Override
	protected Class<?> getType() {
		return BigDecimal.class;
	}
}
