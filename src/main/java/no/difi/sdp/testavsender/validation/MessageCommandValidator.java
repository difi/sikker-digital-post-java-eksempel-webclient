package no.difi.sdp.testavsender.validation;

import no.difi.sdp.testavsender.web.MessageCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 *
 * Validator/klasse for MessageCommand.
 */
@Component("messageCommandValidator")
public class MessageCommandValidator implements Validator {

    @Autowired
    @Qualifier("mvcValidator")
    private Validator basicValidator;

    @Autowired
    @Qualifier("fysiskPostCommandValidator")
    private FysiskPostCommandValidator fysiskPostValidator;

    @Override
    public boolean supports(Class<?> aClass) {
        return MessageCommand.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {

        MessageCommand message = (MessageCommand) o;
        if (message.isFysiskPost()) {
            fysiskPostValidator.validate(message.getFysiskPostCommand(), errors);

        } else if (message.isDigitalPost()) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "ssn", "ssn", "SSN er tomt");
            basicValidator.validate(message.getDigitalPostCommand(), errors);
        }

    }
}
