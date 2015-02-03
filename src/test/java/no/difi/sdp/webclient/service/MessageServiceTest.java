package no.difi.sdp.webclient.service;

import no.difi.kontaktinfo.xsd.oppslagstjeneste._14_05.HentPersonerForespoersel;
import no.difi.sdp.client.KlientKonfigurasjon;
import no.difi.sdp.client.SikkerDigitalPostKlient;
import no.difi.sdp.client.domain.Noekkelpar;
import no.difi.sdp.client.domain.Prioritet;
import no.difi.sdp.client.domain.TekniskAvsender;
import no.difi.sdp.client.domain.kvittering.ForretningsKvittering;
import no.difi.sdp.client.domain.kvittering.KvitteringForespoersel;
import no.difi.sdp.webclient.configuration.SdpClientConfiguration;
import no.difi.sdp.webclient.domain.Configuration;
import no.difi.sdp.webclient.domain.DigitalPost;
import no.difi.sdp.webclient.domain.Message;
import no.difi.sdp.webclient.repository.MessageRepository;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;

import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SdpClientConfiguration.class})
@WebAppConfiguration
public class MessageServiceTest {

//    @Mock
//    Message message;

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
        verify(messageRepository, times(1)).getOne(id);
    }

    @Test
    public void test_get_receipts_when_null(){

        when(messageRepository.waitingClients()).thenReturn(new ArrayList<String>());

        service.getReceipts(Prioritet.NORMAL);
        verify(messageRepository, times(1)).waitingClients();
        verify(postklientService, never());
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
    @Ignore
    public void test_send_message_digital_do_not_use_oppslagstjeneste() {

        Message message = new Message();
        DigitalPost digitalPost = new DigitalPost("tittel");
        digitalPost.setRetrieveContactDetails(false);

        message.setDigitalPost(digitalPost);

        service.sendMessage(message);
        verify(hentPersonerForespoersel.getInformasjonsbehov(), never());
        verify(hentPersonerForespoersel.getPersonidentifikator(), never());
        verify(messageRepository.save(any(Message.class)));


        /*doThrow(new Exception());
        Message message = new Message();
        message.setDigitalPost(new DigitalPost("tittel"));
        service.sendMessage(message);
        verify(mockedMessageRepository).save(any(Message.class));
*/


    }



    @Autowired
	private MessageService messageService;

	@Test
    public void test_that_the_spring_context_is_successfully_initialized() {
    	messageService.getCountByStatus(); // Any method call
    }
    
}
