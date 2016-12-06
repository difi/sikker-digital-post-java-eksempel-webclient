package no.difi.sdp.testavsender.service;

import no.difi.begrep.*;
import no.difi.kontaktinfo.xsd.oppslagstjeneste._16_02.HentPersonerForespoersel;
import no.difi.kontaktinfo.xsd.oppslagstjeneste._16_02.HentPersonerRespons;
import no.difi.kontaktinfo.xsd.oppslagstjeneste._16_02.Informasjonsbehov;

import no.difi.sdp.client2.SikkerDigitalPostKlient;
import no.difi.sdp.client2.domain.Forsendelse;
import no.difi.sdp.client2.domain.Prioritet;
import no.difi.sdp.client2.domain.kvittering.*;
import no.difi.sdp.testavsender.configuration.util.Holder;
import no.difi.sdp.testavsender.configuration.util.StringUtil;
import no.difi.sdp.testavsender.domain.Document;
import no.difi.sdp.testavsender.domain.Message;
import no.difi.sdp.testavsender.domain.MessageStatus;
import no.difi.sdp.testavsender.domain.Receipt;
import no.difi.sdp.testavsender.repository.DocumentRepository;
import no.difi.sdp.testavsender.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class MessageService {

	private final static Logger LOGGER = LoggerFactory.getLogger(MessageService.class);

	private final static int NUMBER_OF_MESSAGES_PER_PAGE = 100;

	@Autowired
	private MessageRepository messageRepository;

	@Autowired
	private DocumentRepository documentRepository;

	@Autowired
	private OppslagstjenestenProvider oppslagstjenestenProvider;

    @Autowired
    BuilderService builderService;

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
    	message.getDigitalPost().setContactRegisterStatus(person.getStatus());
        Reservasjon reservasjon = person.getReservasjon();
        if (reservasjon != null) {
        	message.getDigitalPost().setReservationStatus(person.getReservasjon());
        }
        SikkerDigitalPostAdresse sikkerDigitalPostAdresse = person.getSikkerDigitalPostAdresse();
        if (sikkerDigitalPostAdresse != null) {
        	message.getDigitalPost().setPostboxAddress(sikkerDigitalPostAdresse.getPostkasseadresse());
            message.getDigitalPost().setPostboxVendorOrgNumber(sikkerDigitalPostAdresse.getPostkasseleverandoerAdresse());
        }

        message.getDigitalPost().setPostboxCertificate(person.getX509Sertifikat());
        Kontaktinformasjon kontaktinformasjon = person.getKontaktinformasjon();    

    	if (kontaktinformasjon != null && kontaktinformasjon.getEpostadresse() != null) {
    		message.getDigitalPost().setEmail(kontaktinformasjon.getEpostadresse().getValue());
    	}
    	if (kontaktinformasjon != null && kontaktinformasjon.getMobiltelefonnummer() != null) {
        	message.getDigitalPost().setMobile(kontaktinformasjon.getMobiltelefonnummer().getValue());
        }
    }

    private void enrichMessage(Message message, Forsendelse forsendelse) {
    	message.setConversationId(forsendelse.getKonversasjonsId());
    	if (message.getSaveBinaryContent()) {
    		message.setAsic(postklientService.createAsice(message.getKeyPairAlias(), forsendelse));
    	}
    }


    public void sendMessage(Message message)  {
    	message.setDate(new Date());
    	try {
    		if (message.getDigitalPost().getRetrieveContactDetails()) {
    			retrieveContactDetailsFromOppslagstjeneste(message);
    		}
    		sendMessageToMeldingsformidler(message);
    	} catch (MessageServiceException e) {
    		LOGGER.error(e.getStatus().toString(), e);
    		message.setStatus(e.getStatus());
    		message.setException(stringUtil.toString(e));
    	}
        addOppslagstjenestenSendData(message);
        addSikkerDigitalPostSendData(message);
    	if (! message.getSaveBinaryContent()) {
    		removeBinaryContent(message);
    	}
    	message.setCompletedDate(new Date());
    	messageRepository.save(message);
	}

    private void addSikkerDigitalPostSendData(Message message) {
        message.setXmlSendMessageRequest(stringUtil.nullIfEmpty(postKlientSoapRequest));
        message.setXmlSendMessageRequestPayload(stringUtil.nullIfEmpty(postKlientSoapRequestPayload));
        message.setXmlSendMessageResponse(stringUtil.nullIfEmpty(postKlientSoapResponse));
    }

    private void addOppslagstjenestenSendData(Message message) {
        message.setXmlRetrievePersonsRequest(stringUtil.nullIfEmpty(xmlRetrievePersonsRequest));
        message.setXmlRetrievePersonsRequestPayload(stringUtil.nullIfEmpty(xmlRetrievePersonsRequestPayload));
        message.setXmlRetrievePersonsResponse(stringUtil.nullIfEmpty(xmlRetrievePersonsResponse));
        message.setXmlRetrievePersonsResponsePayload(stringUtil.nullIfEmpty(xmlRetrievePersonsResponsePayload));
    }

    private void removeBinaryContent(Message message) {
    	message.setAsic(null);
		message.getDocument().setContent(null);
		for (Document attachment : message.getAttachments()) {
			attachment.setContent(null);
		}
    }

    private void retrieveContactDetailsFromOppslagstjeneste(Message message) throws MessageServiceException {
		HentPersonerForespoersel hentPersonerForespoersel = buildHentPersonerForespoersel(message.getSsn());
    	try {
            HentPersonerRespons hentPersonerRespons = oppslagstjenestenProvider.oppslagstjeneste().hentPersoner(hentPersonerForespoersel, null);
            extractOppslagstjenesteMessages(hentPersonerForespoersel, hentPersonerRespons);
            enrichMessage(message, hentPersonerRespons);
        } catch (Exception e) {
        	throw new MessageServiceException(MessageStatus.FAILED_RETRIEVING_PERSON_FROM_OPPSLAGSTJENESTE, e);
        }
    }

    private void sendMessageToMeldingsformidler(Message message) throws MessageServiceException {

        Forsendelse forsendelse = null;

        if (message.isDigital()){
            if (! message.getDigitalPost().getContactRegisterStatus().equals(Status.AKTIV)) {
                throw new MessageServiceException(MessageStatus.FAILED_QUALIFYING_FOR_DIGITAL_POST, "Kunne ikke sende digital post. Bruker har ikke status som aktiv i kontaktregisteret.");
            }

            if (message.getDigitalPost().getPostboxAddress() == null || message.getDigitalPost().getPostboxVendorOrgNumber() == null || message.getDigitalPost().getPostboxCertificate() == null) {
                throw new MessageServiceException(MessageStatus.FAILED_QUALIFYING_FOR_DIGITAL_POST, "Kunne ikke sende digital post. Bruker mangler postboksadresse, postboksleverandør eller postbokssertifikat i kontaktregisteret.");
            }
    	// In most (but not all) scenarios the check below should be included
        //if (message.getReservationStatus().equals(Reservasjon.JA)) {
        //	throw new MessageServiceException(MessageStatus.FAILED_QUALIFYING_FOR_DIGITAL_POST, "Kunne ikke sende digital post. Bruker har reservasjon i kontaktregisteret.");
        //}

        	forsendelse = builderService.buildDigitalForsendelse(message, getMessagePartitionChannel());

        } else if(!message.isDigital()) {
            forsendelse = builderService.buildFysiskForsendelse(message, getMessagePartitionChannel());
        }
		try {
			enrichMessage(message, forsendelse);
			messageRepository.save(message);
			SikkerDigitalPostKlient postklient = postklientService.get(message.getKeyPairAlias());
			postklient.send(forsendelse);
			message.setRequestSentDate(postKlientSoapRequestSentDate.getValue());
			message.setResponseReceivedDate(postKlientSoapResponseReceivedDate.getValue());
			message.setStatus(MessageStatus.WAITING_FOR_RECEIPT);

		} catch (Exception e) {
			if(message.isDigital()){
				throw new MessageServiceException(MessageStatus.FAILED_SENDING_DIGITAL_POST, e);
			}else{
				throw new MessageServiceException(MessageStatus.FAILED_SENDING_FYSISK_POST, e);
			}

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
            Message message = convertToMessage(rawMessage);
			messages.add(message);
		}
		return new PageImpl<Message>(messages, pageRequest, rawMessagePage.getTotalElements());
	}

    private Message convertToMessage(Object[] rawMessage) {
        // Refer to messageRepository.list() for field order
        Message message = new Message();
        message.setId((Long) rawMessage[0]);
        message.setDate((Date) rawMessage[1]);
        message.setSsn((String) rawMessage[2]);
        message.setDigital((boolean) rawMessage[4]);

        no.difi.sdp.testavsender.domain.FysiskPost fysiskPost= new no.difi.sdp.testavsender.domain.FysiskPost();
        no.difi.sdp.testavsender.domain.KonvoluttAdresse adressat = new no.difi.sdp.testavsender.domain.KonvoluttAdresse();
        adressat.setNavn((String) rawMessage[5]);
        fysiskPost.setAdressat(adressat);
        message.setFysiskPost(fysiskPost);

        Document document = new Document();
        document.setTitle((String) rawMessage[3]);
        message.setDocument(document);
        return message;
    }

    public Message getMessage(Long id) {
		return messageRepository.findOne(id);
	}

	/**
	 * Gets all receipts with prioritized priority from meldingsformidler for all integrations.
	 * @return
	 */
	@Async
	@Scheduled(fixedRate = 60000, initialDelay = 60000)
    public void getPrioritizedReceipts() {
		getReceipts(Prioritet.PRIORITERT);
	}

    /**
     * Gets all receipts with normal priority from meldingsformidler for all integrations.
     * @return
     */
    @Async
    @Scheduled(fixedRate = 60000, initialDelay = 60000)
    public void getNormalReceipts() {
        getReceipts(Prioritet.NORMAL);
    }

    /**
     * Gets all receipts from meldingsformidler for all integrations and the given priority.
     * @return
     */
    public void getReceipts(Prioritet prioritet) {
        List<String> waitingClients = messageRepository.waitingClients();
        if (waitingClients.size() == 0) {
            LOGGER.info("No messages are waiting for receipt");
            return;
        }
        LOGGER.info("Started retrieving receipts");
        for (String keyPairAlias : waitingClients) {
            SikkerDigitalPostKlient postklient = postklientService.get(keyPairAlias);
            LOGGER.info("Retrieving " + prioritet + " receipts for keyPairAlias " + keyPairAlias);
            while (getReceipt(prioritet, postklient));
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
				.mpcId(getMessagePartitionChannel())
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
		Message message = messages.get(0);

        Receipt receipt = createReceipt(date, forretningsKvittering, message);
        message.getReceipts().add(receipt);
        handleKvittering(forretningsKvittering, message, receipt);
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

    private String getMessagePartitionChannel() {
        return configurationService.getConfiguration().getMessagePartitionChannel();
    }

    private Receipt createReceipt(Date date, ForretningsKvittering forretningsKvittering, Message message) {
        String xmlRequestString = stringUtil.nullIfEmpty(postKlientSoapRequest);
        String xmlResponseString = stringUtil.nullIfEmpty(postKlientSoapResponse);
        String xmlResponsePayloadString = stringUtil.nullIfEmpty(postKlientSoapResponsePayload);

        Receipt receipt = new Receipt();
        receipt.setMessage(message);
        receipt.setDate(date);
        receipt.setRequestSentDate(postKlientSoapRequestSentDate.getValue());
        receipt.setResponseReceivedDate(postKlientSoapResponseReceivedDate.getValue());
        receipt.setCompletedDate(new Date());
        receipt.setPostboxDate( Date.from(forretningsKvittering.getTidspunkt()));
        receipt.setXmlRequest(xmlRequestString);
        receipt.setXmlResponse(xmlResponseString);
        receipt.setXmlResponsePayload(xmlResponsePayloadString);
        return receipt;
    }

    private void handleKvittering(ForretningsKvittering forretningsKvittering, Message message, Receipt receipt) {
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
        } else if (forretningsKvittering instanceof MottaksKvittering) {
            receipt.setType("Mottakskvittering");
            message.setStatus(MessageStatus.WAITING_FOR_DELIVERED_RECEIPT);
        }else if (forretningsKvittering instanceof LeveringsKvittering) {
            receipt.setType("Leveringskvittering");
            if (message.isDigital() && message.getDigitalPost().getRequiresMessageOpenedReceipt()) {
                message.setStatus(MessageStatus.WAITING_FOR_OPENED_RECEIPT);
            } else {
                message.setStatus(MessageStatus.SUCCESSFULLY_SENT_MESSAGE);
            }
        } else if (forretningsKvittering instanceof VarslingFeiletKvittering) {
            VarslingFeiletKvittering varslingFeiletKvittering = (VarslingFeiletKvittering) forretningsKvittering;
            receipt.setType("Varsling feilet");
            receipt.setNotificationErrorChannel(varslingFeiletKvittering.getVarslingskanal());
            receipt.setNotificationErrorDescription(varslingFeiletKvittering.getBeskrivelse());
        } else if(forretningsKvittering instanceof ReturpostKvittering){
            receipt.setType("Returpostkvittering");
            message.setStatus(MessageStatus.DELIVER_MESSAGE_FAILED);
        } else {
            LOGGER.error("Recieved unknown receipt type " + forretningsKvittering.getClass());
        }
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
