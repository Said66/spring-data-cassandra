package org.springframework.cassandra.core.converter;

import java.nio.ByteBuffer;

public class ResultSetToByteBufferConverter extends AbstractResultSetConverter<ByteBuffer> {

	/* (non-Javadoc)
	 * @see org.springframework.cassandra.core.converter.AbstractResultSetConverter#doConvertSingleValue(java.lang.Object)
	 */
	@Override
	protected ByteBuffer doConvertSingleValue(Object object) {

		if (!(object instanceof ByteBuffer)) {
			doThrow("value");
		}

		return (ByteBuffer) object;
	}

	/* (non-Javadoc)
	 * @see org.springframework.cassandra.core.converter.AbstractResultSetConverter#getType()
	 */
	@Override
	protected Class<?> getType() {
		return ByteBuffer.class;
	}
}
