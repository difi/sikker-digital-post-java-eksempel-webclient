package no.difi.sdp.webclient.web;

import no.difi.begrep.Reservasjon;
import no.difi.begrep.Status;
import no.difi.sdp.client2.domain.Prioritet;
import no.difi.sdp.webclient.validation.Document;
import no.difi.sdp.webclient.validation.Ssn;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Component
public class MessageCommand {

	public enum Type{FYSISK, DIGITAL};

	private Type type;

	@Ssn(message = "Ugyldig fødselsnummer.")
	private String ssn;
	
	@Size(min = 1, message = "Du må oppgi tittel.")
	@NotNull
	private String title;

	private FysiskPostCommand fysiskPostCommand;

	private DigitalPostCommand digitalPostCommand;

	@Document(message = "Du må oppgi hoveddokument.")
	private MultipartFile document;

	private List<MultipartFile> attachments;
	
	@NotNull
	private String senderOrgNumber;
	
	private String senderId;
	
	private String invoiceReference;
	
	@NotNull
	private String keyPairAlias;

	@NotNull
	private Prioritet priority;
	
	@NotNull
	private String languageCode;
	
	private boolean retrieveContactDetails;
	
	private Status contactRegisterStatus;
	
	private Reservasjon reservationStatus;
	
	private String postboxAddress;
	
	private String postboxVendorOrgNumber;
	
	private MultipartFile postboxCertificate;
	
	private String mobile;
	
	private String email;
	
	public String getSsn() {
		return ssn;
	}

	public void setSsn(String ssn) {
		this.ssn = ssn;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public MessageCommand() {
	}

	public MessageCommand(Type type) {
		this.type = type;
	}

	public FysiskPostCommand getFysiskPostCommand() {
		if(fysiskPostCommand == null){
			setFysiskPostCommand(new FysiskPostCommand());
		}
		return fysiskPostCommand;
	}

	public void setFysiskPostCommand(FysiskPostCommand fysiskPostCommand) {
		this.fysiskPostCommand = fysiskPostCommand;
	}

	public DigitalPostCommand getDigitalPostCommand() {
		if(digitalPostCommand == null){
			setDigitalPostCommand(new DigitalPostCommand());
		}
		return digitalPostCommand;
	}

	public void setDigitalPostCommand(DigitalPostCommand digitalPostCommand) {
		this.digitalPostCommand = digitalPostCommand;
	}

	public MultipartFile getDocument() {
		return document;
	}

	public void setDocument(MultipartFile document) {
		this.document = document;
	}
	
	public List<MultipartFile> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<MultipartFile> attachments) {
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
	
	public String getKeyPairAlias() {
		return keyPairAlias;
	}

	public void setKeyPairAlias(String keyPairAlias) {
		this.keyPairAlias = keyPairAlias;
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

	public boolean getRetrieveContactDetails() {
		return retrieveContactDetails;
	}

	public void setRetrieveContactDetails(boolean retrieveContactDetails) {
		this.retrieveContactDetails = retrieveContactDetails;
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

	public MultipartFile getPostboxCertificate() {
		return postboxCertificate;
	}

	public void setPostboxCertificate(MultipartFile postboxCertificate) {
		this.postboxCertificate = postboxCertificate;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isFysiskPost() {
		return getType() == Type.FYSISK;
	}

	public boolean isDigitalPost() {
		return getType() == Type.DIGITAL;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
}
