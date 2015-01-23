package no.difi.sdp.webclient.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 *
 *
 */
@Entity
public class FysiskPost {

    @Id
    @GeneratedValue
    private Long id;

    private boolean posttype;

    /*
    KonvoluttAdresse adressat: adresse,postnr,land etc
    Posttype: A-/B-prioritet
    Utskriftsfarge: farge, svart/kvitt
    Returhaandtering: direkte/makuler
    KonvoluttAdresse returadresse: adresse,postnr,land etc
    TekniskMottaker utskriftsleverandoer: orgnr+sertifikat
    */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}


