package no.difi.sdp.webclient.domain;

import no.difi.sdp.client.domain.digital_post.Sikkerhetsnivaa;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 *
 *
 */
@Entity
public class DigitalPost {


    @Id
    @GeneratedValue
    private Long id;

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

    public DigitalPost() {
    }

    public DigitalPost(String insensitiveTitle) {
        this.insensitiveTitle = insensitiveTitle;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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


}
