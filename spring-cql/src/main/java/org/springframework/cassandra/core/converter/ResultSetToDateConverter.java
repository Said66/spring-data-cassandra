package org.springframework.cassandra.core.converter;

import java.util.Date;

public class ResultSetToDateConverter extends AbstractResultSetToBasicFixedTypeConverter<Date> {

	/* (non-Javadoc)
	 * @see org.springframework.cassandra.core.converter.AbstractResultSetConverter#doConvertSingleValue(java.lang.Object)
	 */
	@Override
	protected Date doConvertSingleValue(Object object) {
		return CONVERTER.convert(object, Date.class);
	}

	/* (non-Javadoc)
	 * @see org.springframework.cassandra.core.converter.AbstractResultSetConverter#getType()
	 */
	@Override
	protected Class<?> getType() {
		return Date.class;
	}
}
