package no.difi.sdp.webclient.validation;

import no.difi.sdp.webclient.web.KonvoluttAdresse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validator for KonvoluttAdresse.
 */
@Component("adresseValidator")
public class AdresseValidator implements Validator {

    private String adresseType;
    protected String adressatFeilemlding = adresseType;

    @Override
    public boolean supports(Class<?> aClass) {
        return KonvoluttAdresse.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        KonvoluttAdresse adresse = (KonvoluttAdresse) o;
        final String feltNavnAdresse = "fysiskPostCommand." + adresseType;
        validerFellesFelt(adresse, errors, feltNavnAdresse);
        if (adresse.getType() == KonvoluttAdresse.Type.NORSK) {
            validerNorskAdresse(adresse, errors, feltNavnAdresse);
        } else if (adresse.getType() == KonvoluttAdresse.Type.UTENLANDSK) {
            validerUtenlandskAdresse(adresse, errors, feltNavnAdresse);
        }

    }

    private void validerUtenlandskAdresse(KonvoluttAdresse adresse, Errors errors, final String feltNavnAdresse) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, feltNavnAdresse + ".adresselinje1", feltNavnAdresse + ".adresselinje1", adressatFeilemlding + " adresselinje1 er tomt");
        if (StringUtils.isEmpty(adresse.getLand()) && StringUtils.isEmpty(adresse.getLandkode())) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, feltNavnAdresse + ".land", feltNavnAdresse + ".land", adressatFeilemlding + " land er tomt");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, feltNavnAdresse + ".landkode", feltNavnAdresse + ".landkode", adressatFeilemlding + " landkode er tomt");
        }

    }

    private void validerFellesFelt(final KonvoluttAdresse adresse, Errors errors, String feltNavnAdresse) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, feltNavnAdresse + ".navn", feltNavnAdresse + ".navn", adressatFeilemlding + " navn er tomt");
    }

    private void validerNorskAdresse(final KonvoluttAdresse adresse, Errors errors, String feltNavnAdresse) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, feltNavnAdresse + ".postnummer", feltNavnAdresse + ".postnummer", adressatFeilemlding + " postnummer er tomt");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, feltNavnAdresse + ".poststed", feltNavnAdresse + ".poststed", adressatFeilemlding + " poststed er tomt");
    }

    public void setAdresseType(String adresseType) {
        this.adresseType = adresseType;
    }
}
