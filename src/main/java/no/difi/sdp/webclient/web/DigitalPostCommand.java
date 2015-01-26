package no.difi.sdp.webclient.web;

import no.difi.sdp.client.domain.digital_post.Sikkerhetsnivaa;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 *
 */
@Component
public class DigitalPostCommand {

    @Size(min = 1, message = "Du må oppgi ikke-sensitiv tittel.")
    @NotNull
    private String insensitiveTitle;

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
