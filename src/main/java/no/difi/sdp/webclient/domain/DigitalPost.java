package no.difi.sdp.webclient.domain;

import no.difi.begrep.Reservasjon;
import no.difi.begrep.Status;
import no.difi.sdp.client2.domain.digital_post.Sikkerhetsnivaa;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 *
 *
 */
@Embeddable
public class DigitalPost {

    @Enumerated(EnumType.STRING)
    private Status contactRegisterStatus;

    @NotNull
    @Size(min = 1)
    private String insensitiveTitle;

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

    private boolean retrieveContactDetails;

    @Enumerated(EnumType.STRING)
    private Reservasjon reservationStatus;

    private String postboxVendorOrgNumber;

    private String postboxAddress;

    @Lob
    private byte[] postboxCertificate;

    private String mobile;

    private String email;

    public DigitalPost() {
    }

    public DigitalPost(String insensitiveTitle) {
        this.insensitiveTitle = insensitiveTitle;
    }

    public Status getContactRegisterStatus() {
        return contactRegisterStatus;
    }

    public void setContactRegisterStatus(Status contactRegisterStatus) {
        this.contactRegisterStatus = contactRegisterStatus;
    }

    public String getInsensitiveTitle() {
        return insensitiveTitle;
    }

    public void setInsensitiveTitle(String insensitiveTitle) {
        this.insensitiveTitle = insensitiveTitle;
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

    public boolean getRetrieveContactDetails() {
        return retrieveContactDetails;
    }

    public void setRetrieveContactDetails(boolean retrieveContactDetails) {
        this.retrieveContactDetails = retrieveContactDetails;
    }

    public Reservasjon getReservationStatus() {
        return reservationStatus;
    }

    public void setReservationStatus(Reservasjon reservationStatus) {
        this.reservationStatus = reservationStatus;
    }

    public String getPostboxVendorOrgNumber() {
        return postboxVendorOrgNumber;
    }

    public void setPostboxVendorOrgNumber(String postboxVendorOrgNumber) {
        this.postboxVendorOrgNumber = postboxVendorOrgNumber;
    }

    public String getPostboxAddress() {
        return postboxAddress;
    }

    public void setPostboxAddress(String postboxAddress) {
        this.postboxAddress = postboxAddress;
    }

    public byte[] getPostboxCertificate() {
        return postboxCertificate;
    }

    public void setPostboxCertificate(byte[] postboxCertificate) {
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
}
