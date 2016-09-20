package no.difi.sdp.webclient.service;

import no.difi.begrep.Status;
import no.difi.kontaktinfo.xsd.oppslagstjeneste._16_02.HentPersonerForespoersel;
import no.difi.sdp.client.KlientKonfigurasjon;
import no.difi.sdp.client.SikkerDigitalPostKlient;
import no.difi.sdp.client.domain.Forsendelse;
import no.difi.sdp.client.domain.Noekkelpar;
import no.difi.sdp.client.domain.Prioritet;
import no.difi.sdp.client.domain.TekniskAvsender;
import no.difi.sdp.client.domain.digital_post.Sikkerhetsnivaa;
import no.difi.sdp.client.domain.kvittering.ForretningsKvittering;
import no.difi.sdp.client.domain.kvittering.KvitteringForespoersel;
import no.difi.sdp.webclient.BaseTest;
import no.difi.sdp.webclient.configuration.SdpClientConfiguration;
import no.difi.sdp.webclient.configuration.util.Holder;
import no.difi.sdp.webclient.configuration.util.StringUtil;
import no.difi.sdp.webclient.domain.*;
import no.difi.sdp.webclient.repository.DocumentRepository;
import no.difi.sdp.webclient.repository.MessageRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SdpClientConfiguration.class})
@WebAppConfiguration
public class MessageServiceTest extends BaseTest {

    @InjectMocks
    private MessageService service;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private PostklientService postklientService;

    @Mock
    private SikkerDigitalPostKlient sikkerDigitalPostKlient;

    @Mock
    private KlientKonfigurasjon klientKonfigurasjon;

    @Mock
    private BuilderService builderService;

    @Mock
    private Noekkelpar noekkelpar;

    @Mock
    private TekniskAvsender tekniskAvsender;

    @Mock
    private ConfigurationService configurationService;

    @Mock
    private Configuration configuration;

    @Mock
    private ForretningsKvittering forretningsKvittering;

    @Mock
    private KvitteringForespoersel kvitteringForespoersel;

    @Mock
    private HentPersonerForespoersel hentPersonerForespoersel;

    @Mock
    private X509Certificate x509Certificate;

    @Mock
    private Holder<Date> postKlientSoapRequestSentDate;

    @Mock
    private Holder<Date> postKlientSoapResponseReceivedDate;

    @Mock
    private StringUtil stringUtil;

    @Mock
    private DocumentRepository documentRepository;

    @Test
    public void test_delete_message() {

        Long id = new Long(123);

        service.deleteMessage(id);
        verify(messageRepository, times(1)).delete(id);
    }

    @Test
    public void test_delete_all_messages() {
        service.deleteAllMessages();
        verify(messageRepository, times(1)).deleteAll();
    }

    @Test
    public void test_get_document() {

        Long id = new Long(123);

        service.getDocument(id);
        verify(documentRepository, times(1)).findOne(id);
    }

    @Test
    public void test_get_receipts_when_null(){

        when(messageRepository.waitingClients()).thenReturn(new ArrayList<String>());

        service.getReceipts(Prioritet.NORMAL);
        verify(messageRepository, times(1)).waitingClients();
        verify(postklientService, never()).get(anyString());
    }

    @Test
    public void test_get_receipts(){

        ArrayList clients = new ArrayList();
        clients.add("test1");
        clients.add("test2");

        when(sikkerDigitalPostKlient.hentKvittering(kvitteringForespoersel)).thenReturn(forretningsKvittering);
        when(configuration.getMessagePartitionChannel()).thenReturn("MessagePartitionChannel");
        when(configurationService.getConfiguration()).thenReturn(configuration);
        when(messageRepository.waitingClients()).thenReturn(clients);
        when(postklientService.get("test1")).thenReturn(sikkerDigitalPostKlient);
        when(postklientService.get("test2")).thenReturn(sikkerDigitalPostKlient);

        service.getReceipts(Prioritet.NORMAL);

        verify(messageRepository, times(1)).waitingClients();
        verify(postklientService, times(2)).get(anyString());
    }

    @Test
    public void test_send_message_digital_do_not_use_oppslagstjeneste() {

        final byte[] postboxCertificate = {(byte) 0xe7, 0x4f};
        final String messagePartitionChannel = "MessagePartitionChannel";
        Message message = createDigitalMessage(postboxCertificate);

        when(configuration.getMessagePartitionChannel()).thenReturn(messagePartitionChannel);
        when(configurationService.getConfiguration()).thenReturn(configuration);

        when(postklientService.get(anyString())).thenReturn(sikkerDigitalPostKlient);
        when(postKlientSoapRequestSentDate.getValue()).thenReturn(new Date());
        when(postKlientSoapResponseReceivedDate.getValue()).thenReturn(new Date());
        when(stringUtil.nullIfEmpty(anyString())).thenReturn("");

        when(builderService.buildDigitalForsendelse(any(Message.class), eq(messagePartitionChannel))).thenReturn(Forsendelse
                .digital(null, null, null)
                .prioritet(message.getPriority())
                .spraakkode(message.getLanguageCode())
                .mpcId(messagePartitionChannel)
                .build());

        service.sendMessage(message);
        verify(builderService).buildDigitalForsendelse(message, messagePartitionChannel);
        verify(hentPersonerForespoersel, never()).getInformasjonsbehov();
        verify(hentPersonerForespoersel, never()).getPersonidentifikator();
        verify(messageRepository, times(2)).save(message);
        verify(sikkerDigitalPostKlient).send(any(Forsendelse.class));
    }

    @Test
    public void test_send_fysisk_message() {

        final byte[] postboxCertificate = {(byte) 0xe7, 0x4f};
        final String messagePartitionChannel = "MessagePartitionChannel";
        Message message = createFysiskMessage(postboxCertificate);

        when(configuration.getMessagePartitionChannel()).thenReturn(messagePartitionChannel);
        when(configurationService.getConfiguration()).thenReturn(configuration);

        when(postklientService.get(anyString())).thenReturn(sikkerDigitalPostKlient);
        when(postKlientSoapRequestSentDate.getValue()).thenReturn(new Date());
        when(postKlientSoapResponseReceivedDate.getValue()).thenReturn(new Date());
        when(stringUtil.nullIfEmpty(anyString())).thenReturn("");

        when(builderService.buildFysiskForsendelse(any(Message.class), eq(messagePartitionChannel))).thenReturn(Forsendelse
                .fysisk(null, null, null)
                .prioritet(message.getPriority())
                .spraakkode(message.getLanguageCode())
                .mpcId(messagePartitionChannel)
                .build());

        service.sendMessage(message);

        verify(builderService).buildFysiskForsendelse(message, messagePartitionChannel);
        verify(hentPersonerForespoersel, never()).getInformasjonsbehov();
        verify(hentPersonerForespoersel, never()).getPersonidentifikator();
        verify(messageRepository, times(2)).save(message);
        verify(sikkerDigitalPostKlient).send(any(Forsendelse.class));
    }

    private Message createDigitalMessage(byte[] postboxCertificate) {
        DigitalPost digitalPost = new DigitalPost("tittel");
        digitalPost.setRetrieveContactDetails(false);
        digitalPost.setContactRegisterStatus(Status.AKTIV);
        digitalPost.setPostboxAddress("PostboxAdress");
        digitalPost.setPostboxVendorOrgNumber("PostboxVendorOrgNumber");

        digitalPost.setPostboxCertificate(postboxCertificate);
        digitalPost.setInsensitiveTitle("Insensitive Tittel");
        digitalPost.setSecurityLevel(Sikkerhetsnivaa.NIVAA_3);
        digitalPost.setRequiresMessageOpenedReceipt(false);
        digitalPost.setDelayedAvailabilityDate(new Date());



        Message message = new Message(digitalPost);
        setCommonMessageAttributes(message);
        return message;
    }

    private void setCommonMessageAttributes(Message message) {
        message.setSenderOrgNumber("SenderOrgNummer");
        message.setSenderId("SenderId");
        message.setInvoiceReference("InvoiceReference");
        message.setPriority(Prioritet.NORMAL);
        message.setLanguageCode("Language");
        Document document = new Document();
        document.setContent(new byte[]{0x2d, 0x2d});
        document.setFilename("Document filename");
        document.setId(new Long(123));
        message.setDocument(document);
        Set<Document> documentSet = new HashSet<>();

        message.setAttachments(documentSet);
    }

    private Message createFysiskMessage(byte[] postboxCertificate) {
        FysiskPost fysiskPost = new FysiskPost();

        Message message = new Message(fysiskPost);
        setCommonMessageAttributes(message);
        return message;
    }

    @Autowired
	private MessageService messageService;

	@Test
    public void test_that_the_spring_context_is_successfully_initialized() {
    	messageService.getCountByStatus(); // Any method call
    }
    
}
