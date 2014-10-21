package no.difi.sdp.webclient.service;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import no.difi.begrep.Kontaktinformasjon;
import no.difi.begrep.Person;
import no.difi.begrep.Reservasjon;
import no.difi.begrep.SikkerDigitalPostAdresse;
import no.difi.begrep.Status;
import no.difi.kontaktinfo.wsdl.oppslagstjeneste_14_05.Oppslagstjeneste1405;
import no.difi.kontaktinfo.xsd.oppslagstjeneste._14_05.HentPersonerForespoersel;
import no.difi.kontaktinfo.xsd.oppslagstjeneste._14_05.HentPersonerRespons;
import no.difi.kontaktinfo.xsd.oppslagstjeneste._14_05.Informasjonsbehov;
import no.difi.sdp.client.SikkerDigitalPostKlient;
import no.difi.sdp.client.domain.*;
import no.difi.sdp.client.domain.digital_post.DigitalPost;
import no.difi.sdp.client.domain.digital_post.EpostVarsel;
import no.difi.sdp.client.domain.digital_post.SmsVarsel;
import no.difi.sdp.client.domain.kvittering.AapningsKvittering;
import no.difi.sdp.client.domain.kvittering.Feil;
import no.difi.sdp.client.domain.kvittering.ForretningsKvittering;
import no.difi.sdp.client.domain.kvittering.KvitteringForespoersel;
import no.difi.sdp.client.domain.kvittering.LeveringsKvittering;
import no.difi.sdp.client.domain.kvittering.VarslingFeiletKvittering;
import no.difi.sdp.webclient.configuration.util.CryptoUtil;
import no.difi.sdp.webclient.configuration.util.Holder;
import no.difi.sdp.webclient.configuration.util.StringUtil;
import no.difi.sdp.webclient.domain.*;
import no.difi.sdp.webclient.repository.DocumentRepository;
import no.difi.sdp.webclient.repository.MessageRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

	private final static Logger LOGGER = LoggerFactory.getLogger(MessageService.class);
	
	private final static int NUMBER_OF_MESSAGES_PER_PAGE = 100;
	
	@Autowired
	private MessageRepository messageRepository;
    
	@Autowired
	private DocumentRepository documentRepository;
	
	@Autowired
	private Oppslagstjeneste1405 oppslagstjeneste;
    
	@Autowired
	private StringUtil stringUtil;
	
	@Autowired
	private StringWriter xmlRetrievePersonsRequest; // Set by SdpClientConfiguration.oppslagstjenesteLoggingOutInterceptor
	
	@Autowired
	private StringWriter xmlRetrievePersonsRequestPayload; // Set by MessageService.extractOppslagstjenesteMessages(HentPersonerForespoersel, HentPersonerRespons)
	
	@Autowired
	private StringWriter xmlRetrievePersonsResponse; // Set by SdpClientConfiguration.oppslagstjenesteLoggingInInterceptor
	
	@Autowired
	private StringWriter xmlRetrievePersonsResponsePayload; // Set by MessageService.extractOppslagstjenesteMessages(HentPersonerForespoersel, HentPersonerRespons)
	
	@Autowired
	private Holder<StringWriter> postKlientSoapRequest; // Set by SdpClientConfiguration.postKlientSoapInterceptor
	
	@Autowired
	private Holder<StringWriter> postKlientSoapRequestPayload; // Set by SdpClientConfiguration.postKlientSoapInterceptor
	
	@Autowired
	private Holder<Date> postKlientSoapRequestSentDate; // Set by SdpClientConfiguration.postKlientSoapInterceptor
	
	@Autowired
	private Holder<StringWriter> postKlientSoapResponse; // Set by SdpClientConfiguration.postKlientSoapInterceptor
	
	@Autowired
	private Holder<StringWriter> postKlientSoapResponsePayload; // Set by SdpClientConfiguration.postKlientSoapInterceptor
	
	@Autowired
	private Holder<Date> postKlientSoapResponseReceivedDate; // Set by SdpClientConfiguration.postKlientSoapInterceptor
	
	@Autowired
	private CryptoUtil cryptoUtil;
	
	@Autowired
	private ConfigurationService configurationService;
	
	@Autowired
	private PostklientService postklientService;
	
	private HentPersonerForespoersel buildHentPersonerForespoersel(String ssn) {
    	HentPersonerForespoersel hentPersonerForespoersel = new HentPersonerForespoersel();
        hentPersonerForespoersel.getInformasjonsbehov().add(Informasjonsbehov.KONTAKTINFO);
        hentPersonerForespoersel.getInformasjonsbehov().add(Informasjonsbehov.PERSON);
        hentPersonerForespoersel.getInformasjonsbehov().add(Informasjonsbehov.SERTIFIKAT);
        hentPersonerForespoersel.getInformasjonsbehov().add(Informasjonsbehov.SIKKER_DIGITAL_POST);
        hentPersonerForespoersel.getPersonidentifikator().add(ssn);
        return hentPersonerForespoersel;
    }
    
    private void enrichMessage(Message message, HentPersonerRespons hentPersonerRespons) {
    	if (hentPersonerRespons.getPerson().size() != 1) {
        	return;
        }
    	Person person = hentPersonerRespons.getPerson().get(0);
    	message.setContactRegisterStatus(person.getStatus());
        Reservasjon reservasjon = person.getReservasjon();
        if (reservasjon != null) {
        	message.setReservationStatus(person.getReservasjon());
        }
        SikkerDigitalPostAdresse sikkerDigitalPostAdresse = person.getSikkerDigitalPostAdresse();
        if (sikkerDigitalPostAdresse != null) {
        	message.setPostboxAddress(sikkerDigitalPostAdresse.getPostkasseadresse());
            message.setPostboxVendorOrgNumber(sikkerDigitalPostAdresse.getPostkasseleverandoerAdresse());
        }
        message.setPostboxCertificate(person.getX509Sertifikat());
        Kontaktinformasjon kontaktinformasjon = person.getKontaktinformasjon();    
    	if (kontaktinformasjon != null && kontaktinformasjon.getEpostadresse() != null) {
    		message.setEmail(kontaktinformasjon.getEpostadresse().getValue());
    	}
    	if (kontaktinformasjon != null && kontaktinformasjon.getMobiltelefonnummer() != null) {
        	message.setMobile(kontaktinformasjon.getMobiltelefonnummer().getValue());
        }
    }
    
    private void enrichMessage(Message message, Forsendelse forsendelse) {
    	message.setConversationId(forsendelse.getKonversasjonsId());
    	if (message.getSaveBinaryContent()) {
    		message.setAsic(postklientService.createAsice(message.getKeyPairAlias(), forsendelse));
    	}
    }
    
    private EpostVarsel buildEpostVarsel(Message message) {
    	if (message.getEmailNotification() == null || message.getEmail() == null) {
    		return null;
    	}
        EpostVarsel.Builder epostVarselBuilder = EpostVarsel.builder(message.getEmail(), message.getEmailNotification());
        if (message.getEmailNotificationSchedule() != null) {
        	epostVarselBuilder.varselEtterDager(StringUtil.toIntList(message.getEmailNotificationSchedule()));
        }
        return epostVarselBuilder.build();
    }
    
    private SmsVarsel buildSmsVarsel(Message message) {
    	if (message.getMobileNotification() == null || message.getMobile() == null) {
    		return null;
    	}
    	SmsVarsel.Builder smsVarselBuilder = SmsVarsel.builder(message.getMobile(), message.getMobileNotification());
    	if (message.getMobileNotificationSchedule() != null) {
    		smsVarselBuilder.varselEtterDager(StringUtil.toIntList(message.getMobileNotificationSchedule()));
    	}
        return smsVarselBuilder.build();
    }
    
    private Sertifikat buildSertifikat(Message message) {
    	X509Certificate x509Certificate = cryptoUtil.loadX509Certificate(message.getPostboxCertificate());
    	return Sertifikat.fraCertificate(x509Certificate);
    }
    
    private Mottaker buildMottaker(Message message) {
    	Sertifikat sertifikat = buildSertifikat(message);
    	return Mottaker
    			.builder(message.getSsn(), message.getPostboxAddress(), sertifikat, message.getPostboxVendorOrgNumber())
    			.build();
    }
    
    private Dokument buildDokument(Document document) {
    	ByteArrayInputStream documentContent = new ByteArrayInputStream(document.getContent());
    	return Dokument
        		.builder(document.getTitle(), document.getFilename(), documentContent)
        		.mimeType(document.getMimetype())
        		.build();
    }
    
    private List<Dokument> buildVedlegg(Message message) {
    	List<Dokument> attachments = new ArrayList<Dokument>();
    	for (Document document : message.getAttachments()) {
    		attachments.add(buildDokument(document));
    	}
    	return attachments;
    }

    private Behandlingsansvarlig buildBehandlingsansvarlig(Message message) {
    	Behandlingsansvarlig behandlingsansvarlig = Behandlingsansvarlig
    			.builder(message.getSenderOrgNumber())
    			.avsenderIdentifikator(message.getSenderId())
    			.fakturaReferanse(message.getInvoiceReference())
    			.build();
    	return behandlingsansvarlig;
	}

    private Dokumentpakke buildDokumentpakke(Message message) {
    	Dokumentpakke.Builder builder = Dokumentpakke.builder(buildDokument(message.getDocument()));
    	if (message.getAttachments().size() == 0) {
    		return builder.build();
    	}
    	builder.vedlegg(buildVedlegg(message));
    	return builder.build();
    }
    
    private Forsendelse buildDigitalForsendelse(Message message) {
    	Mottaker mottaker  = buildMottaker(message);
        DigitalPost digitalPost = DigitalPost
        		.builder(mottaker, message.getInsensitiveTitle())
        		.sikkerhetsnivaa(message.getSecurityLevel())
        		.aapningskvittering(message.getRequiresMessageOpenedReceipt())
        		.epostVarsel(buildEpostVarsel(message))
        		.smsVarsel(buildSmsVarsel(message))
        		.virkningsdato(message.getDelayedAvailabilityDate())
        		.build();
        Dokumentpakke dokumentPakke = buildDokumentpakke(message);
        Behandlingsansvarlig behandlingsansvarlig =  buildBehandlingsansvarlig(message);
        return Forsendelse
        		.digital(behandlingsansvarlig, digitalPost, dokumentPakke)
        		.prioritet(message.getPriority())
        		.spraakkode(message.getLanguageCode())
				.mpcId(configurationService.getConfiguration().getMessagePartitionChannel())
        		.build();
    }
    
    public void sendMessage(Message message)  {
    	message.setDate(new Date());
    	try {
    		if (message.getRetrieveContactDetails()) {
    			retrieveContactDetailsFromOppslagstjeneste(message);
    		}
    		sendMessageToMeldingsformidler(message);
    	} catch (MessageServiceException e) {
    		LOGGER.error(e.getStatus().toString(), e);
    		message.setStatus(e.getStatus());
    		message.setException(stringUtil.toString(e));
    	}
    	message.setXmlRetrievePersonsRequest(stringUtil.nullIfEmpty(xmlRetrievePersonsRequest));
    	message.setXmlRetrievePersonsRequestPayload(stringUtil.nullIfEmpty(xmlRetrievePersonsRequestPayload));
    	message.setXmlRetrievePersonsResponse(stringUtil.nullIfEmpty(xmlRetrievePersonsResponse));
    	message.setXmlRetrievePersonsResponsePayload(stringUtil.nullIfEmpty(xmlRetrievePersonsResponsePayload));
    	message.setXmlSendMessageRequest(stringUtil.nullIfEmpty(postKlientSoapRequest));
    	message.setXmlSendMessageRequestPayload(stringUtil.nullIfEmpty(postKlientSoapRequestPayload));
    	message.setXmlSendMessageResponse(stringUtil.nullIfEmpty(postKlientSoapResponse));
    	if (! message.getSaveBinaryContent()) {
    		removeBinaryContent(message);
    	}
    	message.setCompletedDate(new Date());
    	messageRepository.save(message);
	}
    
    private void removeBinaryContent(Message message) {
    	message.setAsic(null);
		message.getDocument().setContent(null);
		for (Document attachment : message.getAttachments()) {
			attachment.setContent(null);
		}
    }
    
    private void retrieveContactDetailsFromOppslagstjeneste(Message message) throws MessageServiceException {
    	try {
            HentPersonerForespoersel hentPersonerForespoersel = buildHentPersonerForespoersel(message.getSsn());
            HentPersonerRespons hentPersonerRespons = oppslagstjeneste.hentPersoner(hentPersonerForespoersel);
            extractOppslagstjenesteMessages(hentPersonerForespoersel, hentPersonerRespons);
            enrichMessage(message, hentPersonerRespons);
        } catch (Exception e) {
        	throw new MessageServiceException(MessageStatus.FAILED_RETRIEVING_PERSON_FROM_OPPSLAGSTJENESTE, e);
        }
    }
    
    private void sendMessageToMeldingsformidler(Message message) throws MessageServiceException {
    	if (! message.getContactRegisterStatus().equals(Status.AKTIV)) {
    		throw new MessageServiceException(MessageStatus.FAILED_QUALIFYING_FOR_DIGITAL_POST, "Kunne ikke sende digital post. Bruker har ikke status som aktiv i kontaktregisteret.");
        }
    	// In most (but not all) scenarios the check below should be included
        //if (message.getReservationStatus().equals(Reservasjon.JA)) {
        //	throw new MessageServiceException(MessageStatus.FAILED_QUALIFYING_FOR_DIGITAL_POST, "Kunne ikke sende digital post. Bruker har reservasjon i kontaktregisteret.");
        //}
        if (message.getPostboxAddress() == null || message.getPostboxVendorOrgNumber() == null || message.getPostboxCertificate() == null) {
        	throw new MessageServiceException(MessageStatus.FAILED_QUALIFYING_FOR_DIGITAL_POST, "Kunne ikke sende digital post. Bruker mangler postboksadresse, postboksleverandør eller postbokssertifikat i kontaktregisteret.");
        }
        try {
        	Forsendelse forsendelse = buildDigitalForsendelse(message);
    		enrichMessage(message, forsendelse);
    		SikkerDigitalPostKlient postklient = postklientService.get(message.getKeyPairAlias());
    		postklient.send(forsendelse);
			message.setRequestSentDate(postKlientSoapRequestSentDate.getValue());
			message.setResponseReceivedDate(postKlientSoapResponseReceivedDate.getValue());
	    	message.setStatus(MessageStatus.WAITING_FOR_RECEIPT);
		} catch (Exception e) {
			throw new MessageServiceException(MessageStatus.FAILED_SENDING_DIGITAL_POST, e);
		}
    }
	
	private void extractOppslagstjenesteMessages(HentPersonerForespoersel hentPersonerForespoersel, HentPersonerRespons hentPersonerRespons) {
		stringUtil.marshalJaxbObject(hentPersonerForespoersel, xmlRetrievePersonsRequestPayload);
		stringUtil.marshalJaxbObject(hentPersonerRespons, xmlRetrievePersonsResponsePayload);
	}

	public Page<Message> getMessages(int pageNumber) {
		PageRequest pageRequest = buildPageRequest(pageNumber);
		Page<Object[]> rawMessagePage = messageRepository.list(pageRequest);
		Page<Message> messagePage = toMessagePage(rawMessagePage, pageRequest);
		return messagePage;
	}
	
	public Page<Message> getMessages(MessageStatus messageStatus, int pageNumber) {
		PageRequest pageRequest = buildPageRequest(pageNumber);
		Page<Object[]> rawMessagePage = messageRepository.list(messageStatus, pageRequest);
		Page<Message> messagePage = toMessagePage(rawMessagePage, pageRequest);
		return messagePage;
	}
	
	private PageRequest buildPageRequest(int pageNumber) {
		return new PageRequest(pageNumber, NUMBER_OF_MESSAGES_PER_PAGE);
	}
	
	private Page<Message> toMessagePage(Page<Object[]> rawMessagePage, PageRequest pageRequest) {
		List<Message> messages = new ArrayList<Message>();
		for (Object[] rawMessage : rawMessagePage.getContent()) {
			// Refer to messageRepository.list() for field order
			Message message = new Message();
			message.setId((Long) rawMessage[0]);
			message.setDate((Date) rawMessage[1]);
			message.setSsn((String) rawMessage[2]);
			Document document = new Document();
			document.setTitle((String) rawMessage[3]); 
			message.setDocument(document);
			messages.add(message);
		}
		return new PageImpl<Message>(messages, pageRequest, rawMessagePage.getTotalElements());
	}

	public Message getMessage(Long id) {
		return messageRepository.findOne(id);
	}
	
	/**
	 * Gets all receipts from meldingsformidler for all integrations and priorities.
	 * @return
	 */
	@Async
	@Scheduled(fixedRate = 10000, initialDelay = 10000) // Note that in a production environment this rate is unacceptably high, but for testing purposes it is useful to get quick feedback
    public void getReceipts() {
		List<String> waitingClients = messageRepository.waitingClients();
		if (waitingClients.size() == 0) {
			LOGGER.info("No messages are waiting for receipt");
			return;
		}
		LOGGER.info("Started retrieving receipts");
    	for (String keyPairAlias : waitingClients) {
			SikkerDigitalPostKlient postklient = postklientService.get(keyPairAlias);
			LOGGER.info("Retrieving prioritized receipts for keyPairAlias " + keyPairAlias);
			while (getReceipt(Prioritet.PRIORITERT, postklient));
			LOGGER.info("Retrieving normal receipts for keyPairAlias " + keyPairAlias);
			while (getReceipt(Prioritet.NORMAL, postklient));
		}
    	LOGGER.info("Done retrieving receipts");
	}
	
	/**
	 * Gets next receipt from meldingsformidler for a given integration and priority.
	 * Resolves message for receipt from database and updates message status.
	 * @param prioritet
	 * @param postklient
	 * @return True if a receipt was available from meldingsformidler, false if not.
	 */
	private boolean getReceipt(Prioritet prioritet, SikkerDigitalPostKlient postklient) {
		Date date = new Date();
		ForretningsKvittering forretningsKvittering = postklient.hentKvittering(KvitteringForespoersel
				.builder(prioritet)
				.mpcId(configurationService.getConfiguration().getMessagePartitionChannel())
				.build());
		if (forretningsKvittering == null) {
			// No available receipts
    		LOGGER.info("No receipts were available at server");
			return false;
		}
		LOGGER.info("Recieved receipt with conversation id " + forretningsKvittering.getKonversasjonsId());
		List<Message> messages = messageRepository.findByConversationId(forretningsKvittering.getKonversasjonsId());
		if (messages.size() != 1) {
			LOGGER.error("Recieved receipt for message not found in datastore");
			try {
				postklient.bekreft(forretningsKvittering);
			} catch (Exception e) {
				LOGGER.error("Failed acknowledging retrieved receipt", e);
			}
			return true;
		}
		String xmlRequestString = stringUtil.nullIfEmpty(postKlientSoapRequest);
		String xmlResponseString = stringUtil.nullIfEmpty(postKlientSoapResponse);
		String xmlResponsePayloadString = stringUtil.nullIfEmpty(postKlientSoapResponsePayload);
    	Message message = messages.get(0);
		Receipt receipt = new Receipt();
		message.getReceipts().add(receipt);
		receipt.setMessage(message);
		receipt.setDate(date);
		receipt.setRequestSentDate(postKlientSoapRequestSentDate.getValue());
		receipt.setResponseReceivedDate(postKlientSoapResponseReceivedDate.getValue());
		receipt.setCompletedDate(new Date());
		receipt.setPostboxDate(forretningsKvittering.getTidspunkt());
		receipt.setXmlRequest(xmlRequestString);
		receipt.setXmlResponse(xmlResponseString);
		receipt.setXmlResponsePayload(xmlResponsePayloadString);
		if (forretningsKvittering instanceof Feil) {
			Feil feil = (Feil) forretningsKvittering;
			LOGGER.error("Recieved error type " + feil.getFeiltype().toString() + " with details: " + feil.getDetaljer());
			receipt.setType("Feil");
			receipt.setErrorDetails(feil.getDetaljer());
			receipt.setErrorType(feil.getFeiltype());
			message.setStatus(MessageStatus.FAILED_SENDING_DIGITAL_POST);
		} else if (forretningsKvittering instanceof AapningsKvittering) {
			receipt.setType("Åpningskvittering");
			message.setStatus(MessageStatus.SUCCESSFULLY_SENT_MESSAGE);
		} else if (forretningsKvittering instanceof LeveringsKvittering) {
			receipt.setType("Leveringskvittering");
			if (message.getRequiresMessageOpenedReceipt()) {
				message.setStatus(MessageStatus.WAITING_FOR_OPENED_RECEIPT);
			} else {
				message.setStatus(MessageStatus.SUCCESSFULLY_SENT_MESSAGE);
			}
		} else if (forretningsKvittering instanceof VarslingFeiletKvittering) {
			VarslingFeiletKvittering varslingFeiletKvittering = (VarslingFeiletKvittering) forretningsKvittering;
			receipt.setType("Varsling feilet");
			receipt.setNotificationErrorChannel(varslingFeiletKvittering.getVarslingskanal());
			receipt.setNotificationErrorDescription(varslingFeiletKvittering.getBeskrivelse());
		} else {
			LOGGER.error("Recieved unknown receipt type " + forretningsKvittering.getClass());
		}
		try {
			postklient.bekreft(forretningsKvittering);
		} catch (Exception e) {
			LOGGER.error("Failed acknowledging retrieved receipt", e);
			message.setStatus(MessageStatus.FAILED_ACKNOWLEDGING_RETRIEVED_RECEIPT);
			message.setException(stringUtil.toString(e));
		}
		receipt.setAckRequestSentDate(postKlientSoapRequestSentDate.getValue());
		receipt.setAckResponseReceivedDate(new Date());
		messageRepository.save(message);
		return true;
	}

	public void deleteMessage(Long id) {
		messageRepository.delete(id);
	}
	
	public void deleteAllMessages() {
		messageRepository.deleteAll();
	}
	
	public Document getDocument(Long id) {
		return documentRepository.findOne(id);
	}
	
	/**
	 * Gets the following fields for all messages: id, ssn, date, postboxVendorOrgNumber, postboxAddress, status, receipt.type and receipt.date.
	 * @return
	 */
	public List<Object[]> getReport() {
		return messageRepository.getReport();
	}
	
	public List<Object[]> getCountByStatus() {
		return messageRepository.countByStatus();
	}
	
	private class MessageServiceException extends Exception {
		
		private static final long serialVersionUID = 1L;
		
		private MessageStatus status;
		
		public MessageServiceException(MessageStatus status, Exception e) {
			super(e);
			this.status = status;
		}
		
		public MessageServiceException(MessageStatus status, String message) {
			super(message);
			this.status = status;
		}
		
		public MessageStatus getStatus() {
			return status;
		}
		
	}
	
}
