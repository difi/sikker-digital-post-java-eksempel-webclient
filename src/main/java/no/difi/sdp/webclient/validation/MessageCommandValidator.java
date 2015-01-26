package no.difi.sdp.webclient.validation;

import no.difi.sdp.webclient.web.MessageCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 *
 * Validator/klasse for MessageCommand.
 */
@Component("messageCommandValidator")
public class MessageCommandValidator implements Validator {

    @Autowired
    private Validator basicValidator;

    @Override
    public boolean supports(Class<?> aClass) {
        return MessageCommand.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {

        MessageCommand message = (MessageCommand) o;
        if (message.isFysiskPost()) {
            basicValidator.validate(message.getFysiskPostCommand(), errors);

        } else if (message.isDigitalPost()) {
            basicValidator.validate(message.getDigitalPostCommand(), errors);
            //ValidationUtils.rejectIfEmptyOrWhitespace(errors, "id", "id.required");
            //errors.rejectValue("id", "negativeValue", new Object[]{"'id'"}, "id can't be negative");
            // TODO: randi implementer logikk for adresse etc.
        }

    }
}
