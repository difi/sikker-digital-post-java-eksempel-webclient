package no.difi.sdp.webclient.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
public class FysiskPost {

    @Id
    @GeneratedValue
    private Long id;

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
    @OneToOne(cascade = CascadeType.ALL)
    private KonvoluttAdresse adressat;

    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
    private KonvoluttAdresse returadresse;

    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
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


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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


