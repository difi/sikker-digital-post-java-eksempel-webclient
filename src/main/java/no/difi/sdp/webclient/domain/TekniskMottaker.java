package no.difi.sdp.webclient.domain;

import javax.persistence.Embeddable;
import javax.persistence.Lob;
import java.security.cert.X509Certificate;

@Embeddable
public class TekniskMottaker {

    public TekniskMottaker() {

    }

    private String organisasjonsnummer;

    @Lob
    private X509Certificate x509Certificate;

    public TekniskMottaker(String organisasjonsnummer, X509Certificate sertifikat) {
        this.organisasjonsnummer = organisasjonsnummer;
        this.x509Certificate = sertifikat;
    }

    public String getOrganisasjonsnummer() {
        return organisasjonsnummer;
    }

    public X509Certificate getSertifikat() {
        return x509Certificate;
    }
}
