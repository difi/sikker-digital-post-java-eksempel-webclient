package no.difi.sdp.webclient.service;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

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
import no.difi.sdp.webclient.domain.*;
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
	private SikkerDigitalPostKlient postklient;
    
	@Autowired
	private Oppslagstjeneste1405 oppslagstjeneste;
    
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
    
    private String nullIfEmpty(String string) {
    	if (string.isEmpty()) {
    		return null;
    	}
    	return string;
    }
    
    private void enrichMessage(Message message, Forsendelse forsendelse) {
    	message.setConversationId(forsendelse.getKonversasjonsId());
    }
    
    private void marshalJaxbObject(Object jaxbObject, StringWriter stringWriter) {
    	try {
    		Marshaller m = JAXBContext.newInstance(jaxbObject.getClass()).createMarshaller();
        	m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        	m.marshal(jaxbObject, stringWriter);
    	} catch (JAXBException e) {
    		LOGGER.error("Failed marsharalling JAXB object", e);
    	}
    }
    
    private EpostVarsel buildEpostVarsel(Message message) {
    	if (message.getEmailNotification() == null || message.getEmail() == null) {
    		return null;
    	}
        EpostVarsel.Builder epostVarselBuilder = EpostVarsel.builder(message.getEmail(), message.getEmailNotification());
        if (message.getEmailNotificationSchedule() != null) {
        	epostVarselBuilder.varselEtterDager(Message.toIntList(message.getEmailNotificationSchedule()));
        }
        return epostVarselBuilder.build();
    }
    
    private SmsVarsel buildSmsVarsel(Message message) {
    	if (message.getMobileNotification() == null || message.getMobile() == null) {
    		return null;
    	}
    	SmsVarsel.Builder smsVarselBuilder = SmsVarsel.builder(message.getMobile(), message.getMobileNotification());
    	if (message.getMobileNotificationSchedule() != null) {
    		smsVarselBuilder.varselEtterDager(Message.toIntList(message.getMobileNotificationSchedule()));
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
    
    private Dokument buildDokument(Message message) {
    	if (message.getAttachment() == null) {
    		return null;
    	}
    	ByteArrayInputStream documentContent = new ByteArrayInputStream(message.getAttachment());
        String attachmentFilename = message.getAttachmentFilename();
        return Dokument
        		.builder(message.getSensitiveTitle(), attachmentFilename, documentContent)
        		.mimeType(message.getAttachmentMimetype())
        		.build();
    }

    private Behandlingsansvarlig buildBehandlingsansvarlig() {
    	String orgNumber = nullIfEmpty(environment.getProperty("meldingsformidler.avsender.organisasjonsnummer"));
    	String avsenderIdentifikator = nullIfEmpty(environment.getProperty("meldingsformidler.avsender.identifikator"));
    	String fakturaReferanse = nullIfEmpty(environment.getProperty("meldingsformidler.avsender.fakturareferanse"));
    	Behandlingsansvarlig behandlingsansvarlig = Behandlingsansvarlig
    			.builder(orgNumber)
    			.avsenderIdentifikator(avsenderIdentifikator)
    			.fakturaReferanse(fakturaReferanse).build();
    	return behandlingsansvarlig;
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
        Dokument dokument = buildDokument(message);
        Dokumentpakke dokumentPakke = Dokumentpakke.builder(dokument).build(); // TODO støtte for vedlegg
        Behandlingsansvarlig behandlingsansvarlig =  buildBehandlingsansvarlig();
        return Forsendelse
        		.digital(behandlingsansvarlig, digitalPost, dokumentPakke) // TODO støtte for prioritet, språkkode
        		.build();
    }
    
    public void sendMessage(Message message)  {
    	try {
    		retrieveContactDetailsFromOppslagstjeneste(message);
    		sendMessageToMeldingsformidler(message);
    		message.setStatus(MessageStatus.WAITING_FOR_RECEIPT);
    	} catch (MessageServiceException e) {
    		LOGGER.error(e.getStatus().toString(), e);
    		message.setStatus(e.getStatus());
    	}
    	message.setXmlRetrievePersonsRequest(nullIfEmpty(xmlRetrievePersonsRequest.toString()));
    	message.setXmlRetrievePersonsRequestPayload(nullIfEmpty(xmlRetrievePersonsRequestPayload.toString()));
    	message.setXmlRetrievePersonsResponse(nullIfEmpty(xmlRetrievePersonsResponse.toString()));
    	message.setXmlRetrievePersonsResponsePayload(nullIfEmpty(xmlRetrievePersonsResponsePayload.toString()));
    	message.setXmlSendMessageRequest(nullIfEmpty(postKlientSoapRequest.toString()));
    	message.setXmlSendMessageRequestPayload(postKlientSoapRequestPayload.toString());
    	message.setXmlSendMessageResponse(nullIfEmpty(postKlientSoapResponse.toString()));
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
		marshalJaxbObject(hentPersonerForespoersel, xmlRetrievePersonsRequestPayload);
        marshalJaxbObject(hentPersonerRespons, xmlRetrievePersonsResponsePayload);
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
	public boolean getReceipt() {
		List<Message> messagesWaitingForReceipt = messageRepository.findByStatus(MessageStatus.WAITING_FOR_RECEIPT);
		if (messagesWaitingForReceipt.size() == 0) {
			// No messages waiting for receipt
			return false;
		}
		ForretningsKvittering forretningsKvittering = postklient.hentKvittering(KvitteringForespoersel.builder(Prioritet.NORMAL).build());
		// Reading all the ClearAfterReadStringWriters at once ensures that they will be cleared in all cases
		String xmlRequestString = nullIfEmpty(postKlientSoapRequest.toString());
		postKlientSoapRequestPayload.toString();
		String xmlResponseString = nullIfEmpty(postKlientSoapResponse.toString());
		String xmlResponsePayloadString = nullIfEmpty(postKlientSoapResponsePayload.toString());
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
		} else if (forretningsKvittering instanceof LeveringsKvittering) {
			receipt.setType("Leveringskvittering");
			message.setStatus(MessageStatus.SUCCESSFULLY_SENT_MESSAGE);
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
