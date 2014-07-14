package no.difi.sdp.webclient.validation;

import java.io.IOException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

public class DocumentConstraint implements ConstraintValidator<Document, MultipartFile>{

	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentConstraint.class);
	
	@Override
	public void initialize(Document attachment) {
	}

	@Override
	public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext constraintValidatorContext) {
		if (multipartFile.getOriginalFilename() == null || multipartFile.getOriginalFilename().length() == 0) {
			return false;
		}
		if (multipartFile.getContentType() == null || multipartFile.getContentType().length() == 0) {
			return false;
		}
		try {
			if (multipartFile.getBytes() == null || multipartFile.getBytes().length == 0) {
				return false;
			}
		} catch (IOException e) {
			LOGGER.error("Failed when validating multipartfile", e);
			return false;
		}
		return true;
	}

}
