package no.difi.sdp.webclient.service;

import no.difi.begrep.Status;
import no.difi.kontaktinfo.xsd.oppslagstjeneste._14_05.HentPersonerForespoersel;
import no.difi.sdp.client.KlientKonfigurasjon;
import no.difi.sdp.client.SikkerDigitalPostKlient;
import no.difi.sdp.client.domain.Noekkelpar;
import no.difi.sdp.client.domain.Prioritet;
import no.difi.sdp.client.domain.TekniskAvsender;
import no.difi.sdp.client.domain.digital_post.Sikkerhetsnivaa;
import no.difi.sdp.client.domain.kvittering.ForretningsKvittering;
import no.difi.sdp.client.domain.kvittering.KvitteringForespoersel;
import no.difi.sdp.webclient.configuration.SdpClientConfiguration;
import no.difi.sdp.webclient.configuration.util.CryptoUtil;
import no.difi.sdp.webclient.configuration.util.Holder;
import no.difi.sdp.webclient.configuration.util.StringUtil;
import no.difi.sdp.webclient.domain.Configuration;
import no.difi.sdp.webclient.domain.DigitalPost;
import no.difi.sdp.webclient.domain.Document;
import no.difi.sdp.webclient.domain.Message;
import no.difi.sdp.webclient.repository.DocumentRepository;
import no.difi.sdp.webclient.repository.MessageRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SdpClientConfiguration.class})
@WebAppConfiguration
public class MessageServiceTest {

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
    private CryptoUtil cryptoUtil;

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

    @Before
    public void runBeforeEachTest(){
        MockitoAnnotations.initMocks(this);
    }

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
        verify(postklientService, never()).get(any());
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
        verify(postklientService, times(2)).get(any());
    }

    @Test
    public void test_send_message_digital_do_not_use_oppslagstjeneste() {

        when(configuration.getMessagePartitionChannel()).thenReturn("MessagePartitionChannel");
        when(configurationService.getConfiguration()).thenReturn(configuration);
        when(cryptoUtil.loadX509Certificate(any())).thenReturn(x509Certificate);
        when(postklientService.get(any())).thenReturn(sikkerDigitalPostKlient);
        when(postKlientSoapRequestSentDate.getValue()).thenReturn(new Date());
        when(postKlientSoapResponseReceivedDate.getValue()).thenReturn(new Date());
        when(stringUtil.nullIfEmpty(anyString())).thenReturn("");

        DigitalPost digitalPost = new DigitalPost("tittel");
        digitalPost.setRetrieveContactDetails(false);
        digitalPost.setContactRegisterStatus(Status.AKTIV);
        digitalPost.setPostboxAddress("PostboxAdress");
        digitalPost.setPostboxVendorOrgNumber("PostboxVendorOrgNumber");
        digitalPost.setPostboxCertificate(new byte[] {(byte) 0xe7, 0x4f});
        digitalPost.setInsensitiveTitle("Insensitive Tittel");
        digitalPost.setSecurityLevel(Sikkerhetsnivaa.NIVAA_3);
        digitalPost.setRequiresMessageOpenedReceipt(false);
        digitalPost.setDelayedAvailabilityDate(new Date());

        Document document = new Document();
        document.setContent(new byte[]{0x2d, 0x2d});
        document.setFilename("Document filename");
        document.setId(new Long(123));

        Message message = new Message(digitalPost);
        message.setSenderOrgNumber("SenderOrgNummer");
        message.setSenderId("SenderId");
        message.setInvoiceReference("InvoiceReference");
        message.setPriority(Prioritet.NORMAL);
        message.setLanguageCode("Language");
        message.setDocument(document);
        Set<Document> documentSet = new HashSet<>();

        message.setAttachments(documentSet);

        service.sendMessage(message);

        verify(hentPersonerForespoersel, never()).getInformasjonsbehov();
        verify(hentPersonerForespoersel, never()).getPersonidentifikator();
        verify(messageRepository, times(2)).save(message);
        verify(sikkerDigitalPostKlient).send(any());

    }

    @Autowired
	private MessageService messageService;

	@Test
    public void test_that_the_spring_context_is_successfully_initialized() {
    	messageService.getCountByStatus(); // Any method call
    }
    
}
