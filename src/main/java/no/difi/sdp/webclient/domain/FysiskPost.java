package no.difi.sdp.webclient.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Embeddable
public class FysiskPost {

    @NotNull
    @Enumerated(EnumType.STRING)
    private Posttype posttype;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Utskriftsfarge utskriftsfarge;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Returhaandtering returhaandtering;

    @NotNull
    @Embedded
    private KonvoluttAdresse adressat;

    @NotNull
    @Embedded
    @AttributeOverrides( {
            @AttributeOverride(name="type", column = @Column(name="returType") ),
            @AttributeOverride(name="navn", column = @Column(name="returNavn") ),
            @AttributeOverride(name="adresselinjer", column = @Column(name="returAdresselinjer") ),
            @AttributeOverride(name="postnummer", column = @Column(name="returPostnummer") ),
            @AttributeOverride(name="poststed", column = @Column(name="returPoststed") ),
            @AttributeOverride(name="landkode", column = @Column(name="returLandkode") ),
            @AttributeOverride(name="land", column = @Column(name="returLand") )
    } )
    private KonvoluttAdresse returadresse;

    @NotNull
    @Embedded
    private TekniskMottaker utskriftsleverandoer;

    public void FysiskPost(){

    }

    public void FysiskPost(Posttype posttype, Utskriftsfarge utskriftsfarge, Returhaandtering returhaandtering, KonvoluttAdresse adressat, KonvoluttAdresse returadresse){
        this.posttype = posttype;
        this.utskriftsfarge = utskriftsfarge;
        this.returhaandtering = returhaandtering;
        this.adressat = adressat;
        this.returadresse = returadresse;
    }

    public Utskriftsfarge getUtskriftsfarge() {
        return utskriftsfarge;
    }

    public void setUtskriftsfarge(Utskriftsfarge utskriftsfarge) {
        this.utskriftsfarge = utskriftsfarge;
    }

    public Posttype getPosttype() {
        return posttype;
    }

    public void setPosttype(Posttype posttype) {
        this.posttype = posttype;
    }

    public Returhaandtering getReturhaandtering() {
        return returhaandtering;
    }

    public void setReturhaandtering(Returhaandtering returhaandtering) {
        this.returhaandtering = returhaandtering;
    }

    public KonvoluttAdresse getAdressat() {
        return adressat;
    }

    public void setAdressat(KonvoluttAdresse adressat) {
        this.adressat = adressat;
    }

    public KonvoluttAdresse getReturadresse() {
        return returadresse;
    }

    public void setReturadresse(KonvoluttAdresse returadresse) {
        this.returadresse = returadresse;
    }

    public TekniskMottaker getUtskriftsleverandoer() {
        return utskriftsleverandoer;
    }

    public void setUtskriftsleverandoer(TekniskMottaker utskriftsleverandoer) {
        this.utskriftsleverandoer = utskriftsleverandoer;
    }
}

