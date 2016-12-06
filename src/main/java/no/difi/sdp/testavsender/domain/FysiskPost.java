package no.difi.sdp.testavsender.domain;

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
    private String tekniskMottakerSertifikatAlias;

    public FysiskPost(){

    }

    public FysiskPost(final Posttype posttype, final Utskriftsfarge utskriftsfarge, final Returhaandtering returhaandtering, final KonvoluttAdresse adressat, final KonvoluttAdresse returadresse){
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

    public String getTekniskMottakerSertifikatAlias() {
        return tekniskMottakerSertifikatAlias;
    }

    public void setTekniskMottakerSertifikatAlias(String tekniskMottakerSertifikatAlias) {
        this.tekniskMottakerSertifikatAlias = tekniskMottakerSertifikatAlias;
    }
}


