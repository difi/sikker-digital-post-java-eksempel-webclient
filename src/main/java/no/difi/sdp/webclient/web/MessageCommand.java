package no.difi.sdp.webclient.web;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import no.difi.sdp.client.domain.Prioritet;
import no.difi.sdp.client.domain.digital_post.Sikkerhetsnivaa;
import no.difi.sdp.webclient.validation.Document;
import no.difi.sdp.webclient.validation.Ssn;

public class MessageCommand {

	@Ssn(message = "Ugyldig fødselsnummer.")
	private String ssn;
	
	@Size(min = 1, message = "Du må oppgi tittel.")
	private String title;

	@Size(min = 1, message = "Du må oppgi ikke-sensitiv tittel.")
	private String insensitiveTitle;

	@Document(message = "Du må oppgi hoveddokument.")
	private MultipartFile document;

	private List<MultipartFile> attachments;
	
	private String senderId;
	
	private String invoiceReference;
	
	@NotNull
	private Sikkerhetsnivaa securityLevel;
	
	private String emailNotification;
	
	@Pattern(regexp = "^[0-9\\s,]*$", message = "Ugyldig verdi.")
	private String emailNotificationSchedule;
	
	private String mobileNotification;
	
	@Pattern(regexp = "^[0-9\\s,]*$", message = "Ugyldig verdi.")
	private String mobileNotificationSchedule;
	
	private boolean requiresMessageOpenedReceipt;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date delayedAvailabilityDate;
	
	private Prioritet priority;
	
	private String languageCode = "NO";
	
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

	public String getInsensitiveTitle() {
		return insensitiveTitle;
	}
	
	public void setInsensitiveTitle(String insensitiveTitle) {
		this.insensitiveTitle = insensitiveTitle;
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

}
