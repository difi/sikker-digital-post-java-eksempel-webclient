package no.difi.sdp.webclient.web;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import no.difi.sdp.webclient.domain.Message;
import no.difi.sdp.webclient.service.MessageService;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MessageController {

	private static final Logger LOGGER = LoggerFactory.getLogger(MessageController.class);
	
	@Autowired
	private Environment environment;
	
	@Autowired
	private MessageService messageService;
	
	@Autowired
	private Validator validator;
	
	@InitBinder
	protected void initBinder(WebDataBinder webDataBinder) {
		webDataBinder.setValidator(validator);
		webDataBinder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/", produces = "text/html")
	public String show_send_message_page(Model model) {
		model.addAttribute("messageCommand", new MessageCommand());
		return "send_message_page";
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/messages")
	public String send_message(@Valid @ModelAttribute("messageCommand") MessageCommand messageCommand, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) throws IOException {
		if (bindingResult.hasErrors()) {
			model.addAttribute("messageCommand", messageCommand);
			model.addAttribute("errors", bindingResult);
			return "send_message_page";
		}
		Message message = new Message();
		message.setSsn(messageCommand.getSsn());
		message.setSensitiveTitle(messageCommand.getSensitiveTitle());
		message.setInsensitiveTitle(messageCommand.getInsensitiveTitle());
		message.setAttachment(messageCommand.getAttachment().getBytes());
		message.setAttachmentFilename(messageCommand.getAttachment().getOriginalFilename());
		message.setAttachmentMimetype(messageCommand.getAttachment().getContentType());
		message.setSecurityLevel(messageCommand.getSecurityLevel());
		message.setEmailNotification(messageCommand.getEmailNotification());
		message.setEmailNotificationSchedule(messageCommand.getEmailNotificationSchedule());
		message.setMobileNotification(messageCommand.getMobileNotification());
		message.setMobileNotificationSchedule(messageCommand.getMobileNotificationSchedule());
		message.setRequiresMessageOpenedReciept(messageCommand.getRequiresMessageOpenedReciept());
		message.setDelayedAvailabilityDate(messageCommand.getDelayedAvailabilityDate());
		messageService.sendMessage(message);
		return "redirect:/client/messages/" + message.getId();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/messages/{id}")
	public String show_message_page(@PathVariable Long id, Model model) throws NotFoundException {
		Message message = messageService.getMessage(id);
		if (message == null) {
			throw new NotFoundException();
		}
		model.addAttribute(message);
		return "show_message_page";
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/messages/{id}/download")
	public void download_message_attachment(@PathVariable Long id, HttpServletResponse response) throws NotFoundException, IOException {
		Message message = messageService.getMessage(id);
		if (message == null) {
			throw new NotFoundException();
		}
		if (message.getAttachment() == null) {
			throw new NotFoundException();
		}
		response.setContentType(message.getAttachmentMimetype());
		InputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(message.getAttachment()));
		IOUtils.copy(inputStream, response.getOutputStream());
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/messages/{id}/delete")
	public String delete_message(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		messageService.deleteMessage(id);
		return "redirect:/client/messages";
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "messages/delete")
	public String delete_all_messages(RedirectAttributes redirectAttributes) {
		messageService.deleteAllMessages();
		return "redirect:/client/messages";
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/messages")
	public String show_message_list_page(Model model) {
		List<Message> messages = messageService.getMessages();
		model.addAttribute("messages", messages);
		return "show_message_list_page";
	}
	
	@ModelAttribute("oppslagstjenestenUrl")
	private String oppslagstjenestenUrl() {
		return environment.getProperty("oppslagstjenesten.url");
	}
	
	@ModelAttribute("meldingsformidlerUrl")
	private String meldingsformidlerUrl() {
		return environment.getProperty("meldingsformidler.url");
	}
	
	@ModelAttribute("avsenderOrganisasjonsnummer")
	private String avsenderOrgansisasjonsnummer() {
		return environment.getProperty("meldingsformidler.avsender.organisasjonsnummer");
	}
	
	@ModelAttribute("avsenderIdentifikator")
	private String avsenderIdentifikator() {
		return environment.getProperty("meldingsformidler.avsender.identifikator");
	}
	
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(NotFoundException.class)
	public void handle_404_not_found(NotFoundException e) {
		// do nothing
	}
	
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(Exception.class)
	public void handle_500_internal_error(Exception e) {
		LOGGER.error("Unexpected error", e);
	}
	
	private class NotFoundException extends Exception {

		private static final long serialVersionUID = 1L;
	
	}
}
