package no.difi.sdp.webclient.web;

import com.sun.istack.NotNull;
import no.difi.sdp.webclient.domain.Posttype;
import no.difi.sdp.webclient.domain.Returhaandtering;
import no.difi.sdp.webclient.domain.Utskriftsfarge;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class FysiskPostCommand {

    @NotNull
    private Posttype posttype;

    @NotNull
    private Utskriftsfarge utskriftsfarge;

    @NotNull
    private Returhaandtering returhaandtering;

    @NotNull
    private KonvoluttAdresse adressat;

    @NotNull
    private KonvoluttAdresse returadresse;

    private TekniskMottaker utskriftsleverandoer;

    public Posttype getPosttype() {
        return posttype;
    }

    public void setPosttype(Posttype posttype) {
        this.posttype = posttype;
    }

    public Utskriftsfarge getUtskriftsfarge() {
        return utskriftsfarge;
    }

    public void setUtskriftsfarge(Utskriftsfarge utskriftsfarge) {
        this.utskriftsfarge = utskriftsfarge;
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