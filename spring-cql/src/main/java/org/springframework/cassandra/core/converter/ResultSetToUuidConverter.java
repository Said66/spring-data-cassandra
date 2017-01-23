package org.springframework.cassandra.core.converter;

import java.util.UUID;

public class ResultSetToUuidConverter extends AbstractResultSetToBasicFixedTypeConverter<UUID> {

	/* (non-Javadoc)
	 * @see org.springframework.cassandra.core.converter.AbstractResultSetConverter#doConvertSingleValue(java.lang.Object)
	 */
	@Override
	protected UUID doConvertSingleValue(Object object) {
		return CONVERTER.convert(object, UUID.class);
	}

	/* (non-Javadoc)
	 * @see org.springframework.cassandra.core.converter.AbstractResultSetConverter#getType()
	 */
	@Override
	protected Class<?> getType() {
		return UUID.class;
	}
}
