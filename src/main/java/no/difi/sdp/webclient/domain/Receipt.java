package no.difi.sdp.webclient.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import no.difi.sdp.client.domain.kvittering.Feil.Feiltype;
import no.difi.sdp.client.domain.kvittering.VarslingFeiletKvittering.Varslingskanal;

@Entity
public class Receipt {

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	private Message message;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date date;
	
	private String type;
	
	@Enumerated(EnumType.STRING)
	private Feiltype errorType;
	
	private String errorDetails;
	
	@Enumerated(EnumType.STRING)
	private Varslingskanal notificationErrorChannel;
	
	private String notificationErrorDescription;
	
	@Lob
	private String xmlRequest;
	
	@Lob
	private String xmlResponse;
	
	@Lob
	private String xmlResponsePayload;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Message getMessage() {
		return message;
	}
	
	public void setMessage(Message message) {
		this.message = message;
	}
	
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getXmlRequest() {
		return xmlRequest;
	}

	public void setXmlRequest(String xmlRequest) {
		this.xmlRequest = xmlRequest;
	}

	public String getXmlResponse() {
		return xmlResponse;
	}

	public void setXmlResponse(String xmlResponse) {
		this.xmlResponse = xmlResponse;
	}

	public String getXmlResponsePayload() {
		return xmlResponsePayload;
	}

	public void setXmlResponsePayload(String xmlResponsePayload) {
		this.xmlResponsePayload = xmlResponsePayload;
	}

	public Feiltype getErrorType() {
		return errorType;
	}

	public void setErrorType(Feiltype errorType) {
		this.errorType = errorType;
	}

	public String getErrorDetails() {
		return errorDetails;
	}

	public void setErrorDetails(String errorDetails) {
		this.errorDetails = errorDetails;
	}

	public Varslingskanal getNotificationErrorChannel() {
		return notificationErrorChannel;
	}

	public void setNotificationErrorChannel(Varslingskanal notificationErrorChannel) {
		this.notificationErrorChannel = notificationErrorChannel;
	}

	public String getNotificationErrorDescription() {
		return notificationErrorDescription;
	}

	public void setNotificationErrorDescription(String notificationErrorDescription) {
		this.notificationErrorDescription = notificationErrorDescription;
	}

}
