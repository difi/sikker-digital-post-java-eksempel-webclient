package no.difi.sdp.webclient.web;

import no.difi.begrep.Reservasjon;
import no.difi.begrep.Status;
import no.difi.sdp.client.domain.Prioritet;
import no.difi.sdp.client.domain.digital_post.Sikkerhetsnivaa;
import no.difi.sdp.webclient.domain.*;
import no.difi.sdp.webclient.service.MessageService;
import no.difi.sdp.webclient.service.PostklientService;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

@Controller
public class MessageController {

	private static final Logger LOGGER = LoggerFactory.getLogger(MessageController.class);

	private static final String FORM_ATTACHMENT_NAME_PREFIX = "attachmentName";
	private static final String FORM_ATTACHMENT_TITLE_PREFIX = "attachmentTitle";

	@Autowired
	private Environment environment;

	@Autowired
	private MessageService messageService;

	@Autowired
	@Qualifier("messageCommandValidator")
	private Validator messageValidator;

	@Autowired
	@Qualifier("mvcValidator")
	private Validator validator;

	@Autowired
	private MultipartResolver multipartResolver;

	@Autowired
	private PostklientService postklientService;

	@InitBinder
	protected void initBinder(WebDataBinder webDataBinder) {
		webDataBinder.setValidator(validator);
		webDataBinder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/", produces = "text/html")
	public String show_send_message_page(Model model, @RequestParam(required = false) Long copy) throws NotFoundException {
		MessageCommand messageCommand = new MessageCommand(MessageCommand.Type.DIGITAL);
		if (copy == null) {
			// Sets default values from configuration
			messageCommand.setLanguageCode("NO");
			messageCommand.setRetrieveContactDetails(true);
			messageCommand.setSenderOrgNumber(environment.getProperty("sdp.behandlingsansvarlig.organisasjonsnummer"));
			messageCommand.setKeyPairAlias(environment.getProperty("sdp.databehandler.keypair.alias"));
			messageCommand.setContactRegisterStatus(Status.AKTIV);
			messageCommand.setReservationStatus(Reservasjon.NEI);
		} else {
			Message message = messageService.getMessage(copy);
			if (message == null) {
				throw new NotFoundException();
			} else {
				// Sets default values from exisiting message
				// Note that default file upload values are not allowed by web browsers so we can't copy document, attachments and postbox certificate
				messageCommand.setKeyPairAlias(message.getKeyPairAlias());
				messageCommand.setInvoiceReference(message.getInvoiceReference());
				messageCommand.setLanguageCode(message.getLanguageCode());
				messageCommand.setPriority(message.getPriority());
				messageCommand.setRetrieveContactDetails(message.getDigitalPost().getRetrieveContactDetails());
				messageCommand.setDigitalPostCommand(setDigitalPostCommandValues(message.getDigitalPost()));
				messageCommand.setSenderId(message.getSenderId());
				messageCommand.setSenderOrgNumber(message.getSenderOrgNumber());
				messageCommand.setSsn(message.getSsn());
				messageCommand.setTitle(message.getDocument().getTitle());
				messageCommand.setContactRegisterStatus(message.getDigitalPost().getContactRegisterStatus());
				messageCommand.setReservationStatus(message.getDigitalPost().getReservationStatus());
				messageCommand.setPostboxAddress(message.getDigitalPost().getPostboxAddress());
				messageCommand.setPostboxVendorOrgNumber(message.getDigitalPost().getPostboxVendorOrgNumber());
				messageCommand.setMobile(message.getDigitalPost().getMobile());
				messageCommand.setEmail(message.getDigitalPost().getEmail());
			}
		}
		model.addAttribute("messageCommand", messageCommand);
		model.addAttribute("keyPairAliases", postklientService.getKeypairAliases());
		return "send_message_page";
	}

	private DigitalPostCommand setDigitalPostCommandValues(DigitalPost digitalPost) {
		DigitalPostCommand digitalPostCommand = new DigitalPostCommand();
		digitalPostCommand.setDelayedAvailabilityDate(digitalPost.getDelayedAvailabilityDate());
		digitalPostCommand.setEmailNotification(digitalPost.getEmailNotification());
		digitalPostCommand.setEmailNotificationSchedule(digitalPost.getEmailNotificationSchedule());
		digitalPostCommand.setInsensitiveTitle(digitalPost.getInsensitiveTitle());
		digitalPostCommand.setMobileNotification(digitalPost.getMobileNotification());
		digitalPostCommand.setMobileNotificationSchedule(digitalPost.getMobileNotificationSchedule());
		digitalPostCommand.setRequiresMessageOpenedReceipt(digitalPost.getRequiresMessageOpenedReceipt());
		digitalPostCommand.setSecurityLevel(digitalPost.getSecurityLevel());
		return digitalPostCommand;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/utskrift", produces = "text/html")
	public String show_print_message_page(Model model, @RequestParam(required = false) Long copy) throws NotFoundException {
		MessageCommand messageCommand = new MessageCommand(MessageCommand.Type.FYSISK);

		if (copy == null) {
			messageCommand.setLanguageCode("NO");
			messageCommand.setSenderOrgNumber(environment.getProperty("sdp.behandlingsansvarlig.organisasjonsnummer"));
			messageCommand.setKeyPairAlias(environment.getProperty("sdp.databehandler.keypair.alias"));
		}  else {
			Message message = messageService.getMessage(copy);
			if (message == null) {
				throw new NotFoundException();
			} else {
				messageCommand.setKeyPairAlias(message.getKeyPairAlias());
				messageCommand.setLanguageCode(message.getLanguageCode());
				messageCommand.setPriority(message.getPriority());
				messageCommand.setFysiskPostCommand(setFysiskPostCommandValues(message.getFysiskPost()));
				messageCommand.setSenderId(message.getSenderId());
				messageCommand.setSenderOrgNumber(message.getSenderOrgNumber());
				messageCommand.setSsn(message.getSsn());
				messageCommand.setTitle(message.getDocument().getTitle());
			}
		}
		model.addAttribute("messageCommand", messageCommand);
		model.addAttribute("keyPairAliases", postklientService.getKeypairAliases());
		model.addAttribute("keyPairTekniskMottakerAliases", postklientService.getKeyStoreTekniskMottakerAliases());
		return "print_message_page";
	}

	private FysiskPostCommand setFysiskPostCommandValues(FysiskPost fysiskPost) {
		FysiskPostCommand fysiskPostCommand = new FysiskPostCommand();
		fysiskPostCommand.setAdressat(getAdresse(fysiskPost.getAdressat()));
		fysiskPostCommand.setReturadresse(getAdresse(fysiskPost.getReturadresse()));
		fysiskPostCommand.setReturhaandtering(fysiskPost.getReturhaandtering());
		fysiskPostCommand.setPosttype(fysiskPost.getPosttype());
		fysiskPostCommand.setUtskriftsfarge(fysiskPost.getUtskriftsfarge());
		fysiskPostCommand.setUtskriftsleverandoer(fysiskPost.getTekniskMottakerSertifikatAlias());
		return fysiskPostCommand;
	}

	private KonvoluttAdresse getAdresse(no.difi.sdp.webclient.domain.KonvoluttAdresse adressat) {
		KonvoluttAdresse adresse = new KonvoluttAdresse();
		adresse.setType(adressat.getType()==no.difi.sdp.webclient.domain.KonvoluttAdresse.Type.NORSK ? KonvoluttAdresse.Type.NORSK : KonvoluttAdresse.Type.UTENLANDSK);
		adresse.setNavn(adressat.getNavn());
		if(adressat.getAdresselinjer().size() == 4) {
			adresse.setAdresselinje1(adressat.getAdresselinjer().get(0));
			adresse.setAdresselinje2(adressat.getAdresselinjer().get(1));
			adresse.setAdresselinje3(adressat.getAdresselinjer().get(2));
			adresse.setAdresselinje4(adressat.getAdresselinjer().get(3));
		}else if(adressat.getAdresselinjer().size() == 3){
			adresse.setAdresselinje1(adressat.getAdresselinjer().get(0));
			adresse.setAdresselinje2(adressat.getAdresselinjer().get(1));
			adresse.setAdresselinje3(adressat.getAdresselinjer().get(2));
		}else if(adressat.getAdresselinjer().size() == 2){
			adresse.setAdresselinje1(adressat.getAdresselinjer().get(0));
			adresse.setAdresselinje2(adressat.getAdresselinjer().get(1));
		}else if(adressat.getAdresselinjer().size() == 1){
			adresse.setAdresselinje1(adressat.getAdresselinjer().get(0));
		}
		adresse.setPostnummer(adressat.getPostnummer());
		adresse.setPoststed(adressat.getPoststed());
		adresse.setLand(adressat.getLand());
		adresse.setLandkode(adressat.getLandkode());
		return adresse;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/messages")
	public String send_message(@Validated @ModelAttribute("messageCommand") MessageCommand messageCommand, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) throws IOException {
		validateUserSpecifiedContactDetails(messageCommand, bindingResult);
		messageValidator.validate(messageCommand, bindingResult);
		if (bindingResult.hasErrors()) {
			model.addAttribute("messageCommand", messageCommand);
			model.addAttribute("keyPairAliases", postklientService.getKeypairAliases());
			model.addAttribute("errors", bindingResult);
			return "send_message_page";
		}
		Message message = new Message(messageCommand.isDigitalPost());
		message.setSsn(messageCommand.getSsn());
		DigitalPost digitalPost = new DigitalPost(messageCommand.getDigitalPostCommand().getInsensitiveTitle());
		message.setDigitalPost(digitalPost);
		setCommonMessageAttributes(messageCommand, request, message);
		setDigitalPostData(digitalPost, messageCommand.getDigitalPostCommand());
		message.getDigitalPost().setRetrieveContactDetails(messageCommand.getRetrieveContactDetails());
		message.setSaveBinaryContent(true);
		if (! message.getDigitalPost().getRetrieveContactDetails()) {
			message.getDigitalPost().setContactRegisterStatus(messageCommand.getContactRegisterStatus());
			message.getDigitalPost().setReservationStatus(messageCommand.getReservationStatus());
			message.getDigitalPost().setPostboxAddress(messageCommand.getPostboxAddress());
			message.getDigitalPost().setPostboxVendorOrgNumber(messageCommand.getPostboxVendorOrgNumber());
			message.getDigitalPost().setPostboxCertificate(messageCommand.getPostboxCertificate() == null ? null : messageCommand.getPostboxCertificate().getBytes());
			message.getDigitalPost().setMobile(messageCommand.getMobile());
			message.getDigitalPost().setEmail(messageCommand.getEmail());
		}
		messageService.sendMessage(message);
		return "redirect:/client/messages/" + message.getId();
	}

	private Set<Document> getAttachments(MessageCommand messageCommand, HttpServletRequest request, Message message) throws IOException {
		Set<Document> attachments = new HashSet<>();
		if (messageCommand.getAttachments() != null) {
			for (MultipartFile multipartFile : messageCommand.getAttachments()) {
				if (! multipartFile.isEmpty()) {
					Document attachment = new Document();
					attachment.setTitle(resolveAttachmentTitle(multipartFile, request));
					attachment.setContent(multipartFile.getBytes());
					attachment.setFilename(multipartFile.getOriginalFilename());
					attachment.setMimetype(multipartFile.getContentType());
					attachment.setMessage(message);
					attachments.add(attachment);
				}
			}
		}
		return attachments;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/printmessages")
	public String send_print_message(@Validated @ModelAttribute("messageCommand") MessageCommand messageCommand, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) throws IOException {
	//	messageValidator.validate(messageCommand, bindingResult);
		//if (bindingResult.hasErrors()) {
	//		model.addAttribute("messageCommand", messageCommand);
	//		model.addAttribute("keyPairAliases", postklientService.getKeypairAliases());
	//		model.addAttribute("keyPairTekniskMottakerAliases", postklientService.getKeyStoreTekniskMottakerAliases());
	//		return "print_message_page";
	//	}
		Message message = new Message(messageCommand.isDigitalPost());
//		message.setSsn(messageCommand.getSsn());

		final FysiskPostCommand fysiskPostCommand = messageCommand.getFysiskPostCommand();
		no.difi.sdp.webclient.domain.KonvoluttAdresse adressat = convertAdresse(fysiskPostCommand.getAdressat());
		no.difi.sdp.webclient.domain.KonvoluttAdresse returAdresse = convertAdresse(fysiskPostCommand.getReturadresse());
		FysiskPost fysiskPost = new FysiskPost(fysiskPostCommand.getPosttype(), fysiskPostCommand.getUtskriftsfarge(), fysiskPostCommand.getReturhaandtering(), adressat, returAdresse);
        fysiskPost.setTekniskMottakerSertifikatAlias(fysiskPostCommand.getUtskriftsleverandoer());
		message.setFysiskPost(fysiskPost);
		setCommonMessageAttributes(messageCommand, request, message);
		messageService.sendMessage(message);

		return "redirect:/client/messages/" + message.getId();
	}

	private void setCommonMessageAttributes(MessageCommand messageCommand, HttpServletRequest request, Message message) throws IOException {
		message.setDocument(getDocument(messageCommand));
		message.setAttachments(getAttachments(messageCommand, request, message));
		message.setSenderOrgNumber(messageCommand.getSenderOrgNumber());
		message.setSenderId(messageCommand.getSenderId());
		message.setInvoiceReference(messageCommand.getInvoiceReference());
		message.setKeyPairAlias(messageCommand.getKeyPairAlias());
		message.setPriority(messageCommand.getPriority());
		message.setLanguageCode(messageCommand.getLanguageCode());
	}

	private no.difi.sdp.webclient.domain.KonvoluttAdresse convertAdresse(KonvoluttAdresse adressatInput) {

		no.difi.sdp.webclient.domain.KonvoluttAdresse adressat = new no.difi.sdp.webclient.domain.KonvoluttAdresse();
		adressat.setType(adressatInput.getType()== KonvoluttAdresse.Type.NORSK ? no.difi.sdp.webclient.domain.KonvoluttAdresse.Type.NORSK : no.difi.sdp.webclient.domain.KonvoluttAdresse.Type.UTENLANDSK);
		List<String> adresse = new ArrayList<>(4);
		adresse.add(adressatInput.getAdresselinje1());
		adresse.add(adressatInput.getAdresselinje2());
		adresse.add(adressatInput.getAdresselinje3());
		adresse.add(adressatInput.getAdresselinje4());
		adressat.setAdresselinjer(adresse);
		adressat.setNavn(adressatInput.getNavn());
		adressat.setPostnummer(adressatInput.getPostnummer());
		adressat.setPoststed(adressatInput.getPostnummer());
		adressat.setLand(adressatInput.getLand());
		adressat.setLandkode(adressatInput.getLandkode());
		return adressat;
	}

	private Document getDocument(MessageCommand messageCommand) throws IOException {
		Document document = new Document();
		document.setTitle(messageCommand.getTitle());
		document.setContent(messageCommand.getDocument().getBytes());
		document.setFilename(messageCommand.getDocument().getOriginalFilename());
		document.setMimetype(messageCommand.getDocument().getContentType());
		return document;
	}

	private void setDigitalPostData(DigitalPost digitalPost, DigitalPostCommand digitalPostCommand) {
		digitalPost.setSecurityLevel(digitalPostCommand.getSecurityLevel());
		digitalPost.setEmailNotification(digitalPostCommand.getEmailNotification());
		digitalPost.setEmailNotificationSchedule(digitalPostCommand.getEmailNotificationSchedule());
		digitalPost.setMobileNotification(digitalPostCommand.getMobileNotification());
		digitalPost.setMobileNotificationSchedule(digitalPostCommand.getMobileNotificationSchedule());
		digitalPost.setRequiresMessageOpenedReceipt(digitalPostCommand.getRequiresMessageOpenedReceipt());
		digitalPost.setDelayedAvailabilityDate(digitalPostCommand.getDelayedAvailabilityDate());
	}

	/**
	 * Resolves the attachment title for a given MultipartFile.
	 * See send_message_page.js for frontend implementation.
	 *
	 * @param multipartFile
	 * @param request
	 * @return Attachment title
	 */
	private String resolveAttachmentTitle(MultipartFile multipartFile, HttpServletRequest request) {
		String attachmentTitle = null;
		for (Entry<String, String[]> parameter : request.getParameterMap().entrySet()) {
			if (parameter.getKey().startsWith(FORM_ATTACHMENT_NAME_PREFIX)) {
				// We matched an attachmentName#-parameter
				if (multipartFile.getOriginalFilename().equals(parameter.getValue()[0])) {
					// The attachmentName#-parameter matched the attachment we are resolving a title for
					// The attachmentNumber of the attachmentName#-parameter allows us to resolve the attachmentTitle#-parameter
					String attachmentNumber = parameter.getKey().replace(FORM_ATTACHMENT_NAME_PREFIX, "");
					String attachmentTitleParameter = FORM_ATTACHMENT_TITLE_PREFIX + attachmentNumber;
					attachmentTitle = request.getParameter(attachmentTitleParameter);
					if (attachmentTitle != null && attachmentTitle.length() > 0) {
						// The attachment title was provided
						return attachmentTitle;
					}
				}
			}
		}
		// No attachment title was provided, falls back to using the original filename as attachment title
		return multipartFile.getOriginalFilename();
	}

	private void validateUserSpecifiedContactDetails(MessageCommand messageCommand, BindingResult bindingResult) {
		if (messageCommand.getRetrieveContactDetails()) {
			return;
		}
		if (messageCommand.getPostboxAddress() == null || messageCommand.getPostboxAddress().length() == 0) {
			bindingResult.addError(new FieldError("messageCommand", "postboxAddress", "Du må oppgi postkasseadresse"));
		}
		if (messageCommand.getPostboxVendorOrgNumber() == null || messageCommand.getPostboxVendorOrgNumber().length() == 0) {
			bindingResult.addError(new FieldError("messageCommand", "postboxVendorOrgNumber", "Du må oppgi organisasjonsnummer for postkasseleverandør"));
		}
		if (messageCommand.getPostboxCertificate() == null || messageCommand.getPostboxCertificate().isEmpty()) {
			bindingResult.addError(new FieldError("messageCommand", "postboxCertificate", "Du må oppgi postkassesertifikat"));
		}
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
		if (document == null || document.getContent() == null) {
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

	@RequestMapping(method = RequestMethod.GET, value = "/messages/{id}/postboxcertificate")
	public void download_message_postboxcertificate(@PathVariable Long id, HttpServletResponse response) throws NotFoundException, IOException {
		Message message = messageService.getMessage(id);
		if (message == null || message.getDigitalPost().getPostboxCertificate() == null) {
			throw new NotFoundException();
		}
		response.addHeader("Content-Disposition", "attachment; filename=\"message-" + message.getId() + "-postboxcertificate.cer\"");
		response.setContentType("application/x-x509-ca-cert");
		InputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(message.getDigitalPost().getPostboxCertificate()));
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
	public String show_message_list_page(Model model, @RequestParam(required = false) MessageStatus status, @RequestParam(defaultValue = "0") int pageNumber) {
		Page<Message> messagePage;
		if (status == null) {
			messagePage = messageService.getMessages(pageNumber);
		} else {
			messagePage = messageService.getMessages(status, pageNumber);
		}
		model.addAttribute("messagePage", messagePage);
		model.addAttribute("messageStatus", status);
		return "show_message_list_page";
	}

    private Document getDocumentByFilename(Message message, String filename) throws IOException {
        InputStream pdfInputStream = this.getClass().getClassLoader().getResourceAsStream(filename);
        byte[] PDF = IOUtils.toByteArray(pdfInputStream);
        Document attachment = new Document();
		attachment.setTitle("Testpdf " + filename);
        attachment.setContent(PDF);
		attachment.setFilename(filename);
        attachment.setMimetype("application/pdf");
        attachment.setMessage(message);
        return attachment;
    }

    private enum PerformanceTestSize {
        SIZE_10KB,
        SIZE_80KB,
        SIZE_800KB,
        SIZE_8MB
    }

    private enum PostboxVendor {
    	EBOKS,
    	DIGIPOST
    }

    @RequestMapping(method = RequestMethod.GET, value = "/performance")
    @ResponseStatus(value = HttpStatus.OK)
    public void performanceTestSendMessage(@RequestParam String ssn, @RequestParam PerformanceTestSize size, @RequestParam(required = false) String postboxAddress, @RequestParam(required = false) PostboxVendor postboxVendor) throws IOException {
        Message message = new Message();
        message.setSsn(ssn);
		DigitalPost digitalPost = new DigitalPost("Brev til " + ssn + " " + new Date());
		message.setDigitalPost(digitalPost);
        message.setPriority(Prioritet.NORMAL);
        digitalPost.setSecurityLevel(Sikkerhetsnivaa.NIVAA_3);
        message.setLanguageCode("NO");
        message.setSenderOrgNumber(environment.getProperty("sdp.behandlingsansvarlig.organisasjonsnummer"));
        message.setKeyPairAlias(environment.getProperty("sdp.databehandler.keypair.alias"));
        Set<Document> attachments = new HashSet<Document>();
        String pdfInputFileName;
        switch (size) {

            case SIZE_10KB:
                pdfInputFileName = "SDP-Litedok_NAV-10kB.pdf";
                break;
            case SIZE_80KB:
                pdfInputFileName = "SDP-MiddelsLiteDok_kreftreg-80kB.pdf";
                break;
            case SIZE_800KB:
                pdfInputFileName = "SDP-MiddelsStortdok_SI-800kB.pdf";
                break;
            case SIZE_8MB:
                pdfInputFileName = "SDP-StortDokument-4MB.pdf";
                attachments.add(getDocumentByFilename(message, "SDP-Vedlegg1-2MB.pdf"));
                attachments.add(getDocumentByFilename(message, "SDP-Vedlegg2-2MB.pdf"));
                break;
            default:
                throw new RuntimeException("Size not supported: " + size.toString());
        }
        message.setAttachments(attachments);

        InputStream pdfInputStream = this.getClass().getClassLoader().getResourceAsStream(pdfInputFileName);
        byte[] pdf = IOUtils.toByteArray(pdfInputStream);
        Document document = new Document();
        document.setFilename("testfil.pdf");
        document.setContent(pdf);
        document.setMimetype("application/pdf");
        document.setTitle("Brev til " + ssn + " " + new Date());
        message.setDocument(document);
        message.getDigitalPost().setRetrieveContactDetails(postboxAddress == null || postboxVendor == null);
        message.setSaveBinaryContent(false);
        if (! message.getDigitalPost().getRetrieveContactDetails()) {
        	// Uses the provided contact details (skips retrieval of contact details from oppslagstjenesten)
        	message.getDigitalPost().setContactRegisterStatus(Status.AKTIV);
        	message.getDigitalPost().setReservationStatus(Reservasjon.NEI);
        	message.getDigitalPost().setPostboxAddress(postboxAddress);
        	switch (postboxVendor) {
				case DIGIPOST:
					message.getDigitalPost().setPostboxVendorOrgNumber(environment.getProperty("performancetest.digipost.orgnr"));
		        	message.getDigitalPost().setPostboxCertificate(Base64.decodeBase64(environment.getProperty("performancetest.digipost.postbox.certificate")));
					break;
				case EBOKS:
					message.getDigitalPost().setPostboxVendorOrgNumber(environment.getProperty("performancetest.eboks.orgnr"));
		        	message.getDigitalPost().setPostboxCertificate(Base64.decodeBase64(environment.getProperty("performancetest.eboks.postbox.certificate")));
					break;
				default:
					throw new RuntimeException("Postbox vendor not supported: " + postboxVendor.toString());
        	}
        }
        messageService.sendMessage(message);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/report")
    public String showReportPage(Model model) {
    	List<Object[]> countByStatus = messageService.getCountByStatus();
    	model.addAttribute("countByStatus", countByStatus);
    	return "show_report_page";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/report/download")
    @ResponseBody
    public String downloadReport(HttpServletResponse response) {
    	response.addHeader("Content-Disposition", "attachment; filename=\"report.csv\"");
		response.setContentType("text/csv");
		StringWriter writer = new StringWriter();
		writeReportColumn(writer, "id", false);
		writeReportColumn(writer, "ssn", false);
		writeReportColumn(writer, "postboxVendorOrgNumber", false);
		writeReportColumn(writer, "postboxAddress", false);
		writeReportColumn(writer, "status", false);
		writeReportColumn(writer, "date", false);
		writeReportColumn(writer, "requestSentDate", false);
		writeReportColumn(writer, "responseReceivedDate", false);
		writeReportColumn(writer, "completedDate", false);
		writeReportColumn(writer, "receiptType", false);
		writeReportColumn(writer, "receiptDate", false);
		writeReportColumn(writer, "receiptRequestSentDate", false);
		writeReportColumn(writer, "receiptResponseReceivedDate", false);
		writeReportColumn(writer, "receiptCompletedDate", false);
		writeReportColumn(writer, "receiptAckRequestSentDate", false);
		writeReportColumn(writer, "receiptAckResponseReceivedDate", false);
		writeReportColumn(writer, "receiptPostboxDate", true);
		List<Object[]> messages = messageService.getReport();
		for (Object[] message : messages) {
			writeReportColumn(writer, (String) message[0], false);
			writeReportColumn(writer, (String) message[1], false);
			writeReportColumn(writer, (String) message[2], false);
			writeReportColumn(writer, (String) message[3], false);
			writeReportColumn(writer, (MessageStatus) message[4], false);
			writeReportColumn(writer, (Date) message[5], false);
			writeReportColumn(writer, (Date) message[6], false);
			writeReportColumn(writer, (Date) message[7], false);
			writeReportColumn(writer, (Date) message[8], false);
			writeReportColumn(writer, (String) message[9], false);
			writeReportColumn(writer, (Date) message[10], false);
			writeReportColumn(writer, (Date) message[11], false);
			writeReportColumn(writer, (Date) message[12], false);
			writeReportColumn(writer, (Date) message[13], false);
			writeReportColumn(writer, (Date) message[14], false);
			writeReportColumn(writer, (Date) message[15], false);
			writeReportColumn(writer, (Date) message[16], true);
		}
    	return writer.toString();
    }

    private void writeReportColumn(StringWriter writer, Enum<?> data, boolean lastColumn) {
    	writer.write(data == null ? "" : data.toString());
    	writer.write(lastColumn ? "\n" : "\t");
    }

    private void writeReportColumn(StringWriter writer, String data, boolean lastColumn) {
    	writer.write(data == null ? "" : data);
    	writer.write(lastColumn ? "\n" : "\t");
    }

    private void writeReportColumn(StringWriter writer, Date data, boolean lastColumn) {
    	writer.write(data == null ? "" : String.valueOf(data.getTime()));
    	writer.write(lastColumn ? "\n" : "\t");
    }

    @ModelAttribute("oppslagstjenestenUrl")
	private String oppslagstjenestenUrl() {
		return environment.getProperty("oppslagstjenesten.url");
	}

	@ModelAttribute("meldingsformidlerUrl")
	private String meldingsformidlerUrl() {
		return environment.getProperty("sdp.meldingsformidler.url");
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
