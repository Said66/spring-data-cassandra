package org.springframework.cassandra.core.converter;

public class ResultSetToFloatConverter extends AbstractResultSetToBasicFixedTypeConverter<Float> {

	/* (non-Javadoc)
	 * @see org.springframework.cassandra.core.converter.AbstractResultSetConverter#doConvertSingleValue(java.lang.Object)
	 */
	@Override
	protected Float doConvertSingleValue(Object object) {
		return CONVERTER.convert(object, Float.class);
	}

	/* (non-Javadoc)
	 * @see org.springframework.cassandra.core.converter.AbstractResultSetConverter#getType()
	 */
	@Override
	protected Class<?> getType() {
		return Float.class;
	}
}
