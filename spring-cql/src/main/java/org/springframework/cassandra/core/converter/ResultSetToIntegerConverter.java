package org.springframework.cassandra.core.converter;

public class ResultSetToIntegerConverter extends AbstractResultSetToBasicFixedTypeConverter<Integer> {

	/* (non-Javadoc)
	 * @see org.springframework.cassandra.core.converter.AbstractResultSetConverter#doConvertSingleValue(java.lang.Object)
	 */
	@Override
	protected Integer doConvertSingleValue(Object object) {
		return CONVERTER.convert(object, Integer.class);
	}

	/* (non-Javadoc)
	 * @see org.springframework.cassandra.core.converter.AbstractResultSetConverter#getType()
	 */
	@Override
	protected Class<?> getType() {
		return Integer.class;
	}
}
