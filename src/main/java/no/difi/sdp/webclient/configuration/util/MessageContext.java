package no.difi.sdp.webclient.configuration.util;

import java.io.StringWriter;

import org.springframework.beans.factory.annotation.Autowired;

public class MessageContext {

	@Autowired
	private StringWriter xmlRetrievePersonsRequest;
	
	@Autowired
	private StringWriter xmlRetrievePersonsRequestPayload;
	
	@Autowired
	private StringWriter xmlRetrievePersonsResponse;
	
	@Autowired
	private StringWriter xmlRetrievePersonsResponsePayload;
	
	@Autowired
	private StringWriter xmlSendMessageRequest;
	
	@Autowired
	private StringWriter xmlSendMessageRequestPayload;
	
	@Autowired
	private StringWriter xmlSendMessageResponse;
	
	@Autowired
	private StringWriter xmlRetrieveMessageRecieptRequest;
	
	@Autowired
	private StringWriter xmlRetrieveMessageRecieptResponse;
	
	@Autowired
	private StringWriter xmlRetrieveMessageRecieptResponsePayload;
	
	public StringWriter getXmlRetrievePersonsRequest() {
		return xmlRetrievePersonsRequest;
	}

	public StringWriter getXmlRetrievePersonsRequestPayload() {
		return xmlRetrievePersonsRequestPayload;
	}
	
	public StringWriter getXmlRetrievePersonsResponse() {
		return xmlRetrievePersonsResponse;
	}

	public StringWriter getXmlRetrievePersonsResponsePayload() {
		return xmlRetrievePersonsResponsePayload;
	}

	public StringWriter getXmlSendMessageRequest() {
		return xmlSendMessageRequest;
	}

	public StringWriter getXmlSendMessageRequestPayload() {
		return xmlSendMessageRequestPayload;
	}

	public StringWriter getXmlSendMessageResponse() {
		return xmlSendMessageResponse;
	}

	public StringWriter getXmlRetrieveMessageRecieptRequest() {
		return xmlRetrieveMessageRecieptRequest;
	}

	public StringWriter getXmlRetrieveMessageRecieptResponse() {
		return xmlRetrieveMessageRecieptResponse;
	}

	public StringWriter getXmlRetrieveMessageRecieptResponsePayload() {
		return xmlRetrieveMessageRecieptResponsePayload;
	}
	
}
