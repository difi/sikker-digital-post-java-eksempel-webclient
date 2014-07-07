package no.difi.sdp.webclient.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class SsnConstraint implements ConstraintValidator<Ssn, String>{

	@Override
	public void initialize(Ssn ssn) {
	}

	@Override
	public boolean isValid(String ssn, ConstraintValidatorContext constaintValidatorContext) {
		return SsnValidator.isValid(ssn);
	}

}
