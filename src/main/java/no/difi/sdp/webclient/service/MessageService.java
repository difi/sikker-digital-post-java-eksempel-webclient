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
import no.difi.sdp.client.asice.CreateASiCE;
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
import no.difi.sdp.webclient.configuration.util.StringUtil;
import no.difi.sdp.webclient.domain.*;
import no.difi.sdp.webclient.repository.DocumentRepository;
import no.difi.sdp.webclient.repository.MessageRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

	private final static Logger LOGGER = LoggerFactory.getLogger(MessageService.class);
	
	@Autowired
	private Environment environment;
	
	@Autowired
	private MessageRepository messageRepository;
    
	@Autowired
	private DocumentRepository documentRepository;
	
	@Autowired
	private SikkerDigitalPostKlient postklient;
    
	@Autowired
	private Oppslagstjeneste1405 oppslagstjeneste;
    
	@Autowired
	private StringUtil stringUtil;
	
	@Autowired
	private StringWriter xmlRetrievePersonsRequest;
	
	@Autowired
	private StringWriter xmlRetrievePersonsRequestPayload;
	
	@Autowired
	private StringWriter xmlRetrievePersonsResponse;
	
	@Autowired
	private StringWriter xmlRetrievePersonsResponsePayload;
	
	@Autowired
	private StringWriter postKlientSoapRequest;
	
	@Autowired
	private StringWriter postKlientSoapRequestPayload;
	
	@Autowired
	private StringWriter postKlientSoapResponse;
	
	@Autowired
	private StringWriter postKlientSoapResponsePayload;
	
	@Autowired
	private CryptoUtil cryptoUtil;
	
	@Autowired
	private ConfigurationService configurationService;
	
	@Autowired
	private CreateASiCE createAsice;
	
	@Autowired
	private TekniskAvsender tekniskAvsender;
	
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
    	message.setDate(new Date());
    	message.setAsic(createAsice.createAsice(tekniskAvsender, forsendelse).getBytes());
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
    	String orgNumber = stringUtil.nullIfEmpty(environment.getProperty("meldingsformidler.avsender.organisasjonsnummer"));
    	Behandlingsansvarlig behandlingsansvarlig = Behandlingsansvarlig
    			.builder(orgNumber)
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
    
    public void sendMessage(Message message, boolean retrieveContactDetails)  {
    	try {
    		if (retrieveContactDetails) {
    			retrieveContactDetailsFromOppslagstjeneste(message);
    		}
    		sendMessageToMeldingsformidler(message);
    		message.setStatus(MessageStatus.WAITING_FOR_RECEIPT);
    	} catch (MessageServiceException e) {
    		LOGGER.error(e.getStatus().toString(), e);
    		message.setStatus(e.getStatus());
    		message.setException(stringUtil.toString(e));
    	}
    	message.setXmlRetrievePersonsRequest(stringUtil.nullIfEmpty(xmlRetrievePersonsRequest.toString()));
    	message.setXmlRetrievePersonsRequestPayload(stringUtil.nullIfEmpty(xmlRetrievePersonsRequestPayload.toString()));
    	message.setXmlRetrievePersonsResponse(stringUtil.nullIfEmpty(xmlRetrievePersonsResponse.toString()));
    	message.setXmlRetrievePersonsResponsePayload(stringUtil.nullIfEmpty(xmlRetrievePersonsResponsePayload.toString()));
    	message.setXmlSendMessageRequest(stringUtil.nullIfEmpty(postKlientSoapRequest.toString()));
    	message.setXmlSendMessageRequestPayload(stringUtil.nullIfEmpty(postKlientSoapRequestPayload.toString()));
    	message.setXmlSendMessageResponse(stringUtil.nullIfEmpty(postKlientSoapResponse.toString()));
    	postKlientSoapResponsePayload.toString();
        messageRepository.save(message);
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
			postklient.send(forsendelse);
		} catch (Exception e) {
			throw new MessageServiceException(MessageStatus.FAILED_SENDING_DIGITAL_POST, e);
		}
    }
	
	private void extractOppslagstjenesteMessages(HentPersonerForespoersel hentPersonerForespoersel, HentPersonerRespons hentPersonerRespons) {
		stringUtil.marshalJaxbObject(hentPersonerForespoersel, xmlRetrievePersonsRequestPayload);
		stringUtil.marshalJaxbObject(hentPersonerRespons, xmlRetrievePersonsResponsePayload);
	}

	public List<Message> getMessages() {
		return messageRepository.findAll();
	}
	
	public Message getMessage(Long id) {
		return messageRepository.findOne(id);
	}
	
	/**
	 * Gets next receipt from meldingsformidler, resolves message for receipt from database and updates message status.
	 * @return True if a receipt was available from meldingsformidler, false if not.
	 */
	public boolean getReceipt(Prioritet prioritet) {
		if (messageRepository.countByStatus(MessageStatus.WAITING_FOR_RECEIPT) == 0 && messageRepository.countByStatus(MessageStatus.WAITING_FOR_OPENED_RECEIPT) == 0) {
			// No messages waiting for receipt
			return false;
		}
		ForretningsKvittering forretningsKvittering = postklient.hentKvittering(KvitteringForespoersel
				.builder(prioritet)
				.mpcId(configurationService.getConfiguration().getMessagePartitionChannel())
				.build());
		// Reading all the ClearAfterReadStringWriters at once ensures that they will be cleared in all cases
		String xmlRequestString = stringUtil.nullIfEmpty(postKlientSoapRequest.toString());
		postKlientSoapRequestPayload.toString();
		String xmlResponseString = stringUtil.nullIfEmpty(postKlientSoapResponse.toString());
		String xmlResponsePayloadString = stringUtil.nullIfEmpty(postKlientSoapResponsePayload.toString());
    	if (forretningsKvittering == null) {
			// No available receipts 
			return false;
		}
		List<Message> messages = messageRepository.findByConversationId(forretningsKvittering.getKonversasjonsId());
		if (messages.size() != 1) {
			LOGGER.error("Recieved receipt for message not found in datastore");
			return true;
		}
		Message message = messages.get(0);
		Receipt receipt = new Receipt();
		receipt.setMessage(message);
		receipt.setDate(forretningsKvittering.getTidspunkt());
		receipt.setXmlRequest(xmlRequestString);
		receipt.setXmlResponse(xmlResponseString);
		receipt.setXmlResponsePayload(xmlResponsePayloadString);
		messageRepository.save(message);
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
		}
		message.getReceipts().add(receipt);
		messageRepository.save(message);
		try {
			postklient.bekreft(forretningsKvittering);
		} catch (Exception e) {
			LOGGER.error("Failed acknowledging retrieved receipt", e);
			message.setStatus(MessageStatus.FAILED_ACKNOWLEDGING_RETRIEVED_RECEIPT);
			messageRepository.save(message);
			return true;
		}
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
