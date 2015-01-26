package no.difi.sdp.webclient.web;

import org.springframework.stereotype.Component;

/**
 *
 *
 */
@Component
public class TekniskMottaker {

    private String organisasjonsnummer;
    private String sertifikat;

    public String getOrganisasjonsnummer() {
        return organisasjonsnummer;
    }

    public void setOrganisasjonsnummer(String organisasjonsnummer) {
        this.organisasjonsnummer = organisasjonsnummer;
    }

    public String getSertifikat() {
        return sertifikat;
    }

    public void setSertifikat(String sertifikat) {
        this.sertifikat = sertifikat;
    }
}
