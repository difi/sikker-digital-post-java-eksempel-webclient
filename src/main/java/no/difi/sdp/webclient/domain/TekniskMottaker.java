package no.difi.sdp.webclient.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class TekniskMottaker {

    @Id
    @GeneratedValue
    private Long id;

    public final String organisasjonsnummer;
    public final String sertifikat;

    public TekniskMottaker(String organisasjonsnummer, String sertifikat) {
        this.organisasjonsnummer = organisasjonsnummer;
        this.sertifikat = sertifikat;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrganisasjonsnummer() {
        return organisasjonsnummer;
    }

    public String getSertifikat() {
        return sertifikat;
    }
}
