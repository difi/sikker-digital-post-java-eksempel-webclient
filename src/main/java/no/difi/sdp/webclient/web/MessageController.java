package no.difi.sdp.webclient.web;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import no.difi.sdp.webclient.domain.Document;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartResolver;
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
	
	@Autowired
	private MultipartResolver multipartResolver;
	
	@InitBinder
	protected void initBinder(WebDataBinder webDataBinder) {
		webDataBinder.setValidator(validator);
		webDataBinder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/", produces = "text/html")
	public String show_send_message_page(Model model, @RequestParam(required = false) Long copy) {
		MessageCommand messageCommand = new MessageCommand();
		if (copy != null) {
			Message message = messageService.getMessage(copy);
			if (message != null) {
				// Default file upload values are not allowed by web browsers so we can't copy document and attachments
				messageCommand.setDelayedAvailabilityDate(message.getDelayedAvailabilityDate());
				messageCommand.setEmailNotification(message.getEmailNotification());
				messageCommand.setEmailNotificationSchedule(message.getEmailNotificationSchedule());
				messageCommand.setInsensitiveTitle(message.getInsensitiveTitle());
				messageCommand.setInvoiceReference(message.getInvoiceReference());
				messageCommand.setLanguageCode(message.getLanguageCode());
				messageCommand.setMobileNotification(message.getMobileNotification());
				messageCommand.setMobileNotificationSchedule(message.getMobileNotificationSchedule());
				messageCommand.setPriority(message.getPriority());
				messageCommand.setRequiresMessageOpenedReceipt(message.getRequiresMessageOpenedReceipt());
				messageCommand.setSecurityLevel(message.getSecurityLevel());
				messageCommand.setSenderId(message.getSenderId());
				messageCommand.setSsn(message.getSsn());
				messageCommand.setTitle(message.getDocument().getTitle());
			}
		}
		model.addAttribute("messageCommand", messageCommand);
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
		message.setInsensitiveTitle(messageCommand.getInsensitiveTitle());
		Document document = new Document();
		document.setTitle(messageCommand.getTitle());
		document.setContent(messageCommand.getDocument().getBytes());
		document.setFilename(messageCommand.getDocument().getOriginalFilename());
		document.setMimetype(messageCommand.getDocument().getContentType());
		message.setDocument(document);
		Set<Document> attachments = new HashSet<Document>();
		for (MultipartFile multipartFile : messageCommand.getAttachments()) {
			if (! multipartFile.isEmpty()) {
				Document attachment = new Document();
				attachment.setTitle(multipartFile.getOriginalFilename()); // Uses attachment filename for attachment title
				attachment.setContent(multipartFile.getBytes());
				attachment.setFilename(multipartFile.getOriginalFilename());
				attachment.setMimetype(multipartFile.getContentType());
				attachment.setMessage(message);
				attachments.add(attachment);
			}
		}
		message.setAttachments(attachments);
		message.setSenderId(messageCommand.getSenderId());
		message.setInvoiceReference(messageCommand.getInvoiceReference());
		message.setSecurityLevel(messageCommand.getSecurityLevel());
		message.setEmailNotification(messageCommand.getEmailNotification());
		message.setEmailNotificationSchedule(messageCommand.getEmailNotificationSchedule());
		message.setMobileNotification(messageCommand.getMobileNotification());
		message.setMobileNotificationSchedule(messageCommand.getMobileNotificationSchedule());
		message.setRequiresMessageOpenedReceipt(messageCommand.getRequiresMessageOpenedReceipt());
		message.setDelayedAvailabilityDate(messageCommand.getDelayedAvailabilityDate());
		message.setPriority(messageCommand.getPriority());
		message.setLanguageCode(messageCommand.getLanguageCode());
		messageService.sendMessage(message);
		return "redirect:/client/messages/" + message.getId();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/messages/{id}")
	public String show_message_page(@PathVariable Long id, Model model) throws NotFoundException {
		Message message = messageService.getMessage(id);
		if (message == null) {
			throw new NotFoundException();
		}
		model.addAttribute("message", message);
		return "show_message_page";
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/messages/documents/{id}")
	public void download_message_document(@PathVariable Long id, HttpServletResponse response) throws NotFoundException, IOException {
		Document document = messageService.getDocument(id);
		if (document == null) {
			throw new NotFoundException();
		}
		response.addHeader("Content-Disposition", "attachment; filename=\"" + document.getFilename() + "\"");
		response.setContentType(document.getMimetype());
		InputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(document.getContent()));
		IOUtils.copy(inputStream, response.getOutputStream());
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/messages/{id}/asic")
	public void download_message_asic(@PathVariable Long id, HttpServletResponse response) throws NotFoundException, IOException {
		Message message = messageService.getMessage(id);
		if (message == null || message.getAsic() == null) {
			throw new NotFoundException();
		}
		response.addHeader("Content-Disposition", "attachment; filename=\"message-" + message.getId() + "-asic.zip\"");
		response.setContentType("application/zip");
		InputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(message.getAsic()));
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
