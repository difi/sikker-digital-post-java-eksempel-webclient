package no.difi.sdp.webclient.domain;

import javax.persistence.Embeddable;

@Embeddable
public class TekniskMottaker {

    public final String organisasjonsnummer;
    public final String sertifikat;

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
