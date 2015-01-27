package no.difi.sdp.webclient.validation;

import no.difi.sdp.webclient.web.KonvoluttAdresse;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 *
 * Validator for KonvoluttAdresse.
 */
@Component("adresseValidator")
public class AdresseValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return KonvoluttAdresse.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        KonvoluttAdresse adresse = (KonvoluttAdresse)o;
        if(adresse.getType()== KonvoluttAdresse.Type.NORSK){
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "navn", "navn.required");
        }else if(adresse.getType()== KonvoluttAdresse.Type.UTENLANDSK){
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "navn", "navn.required");
        }

    }
}
