package no.difi.sdp.webclient.web;

import com.sun.istack.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *
 */
@Component
public class KonvoluttAdresse {

    public enum Type {NORSK, UTENLANDSK}

    private Type type;

    @NotNull
    private String navn;

    private List<String> adresselinjer;

    private String postnummer;
    private String poststed;

    private String landkode;
    private String land;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getNavn() {
        return navn;
    }

    public void setNavn(String navn) {
        this.navn = navn;
    }

    public List<String> getAdresselinjer() {
        return adresselinjer;
    }

    public void setAdresselinjer(List<String> adresselinjer) {
        this.adresselinjer = adresselinjer;
    }

    public String getPostnummer() {
        return postnummer;
    }

    public void setPostnummer(String postnummer) {
        this.postnummer = postnummer;
    }

    public String getPoststed() {
        return poststed;
    }

    public void setPoststed(String poststed) {
        this.poststed = poststed;
    }

    public String getLandkode() {
        return landkode;
    }

    public void setLandkode(String landkode) {
        this.landkode = landkode;
    }

    public String getLand() {
        return land;
    }

    public void setLand(String land) {
        this.land = land;
    }
}
