package no.difi.sdp.testavsender.configuration.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.WebServiceMessage;

public class StringUtil {

	private final static Logger LOGGER = LoggerFactory.getLogger(StringUtil.class);
	
	private final static String BINARY_MARKER = "Content-Transfer-Encoding: binary";
	private final static  String MIME_BOUNDARY_MARKER = "--";
	private final static  String BINARY_CONTENT_PLACEHOLDER = "\n...BINÆRT INNHOLD ...\n";
	private final static  String NEW_LINE = "\n";
	
	public String removeBinaryContentFromMimeMessage(String rawMessage) {
    	if (! rawMessage.startsWith(MIME_BOUNDARY_MARKER)) {
    		// Not a MIME message
    		return rawMessage;
    	}
    	StringWriter writer = new StringWriter();
    	BufferedReader reader = new BufferedReader(new StringReader(rawMessage));
    	try {
    		String mimeBoundary = reader.readLine();
    		writer.append(mimeBoundary);
    		writer.append(NEW_LINE);
    		String line;
    		boolean isBinary = false;
    		while ((line = reader.readLine()) != null) {
    			if (line.startsWith(mimeBoundary)) {
    				// The MIME boundary marks the end of binary content
					isBinary = false;
				} else if (line.startsWith(BINARY_MARKER)) {
					// The binary marker marks the start of binary content
					isBinary = true;
					writer.append(line); // Outputs the binary marker
					writer.append(NEW_LINE);
					writer.append(BINARY_CONTENT_PLACEHOLDER); // Outputs a placeholder for the binary content
					writer.append(NEW_LINE);
				}
    			if (! isBinary) {
    				writer.append(line); // Outputs non-binary content
    				writer.append(NEW_LINE);
    			}
    		}
    	} catch (IOException e) {
    		LOGGER.error("Error removing binary content from message", e);
    	}
    	return writer.toString();
    }
    
	public void transform(WebServiceMessage message, StringWriter stringWriter) {
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			message.writeTo(outputStream);
		} catch (IOException e) {
			LOGGER.error("Failed capturing message", e);
		}
		String rawMessage = outputStream.toString();
		String messageWithoutBinaryContent = removeBinaryContentFromMimeMessage(rawMessage);
		stringWriter.write(messageWithoutBinaryContent);
		try {
			outputStream.close();
		} catch (IOException e) {
			LOGGER.error("Failed closing outputstream", e);
		}
		
    }
    
    public void transform(Source source, StringWriter stringWriter) {
    	if (source != null) {
    		try {
	    		Transformer transformer = TransformerFactory.newInstance().newTransformer();
	    		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	    		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
	    		transformer.transform(source, new StreamResult(stringWriter));
    		} catch (Exception e) {
    			LOGGER.error("Failed capturing message", e);
    		}
    	}
    }
    
    public void marshalJaxbObject(Object jaxbObject, StringWriter stringWriter) {
    	try {
    		Marshaller m = JAXBContext.newInstance(jaxbObject.getClass()).createMarshaller();
        	m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        	m.marshal(jaxbObject, stringWriter);
    	} catch (JAXBException e) {
    		LOGGER.error("Failed marsharalling JAXB object", e);
    	}
    }
    
    public String toString(Exception e) {
		StringWriter stringWriter = new StringWriter();
		e.printStackTrace(new PrintWriter(stringWriter));
		return stringWriter.toString();
	}
	
    public String nullIfEmpty(String string) {
    	if (string == null || string.isEmpty()) {
    		return null;
    	}
    	return string;
    }
    
    public String nullIfEmpty(StringWriter stringWriter) {
    	if (stringWriter == null) {
    		return null;
    	}
    	return nullIfEmpty(stringWriter.toString());
    }
    
    public String nullIfEmpty(Holder<StringWriter> stringWriterHolder) {
    	if (stringWriterHolder == null) {
    		return null;
    	}
    	return nullIfEmpty(stringWriterHolder.getValue());
    }
    
    public static List<Integer> toIntList(String string) {
		if (string == null || string.isEmpty()) {
			return null;
		}
		List<Integer> intList = new ArrayList<Integer>();
		String[] intStrings = string.split(",");
		for (String intString : intStrings) {
			intList.add(Integer.valueOf(intString));
		}
		return intList;
	}

}
