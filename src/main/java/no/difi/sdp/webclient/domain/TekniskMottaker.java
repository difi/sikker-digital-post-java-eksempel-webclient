package no.difi.sdp.webclient.domain;

import javax.persistence.Embeddable;
import javax.persistence.Lob;
import java.security.cert.X509Certificate;

@Embeddable
public class TekniskMottaker {

    private String organisasjonsnummer;

    @Lob
    private X509Certificate x509Certificate;

    private String certificateAlias;

    public TekniskMottaker(String organisasjonsnummer, X509Certificate sertifikat, String certificateAlias) {
        this.organisasjonsnummer = organisasjonsnummer;
        this.x509Certificate = sertifikat;
        this.certificateAlias = certificateAlias;
    }

    public String getOrganisasjonsnummer() {
        return organisasjonsnummer;
    }

    public X509Certificate getSertifikat() {
        return x509Certificate;
    }

    public String getCertificateAlias() {
        return certificateAlias;
    }

    public void setCertificateAlias(String certificateAlias) {
        this.certificateAlias = certificateAlias;
    }
}
