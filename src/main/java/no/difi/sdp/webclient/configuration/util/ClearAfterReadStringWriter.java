package no.difi.sdp.webclient.configuration.util;

import java.io.IOException;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A StringWriter wrapper that clears it's contents after it's been read using the toString() method. 
 */
public class ClearAfterReadStringWriter extends StringWriter {

	private final static Logger LOGGER = LoggerFactory.getLogger(ClearAfterReadStringWriter.class);
	
	private StringWriter stringWriter;
	
	public ClearAfterReadStringWriter() {
		stringWriter = new StringWriter();
	}
	
	private void clear() {
		try {
			stringWriter.close();
		} catch (IOException e) {
			LOGGER.error("Error closing stringwriter", e);
		}
		stringWriter = new StringWriter();
	}
	
	@Override
	public void write(int c) {
		stringWriter.write(c);
	}

	@Override
	public void write(char[] cbuf, int off, int len) {
		stringWriter.write(cbuf, off, len);
	}

	@Override
	public void write(String str) {
		stringWriter.write(str);
	}

	@Override
	public void write(String str, int off, int len) {
		stringWriter.write(str, off, len);
	}

	@Override
	public StringWriter append(CharSequence csq) {
		return stringWriter.append(csq);
	}

	@Override
	public StringWriter append(CharSequence csq, int start, int end) {
		return stringWriter.append(csq, start, end);
	}

	@Override
	public StringWriter append(char c) {
		return stringWriter.append(c);
	}

	@Override
	public String toString() {
		String string = stringWriter.toString();
		clear();
		return string;
	}

	@Override
	public StringBuffer getBuffer() {
		return stringWriter.getBuffer();
	}

	@Override
	public void flush() {
		stringWriter.flush();
	}

	@Override
	public void close() throws IOException {
		stringWriter.close();
	}

	@Override
	public void write(char[] cbuf) throws IOException {
		stringWriter.write(cbuf);
	}

}
