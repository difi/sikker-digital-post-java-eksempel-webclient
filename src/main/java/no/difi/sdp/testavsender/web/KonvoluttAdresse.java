package no.difi.sdp.testavsender.web;

import com.sun.istack.NotNull;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class KonvoluttAdresse {

    public enum Type {NORSK, UTENLANDSK}

    private Type type;

    @NotNull
    private String navn;

    private String adresselinje1;
    private String adresselinje2;
    private String adresselinje3;
    private String adresselinje4;

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

    public String getAdresselinje1() {
        return adresselinje1;
    }

    public void setAdresselinje1(String adresselinje1) {
        this.adresselinje1 = adresselinje1;
    }

    public String getAdresselinje2() {
        return adresselinje2;
    }

    public void setAdresselinje2(String adresselinje2) {
        this.adresselinje2 = adresselinje2;
    }

    public String getAdresselinje3() {
        return adresselinje3;
    }

    public void setAdresselinje3(String adresselinje3) {
        this.adresselinje3 = adresselinje3;
    }

    public String getAdresselinje4() {
        return adresselinje4;
    }

    public void setAdresselinje4(String adresselinje4) {
        this.adresselinje4 = adresselinje4;
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
