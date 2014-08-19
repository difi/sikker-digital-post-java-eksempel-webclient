package no.difi.sdp.webclient.service;

import no.difi.begrep.Epostadresse;
import no.difi.begrep.Kontaktinformasjon;
import no.difi.begrep.Mobiltelefonnummer;
import no.difi.begrep.Person;
import no.difi.begrep.Reservasjon;
import no.difi.begrep.SikkerDigitalPostAdresse;
import no.difi.begrep.Status;
import no.difi.kontaktinfo.wsdl.oppslagstjeneste_14_05.Oppslagstjeneste1405;
import no.difi.kontaktinfo.xsd.oppslagstjeneste._14_05.HentPersonerForespoersel;
import no.difi.kontaktinfo.xsd.oppslagstjeneste._14_05.HentPersonerRespons;
import no.difi.sdp.client.SikkerDigitalPostKlient;
import no.difi.sdp.client.domain.Dokument;
import no.difi.sdp.client.domain.Forsendelse;
import no.difi.sdp.client.domain.digital_post.EpostVarsel;
import no.difi.sdp.client.domain.digital_post.Sikkerhetsnivaa;
import no.difi.sdp.client.domain.digital_post.SmsVarsel;
import no.difi.sdp.test.AssertValue;
import no.difi.sdp.webclient.configuration.SdpClientConfiguration;
import no.difi.sdp.webclient.configuration.util.CryptoUtil;
import no.difi.sdp.webclient.domain.Document;
import no.difi.sdp.webclient.domain.Message;
import no.difi.sdp.webclient.repository.MessageRepository;
import no.difi.sdp.webclient.service.MessageService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SdpClientConfiguration.class})
@WebAppConfiguration
public class MessageServiceTest {

	@Autowired
	@InjectMocks
    MessageService messageService;

    @Mock
    MessageRepository messageRepository;
    
    @Mock
    SikkerDigitalPostKlient klient;

    @Mock
    Oppslagstjeneste1405 oppslagstjeneste;

    @Autowired
    private Environment env;

    @Mock
    CryptoUtil cryptoUtil;
    
    private String ANY_SSN = "01015572953";

    private String ANY_CERTIFICATE = 
   			"MIIDpzCCAo+gAwIBAgIEMarSTzANBgkqhkiG9w0BAQsFADCBgzEQMA4GA1UEBhMHVW5rbm93bjEQ"+
   			"MA4GA1UECBMHVW5rbm93bjEQMA4GA1UEBxMHVW5rbm93bjENMAsGA1UEChMERGlmaTEQMA4GA1UE"+
   			"CxMHVW5rbm93bjEqMCgGA1UEAxMhT3Bwc2xhZ3N0amVuZXN0ZW4gVGVzdCBFbmNyeXB0aW9uMB4X"+
   			"DTE0MDUwNzA2MjIwNFoXDTI0MDUwNDA2MjIwNFowgYMxEDAOBgNVBAYTB1Vua25vd24xEDAOBgNV"+
   			"BAgTB1Vua25vd24xEDAOBgNVBAcTB1Vua25vd24xDTALBgNVBAoTBERpZmkxEDAOBgNVBAsTB1Vu"+
   			"a25vd24xKjAoBgNVBAMTIU9wcHNsYWdzdGplbmVzdGVuIFRlc3QgRW5jcnlwdGlvbjCCASIwDQYJ"+
   			"KoZIhvcNAQEBBQADggEPADCCAQoCggEBAKMTUNJz+4NGkmfatUCBSHTPqWBJ4XDDIMFz1lfnGzX7"+
   			"jxjJgYJjKPulkSSmGGzIOgJEweUTahFdDUL3gNKg0uTbAomRVbQmll2WTpcZub0XVMDtJdzGny3c"+
   			"MXsH1OH0vKHR6E0H5C/PB/oEGtup07NxAy81R+z9U9GUpH9eVStSoNtHsOnNWKNtRIFKFn08cjHi"+
   			"KAiLlCGhdudrS+yJExRB4USGbaixJ6bU7HR/IkEvtW33ovh2PeLCc6IllusIVyyOxMWT+R8cE6X6"+
   			"8YE3dZYbSaxVXE+OfHBSGR0EApEq/fRco9ES/9VWjW0Pq+YXu59WGt+qEbiTuR9vgnWzP9MCAwEA"+
   			"AaMhMB8wHQYDVR0OBBYEFMKAMRGkXJjYOd0FRUyV5gxFlUO6MA0GCSqGSIb3DQEBCwUAA4IBAQBr"+
   			"Il1A7X/MYUVy07wXgsUDGu9ObVnUkIaPVm+Z+quTIFpUD0PF17ip8Z0sX+PpH70js4SyCbrp9em0"+
   			"2bNPI2Ba2jjEAfnsoqPWUGnYpafmNH0opZjUPYPKIJEqaTo6yyltEblqVE/KLcEehifyh1UXcxIy"+
   			"MLfSMFQ8LoD7GK5dG7hKyrTXXSz6+AwDjN4oN7FDb/lqBJ4BWIb2wWT8b8iLxahUmsseDPMeyhAC"+
   			"uo753OKh6eAolYs1YymjyrhA7/i3jlkDJa0i+4ha8H4znty5yNiOM3g4l8+RZqIhLmem9TtKCCJV"+
   			"dkjEV+MeMGDZNg2ZBWbVFAs7pVixx+hPwZNb";

    private String ANY_EMAIL = "test@difi.local";
    private String ANY_MOBILE = "98765432";
    
    private Mobiltelefonnummer buildMobiltelefonnummer() {
    	Mobiltelefonnummer m = new Mobiltelefonnummer();
    	m.setValue(ANY_MOBILE);
    	m.setSistOppdatert(null);
    	m.setSistVerifisert(null);
    	return m;
    }
    
    private Epostadresse buildEpostadresse() {
    	Epostadresse e = new Epostadresse();
    	e.setValue(ANY_EMAIL);
    	e.setSistOppdatert(null);
    	e.setSistVerifisert(null);
    	return e;
    }
    
    private Kontaktinformasjon buildKontaktinformasjon() {
    	Kontaktinformasjon k = new Kontaktinformasjon();
    	k.setEpostadresse(buildEpostadresse());
    	k.setMobiltelefonnummer(buildMobiltelefonnummer());
    	return k;
    }
    
    private SikkerDigitalPostAdresse buildSikkerDigitalPostAdresse() {
    	SikkerDigitalPostAdresse s = new SikkerDigitalPostAdresse();
    	s.setPostkasseadresse("postadresse-abc-123");
    	s.setPostkasseleverandoerAdresse("987654321");
    	return s;
    }
    
    private Person buildPerson(String ssn) {
    	Person p = new Person();
    	p.setBeskrivelse(null);
    	p.setKontaktinformasjon(buildKontaktinformasjon());
    	p.setPersonidentifikator(ssn);
    	p.setReservasjon(Reservasjon.NEI);
    	p.setSikkerDigitalPostAdresse(buildSikkerDigitalPostAdresse());
    	p.setStatus(Status.AKTIV);
    	p.setX509Sertifikat(ANY_CERTIFICATE.getBytes());
    	return p;
    }
    
    private HentPersonerRespons buildHentPersonerRespons(String ssn) {
    	HentPersonerRespons r = new HentPersonerRespons();
    	r.getPerson().add(buildPerson(ssn));
    	return r;
    }
    
    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
        when(oppslagstjeneste.hentPersoner(any(HentPersonerForespoersel.class))).thenReturn(buildHentPersonerRespons(ANY_SSN));
    }

    @Test
    public void should_construct_a_forsendelse_with_ssn_and_tittel(){
        final Message message = new Message();
        message.setInsensitiveTitle("ikke-sensitiv tittel");
        message.setSsn(ANY_SSN);
        Document document = new Document();
        document.setTitle("sensitiv tittel");
        document.setContent("attachment".getBytes());
        document.setFilename("attachment.txt");
        document.setMimetype("text/plain");
        message.setDocument(document);
        message.setAttachments(new HashSet<Document>());
        message.setSecurityLevel(Sikkerhetsnivaa.NIVAA_3);
        message.setLanguageCode("NO");
        messageService.sendMessage(message, true);

        verify(klient).send(argThat(new AssertValue<Forsendelse>() {
            @Override
            public void asserts(Forsendelse value) {
                assertEquals(message.getInsensitiveTitle(), (String) privateValue(value.getDigitalPost(), "ikkeSensitivTittel"));
                assertEquals(message.getSsn(), (String) privateValue(value.getDigitalPost(), "mottaker.personidentifikator"));
            }
        }));
    }

    @Test
    public void should_construct_a_frosendelse_with_document() {
    	final Message message = new Message();
    	message.setInsensitiveTitle("ikke-sensitiv tittel");
        message.setSsn(ANY_SSN);
    	Document document = new Document();
        document.setTitle("sensitiv tittel");
        document.setContent("attachment".getBytes());
        document.setFilename("filnavn.txt");
        document.setMimetype("application/text");
        message.setDocument(document);
        message.setSecurityLevel(Sikkerhetsnivaa.NIVAA_3);
        message.setAttachments(new HashSet<Document>());
        message.setLanguageCode("NO");
        messageService.sendMessage(message, true);

        verify(klient).send(argThat(new AssertValue<Forsendelse>() {
            @Override
            public void asserts(Forsendelse value) throws IOException {
                //Problemer med mochito elns. hvor denne metoden blir kallt
                //to ganger. Selv om send bare kalles en gang.
                if(seen != null && value.equals(seen))
                    return;

                seen = value;

                Dokument dok = (Dokument) privateValue(value.getDokumentpakke(), "hoveddokument");
                assertEquals(message.getDocument().getMimetype(), privateValue(dok, "mimeType"));
                assertEquals(message.getDocument().getFilename(), privateValue(dok, "filnavn"));
                assertTrue(Arrays.equals(message.getDocument().getContent(), (byte[]) privateValue(dok, "dokument")));
                assertEquals(message.getDocument().getTitle(), (String) privateValue(dok, "tittel"));
                
            }

            public Forsendelse seen;
        }));

    }

    @Test
    public void should_construct_a_frosendelse_with_notification() {
    	final Message message = new Message();
    	message.setSsn(ANY_SSN);
        message.setInsensitiveTitle("ikke-sensitiv tittel");
        Document document = new Document();
        document.setTitle("sensitiv tittel");
        document.setContent("content".getBytes());
        document.setFilename("filnavn.txt");
        document.setMimetype("application/text");
        message.setDocument(document);
        message.setAttachments(new HashSet<Document>());
        
        message.setSecurityLevel(Sikkerhetsnivaa.NIVAA_3);
        message.setEmailNotification("emailNotification");
        message.setMobileNotification("mobileNotification");
        message.setLanguageCode("NO");
        
        messageService.sendMessage(message, true);

        verify(klient).send(argThat(new AssertValue<Forsendelse>() {
            @Override
            public void asserts(Forsendelse value) throws IOException {
                //Problemer med mochito elns. hvor denne metoden blir kallt
                //to ganger. Selv om send bare kalles en gang.
                if(seen != null && value.equals(seen))
                    return;

                seen = value;

                EpostVarsel epostVarsel = (EpostVarsel) privateValue(value.getDigitalPost(), "epostVarsel");
                SmsVarsel smsVarsel = (SmsVarsel) privateValue(value.getDigitalPost(), "smsVarsel");
                
                assertEquals(message.getEmailNotification(), privateValue(epostVarsel, "varslingsTekst"));
                assertEquals(message.getEmail(), privateValue(epostVarsel, "epostadresse"));

                assertEquals(message.getMobileNotification(), privateValue(smsVarsel, "varslingsTekst"));
                assertEquals(message.getMobile(), privateValue(smsVarsel, "mobilnummer"));
            }

            public Forsendelse seen;
        }));

    }
    
}
