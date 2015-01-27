package no.difi.sdp.webclient.validation;

import no.difi.sdp.webclient.web.FysiskPostCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 *
 *
 */
@Component("fysiskPostCommandValidator")
public class FysiskPostCommandValidator implements Validator {

    @Autowired
    @Qualifier("mvcValidator")
    private Validator basicValidator;

    @Autowired
    @Qualifier("adresseValidator")
    private AdresseValidator adresseValidator;

    @Override
    public boolean supports(Class<?> aClass) {
        return FysiskPostCommand.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        FysiskPostCommand fysiskPostCommand = (FysiskPostCommand)o;

        adresseValidator.validate(fysiskPostCommand.getAdressat(), errors);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "posttype", "posttype.required");
        //basicValidator.validate(fysiskPostCommand.getPosttype(), errors);
        adresseValidator.validate(fysiskPostCommand.getReturadresse(), errors);
        basicValidator.validate(fysiskPostCommand.getReturhaandtering(), errors);
        basicValidator.validate(fysiskPostCommand.getUtskriftsfarge(), errors);
        basicValidator.validate(fysiskPostCommand.getUtskriftsleverandoer(), errors);
    }
}
