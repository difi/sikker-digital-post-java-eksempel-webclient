package no.difi.sdp.webclient.domain;

import java.util.Date;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import no.difi.begrep.Reservasjon;
import no.difi.begrep.Status;
import no.difi.sdp.client.domain.Prioritet;
import no.difi.sdp.client.domain.digital_post.Sikkerhetsnivaa;
import no.difi.sdp.webclient.validation.Ssn;

@Entity
public class Message {

	@Id
	@GeneratedValue
	private Long id;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date date;
	
	@Ssn
	private String ssn;
	
	@NotNull
	@Size(min = 1)
	private String insensitiveTitle;
	
	@OneToOne(cascade = CascadeType.ALL)
	private Document document;
	
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "message") 
	private Set<Document> attachments;
	
	@NotNull
	private String senderOrgNumber;
	
	private String senderId;
	
	private String invoiceReference;
	
	@NotNull
	private String technicalOrgNumber;
	
	@NotNull
	private String technicalAlias;
	
	@NotNull
	private Sikkerhetsnivaa securityLevel;
	
	@Lob
	private String emailNotification;
	
	private String emailNotificationSchedule;
	
	@Lob
	private String mobileNotification;
	
	private String mobileNotificationSchedule;
	
	private boolean requiresMessageOpenedReceipt;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date delayedAvailabilityDate;
	
	@Enumerated(EnumType.STRING)
	@NotNull
	private Prioritet priority;
	
	@NotNull
	private String languageCode;
	
	@Enumerated(EnumType.STRING)
	private Status contactRegisterStatus;
	
	@Enumerated(EnumType.STRING)
	private Reservasjon reservationStatus;
	
	private String email;
	
	private String mobile;
	
	@Lob
	private byte[] postboxCertificate;
	
	private String postboxAddress;
	
	private String postboxVendorOrgNumber;
	
	private String conversationId;
	
	@Lob
	private String xmlRetrievePersonsRequest;
	
	@Lob
	private String xmlRetrievePersonsRequestPayload;
	
	@Lob
	private String xmlRetrievePersonsResponse;
	
	@Lob
	private String xmlRetrievePersonsResponsePayload;
	
	@Lob
	private String xmlSendMessageRequest;
	
	@Lob
	private String xmlSendMessageRequestPayload;
	
	@Lob
	private String xmlSendMessageResponse;
	
	private MessageStatus status;
	
	@Lob
	private String exception;
	
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "message")
	private Set<Receipt> receipts;
	
	@Lob
	@Basic(fetch = FetchType.LAZY)
	private byte[] asic;
	
	private boolean retrieveContactDetails;
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
	public String getSsn() {
		return ssn;
	}
	
	public void setSsn(String ssn) {
        this.ssn = ssn;
    }
	
	public String getInsensitiveTitle() {
		return insensitiveTitle;
	}
	
	public void setInsensitiveTitle(String insensitiveTitle) {
        this.insensitiveTitle = insensitiveTitle;
    }
	
	public Document getDocument() {
		return document;
	}
	
	public void setDocument(Document document) {
        this.document = document;
    }
	
	public Set<Document> getAttachments() {
		return attachments;
	}
	
	public void setAttachments(Set<Document> attachments) {
		this.attachments = attachments;
	}
	
	public String getSenderOrgNumber() {
		return senderOrgNumber;
	}

	public void setSenderOrgNumber(String senderOrgNumber) {
		this.senderOrgNumber = senderOrgNumber;
	}

	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}
	
	public String getInvoiceReference() {
		return invoiceReference;
	}

	public void setInvoiceReference(String invoiceReference) {
		this.invoiceReference = invoiceReference;
	}
	
	public String getTechnicalOrgNumber() {
		return technicalOrgNumber;
	}

	public void setTechnicalOrgNumber(String technicalOrgNumber) {
		this.technicalOrgNumber = technicalOrgNumber;
	}

	public String getTechnicalAlias() {
		return technicalAlias;
	}

	public void setTechnicalAlias(String technicalAlias) {
		this.technicalAlias = technicalAlias;
	}
	
	public Sikkerhetsnivaa getSecurityLevel() {
		return securityLevel;
	}

	public void setSecurityLevel(Sikkerhetsnivaa securityLevel) {
		this.securityLevel = securityLevel;
	}

	public String getEmailNotification() {
		return emailNotification;
	}

	public void setEmailNotification(String emailNotification) {
		this.emailNotification = emailNotification;
	}

	public String getEmailNotificationSchedule() {
		return emailNotificationSchedule;
	}

	public void setEmailNotificationSchedule(String emailNotificationSchedule) {
		this.emailNotificationSchedule = emailNotificationSchedule;
	}

	public String getMobileNotification() {
		return mobileNotification;
	}
	
	public void setMobileNotification(String mobileNotification) {
		this.mobileNotification = mobileNotification;
	}
	
	public String getMobileNotificationSchedule() {
		return mobileNotificationSchedule;
	}

	public void setMobileNotificationSchedule(String mobileNotificationSchedule) {
		this.mobileNotificationSchedule = mobileNotificationSchedule;
	}
	
	public boolean getRequiresMessageOpenedReceipt() {
		return requiresMessageOpenedReceipt;
	}
	
	public void setRequiresMessageOpenedReceipt(boolean requiresMessageOpenedReceipt) {
		this.requiresMessageOpenedReceipt = requiresMessageOpenedReceipt;
	}
	
	public Date getDelayedAvailabilityDate() {
		return delayedAvailabilityDate;
	}

	public void setDelayedAvailabilityDate(Date delayedAvailabilityDate) {
		this.delayedAvailabilityDate = delayedAvailabilityDate;
	}

	public Prioritet getPriority() {
		return priority;
	}

	public void setPriority(Prioritet priority) {
		this.priority = priority;
	}
	
	public String getLanguageCode() {
		return languageCode;
	}

	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}
	
	public Status getContactRegisterStatus() {
		return contactRegisterStatus;
	}

	public void setContactRegisterStatus(Status contactRegisterStatus) {
		this.contactRegisterStatus = contactRegisterStatus;
	}

	public Reservasjon getReservationStatus() {
		return reservationStatus;
	}

	public void setReservationStatus(Reservasjon reservationStatus) {
		this.reservationStatus = reservationStatus;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMobile() {
		return mobile;
	}
	
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	
	public byte[] getPostboxCertificate() {
		return postboxCertificate;
	}

	public void setPostboxCertificate(byte[] postboxCertificate) {
		this.postboxCertificate = postboxCertificate;
	}

	public String getPostboxAddress() {
		return postboxAddress;
	}

	public void setPostboxAddress(String postboxAddress) {
		this.postboxAddress = postboxAddress;
	}

	public String getPostboxVendorOrgNumber() {
		return postboxVendorOrgNumber;
	}

	public void setPostboxVendorOrgNumber(String postboxVendorOrgNumber) {
		this.postboxVendorOrgNumber = postboxVendorOrgNumber;
	}

	public String getConversationId() {
		return conversationId;
	}

	public void setConversationId(String conversationId) {
		this.conversationId = conversationId;
	}
	
	public String getXmlRetrievePersonsRequest() {
		return xmlRetrievePersonsRequest;
	}

	public void setXmlRetrievePersonsRequest(String xmlRetrievePersonsRequest) {
		this.xmlRetrievePersonsRequest = xmlRetrievePersonsRequest;
	}

	public String getXmlRetrievePersonsRequestPayload() {
		return xmlRetrievePersonsRequestPayload;
	}

	public void setXmlRetrievePersonsRequestPayload(String xmlRetrievePersonsRequestPayload) {
		this.xmlRetrievePersonsRequestPayload = xmlRetrievePersonsRequestPayload;
	}

	public String getXmlRetrievePersonsResponse() {
		return xmlRetrievePersonsResponse;
	}

	public void setXmlRetrievePersonsResponse(String xmlRetrievePersonsResponse) {
		this.xmlRetrievePersonsResponse = xmlRetrievePersonsResponse;
	}

	public String getXmlRetrievePersonsResponsePayload() {
		return xmlRetrievePersonsResponsePayload;
	}

	public void setXmlRetrievePersonsResponsePayload(String xmlRetrievePersonsResponsePayload) {
		this.xmlRetrievePersonsResponsePayload = xmlRetrievePersonsResponsePayload;
	}

	public String getXmlSendMessageRequest() {
		return xmlSendMessageRequest;
	}

	public void setXmlSendMessageRequest(String xmlSendMessageRequest) {
		this.xmlSendMessageRequest = xmlSendMessageRequest;
	}

	public String getXmlSendMessageRequestPayload() {
		return xmlSendMessageRequestPayload;
	}

	public void setXmlSendMessageRequestPayload(String xmlSendMessageRequestPayload) {
		this.xmlSendMessageRequestPayload = xmlSendMessageRequestPayload;
	}

	public String getXmlSendMessageResponse() {
		return xmlSendMessageResponse;
	}

	public void setXmlSendMessageResponse(String xmlSendMessageResponse) {
		this.xmlSendMessageResponse = xmlSendMessageResponse;
	}

	public MessageStatus getStatus() {
		return status;
	}

	public void setStatus(MessageStatus status) {
		this.status = status;
	}
	
	public String getException() {
		return exception;
	}

	public void setException(String exception) {
		this.exception = exception;
	}
	
	public Set<Receipt> getReceipts() {
		return receipts;
	}
	
	public void setReceipts(Set<Receipt> receipts) {
		this.receipts = receipts;
	}

	public byte[] getAsic() {
		return asic;
	}

	public void setAsic(byte[] asice) {
		this.asic = asice;
	}

	public boolean getRetrieveContactDetails() {
		return retrieveContactDetails;
	}

	public void setRetrieveContactDetails(boolean retrieveContactDetails) {
		this.retrieveContactDetails = retrieveContactDetails;
	}

	
}
