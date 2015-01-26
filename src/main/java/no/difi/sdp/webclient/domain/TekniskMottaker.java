package no.difi.sdp.webclient.domain;

import javax.persistence.Embeddable;

@Embeddable
public class TekniskMottaker {

    private String organisasjonsnummer;
    private String sertifikat;

    public TekniskMottaker(String organisasjonsnummer, String sertifikat) {
        this.organisasjonsnummer = organisasjonsnummer;
        this.sertifikat = sertifikat;
    }

    public String getOrganisasjonsnummer() {
        return organisasjonsnummer;
    }

    public String getSertifikat() {
        return sertifikat;
    }
}
