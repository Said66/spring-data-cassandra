package org.springframework.cassandra.core.converter;

import java.net.InetAddress;

public class ResultSetToInetAddressConverter extends AbstractResultSetToBasicFixedTypeConverter<InetAddress> {

	/* (non-Javadoc)
	 * @see org.springframework.cassandra.core.converter.AbstractResultSetConverter#doConvertSingleValue(java.lang.Object)
	 */
	@Override
	protected InetAddress doConvertSingleValue(Object object) {
		return CONVERTER.convert(object, InetAddress.class);
	}

	/* (non-Javadoc)
	 * @see org.springframework.cassandra.core.converter.AbstractResultSetConverter#getType()
	 */
	@Override
	protected Class<?> getType() {
		return InetAddress.class;
	}
}
