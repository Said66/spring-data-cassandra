package org.springframework.cassandra.core.converter;

public class ResultSetToLongConverter extends AbstractResultSetToBasicFixedTypeConverter<Long> {

	/* (non-Javadoc)
	 * @see org.springframework.cassandra.core.converter.AbstractResultSetConverter#doConvertSingleValue(java.lang.Object)
	 */
	@Override
	protected Long doConvertSingleValue(Object object) {
		return CONVERTER.convert(object, Long.class);
	}

	/* (non-Javadoc)
	 * @see org.springframework.cassandra.core.converter.AbstractResultSetConverter#getType()
	 */
	@Override
	protected Class<?> getType() {
		return Long.class;
	}
}
