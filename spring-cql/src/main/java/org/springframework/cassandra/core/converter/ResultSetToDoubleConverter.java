package org.springframework.cassandra.core.converter;

public class ResultSetToDoubleConverter extends AbstractResultSetToBasicFixedTypeConverter<Double> {

	/* (non-Javadoc)
	 * @see org.springframework.cassandra.core.converter.AbstractResultSetConverter#doConvertSingleValue(java.lang.Object)
	 */
	@Override
	protected Double doConvertSingleValue(Object object) {
		return CONVERTER.convert(object, Double.class);
	}

	/* (non-Javadoc)
	 * @see org.springframework.cassandra.core.converter.AbstractResultSetConverter#getType()
	 */
	@Override
	protected Class<?> getType() {
		return Double.class;
	}
}
