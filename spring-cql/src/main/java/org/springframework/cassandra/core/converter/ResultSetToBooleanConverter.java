package org.springframework.cassandra.core.converter;

public class ResultSetToBooleanConverter extends AbstractResultSetToBasicFixedTypeConverter<Boolean> {

	/* (non-Javadoc)
	 * @see org.springframework.cassandra.core.converter.AbstractResultSetConverter#doConvertSingleValue(java.lang.Object)
	 */
	@Override
	protected Boolean doConvertSingleValue(Object object) {
		return CONVERTER.convert(object, Boolean.class);
	}

	/* (non-Javadoc)
	 * @see org.springframework.cassandra.core.converter.AbstractResultSetConverter#getType()
	 */
	@Override
	protected Class<?> getType() {
		return Boolean.class;
	}
}
