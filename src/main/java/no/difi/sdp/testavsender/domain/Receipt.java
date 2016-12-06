package no.difi.sdp.testavsender.domain;

import no.difi.sdp.client2.domain.kvittering.Feil;
import no.difi.sdp.client2.domain.kvittering.VarslingFeiletKvittering;

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


@Entity
public class Receipt {

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	private Message message;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date date;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date requestSentDate;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date responseReceivedDate;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date completedDate;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date ackRequestSentDate;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date ackResponseReceivedDate;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date postboxDate;
	
	private String type;
	
	@Enumerated(EnumType.STRING)
	private Feil.Feiltype errorType;
	
	@Lob
	private String errorDetails;
	
	@Enumerated(EnumType.STRING)
	private VarslingFeiletKvittering.Varslingskanal notificationErrorChannel;
	
	@Lob
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
	
	public Date getRequestSentDate() {
		return requestSentDate;
	}

	public void setRequestSentDate(Date requestSentDate) {
		this.requestSentDate = requestSentDate;
	}

	public Date getResponseReceivedDate() {
		return responseReceivedDate;
	}

	public void setResponseReceivedDate(Date responseReceivedDate) {
		this.responseReceivedDate = responseReceivedDate;
	}

	public Date getCompletedDate() {
		return completedDate;
	}

	public void setCompletedDate(Date completedDate) {
		this.completedDate = completedDate;
	}
	
	public Date getAckRequestSentDate() {
		return ackRequestSentDate;
	}

	public void setAckRequestSentDate(Date ackRequestSentDate) {
		this.ackRequestSentDate = ackRequestSentDate;
	}

	public Date getAckResponseReceivedDate() {
		return ackResponseReceivedDate;
	}

	public void setAckResponseReceivedDate(Date ackResponseReceivedDate) {
		this.ackResponseReceivedDate = ackResponseReceivedDate;
	}
	
	public Date getPostboxDate() {
		return postboxDate;
	}

	public void setPostboxDate(Date postboxDate) {
		this.postboxDate = postboxDate;
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

	public Feil.Feiltype getErrorType() {
		return errorType;
	}

	public void setErrorType(Feil.Feiltype errorType) {
		this.errorType = errorType;
	}

	public String getErrorDetails() {
		return errorDetails;
	}

	public void setErrorDetails(String errorDetails) {
		this.errorDetails = errorDetails;
	}

	public VarslingFeiletKvittering.Varslingskanal getNotificationErrorChannel() {
		return notificationErrorChannel;
	}

	public void setNotificationErrorChannel(VarslingFeiletKvittering.Varslingskanal notificationErrorChannel) {
		this.notificationErrorChannel = notificationErrorChannel;
	}

	public String getNotificationErrorDescription() {
		return notificationErrorDescription;
	}

	public void setNotificationErrorDescription(String notificationErrorDescription) {
		this.notificationErrorDescription = notificationErrorDescription;
	}

}
