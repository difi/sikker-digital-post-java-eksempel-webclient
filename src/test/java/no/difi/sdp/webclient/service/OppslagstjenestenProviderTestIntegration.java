package no.difi.sdp.webclient.service;

import no.difi.begrep.Person;
import no.difi.kontaktinfo.xsd.oppslagstjeneste._16_02.HentPersonerForespoersel;
import no.difi.kontaktinfo.xsd.oppslagstjeneste._16_02.HentPersonerRespons;
import no.difi.kontaktinfo.xsd.oppslagstjeneste._16_02.Informasjonsbehov;
import no.difi.sdp.webclient.configuration.SdpClientConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.StringWriter;
import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = SdpClientConfiguration.class)
public class OppslagstjenestenProviderTestIntegration {

    @Mock
    private Environment environment;

    @InjectMocks
    private OppslagstjenestenProvider oppslagstjenestenProvider;

    private StringWriter request;
    private StringWriter response;


    @Before
    public void setup() {
        setEnvironmentVariables();

        request = new StringWriter();
        oppslagstjenestenProvider.setXmlRetrievePersonsRequest(request);
        response = new StringWriter();
        oppslagstjenestenProvider.setXmlRetrievePersonsResponse(response);
    }

    private void setEnvironmentVariables() {
        when(environment.getProperty("oppslagstjenesten.wss4jininterceptor.dec_key_alias")).thenReturn("client-alias");
        when(environment.getProperty("oppslagstjenesten.wss4joutinterceptor.sig_key_alias")).thenReturn("client-alias");
        when(environment.getProperty("oppslagstjenesten.wss4jininterceptor.dec_key_password")).thenReturn("changeit");
        when(environment.getProperty("oppslagstjenesten.wss4joutinterceptor.sig_key_password")).thenReturn("changeit");

//        when(environment.getProperty("oppslagstjenesten.url")).thenReturn("https://kontaktinfo-ws-ver1.difi.no/kontaktinfo-external/ws-v5");
        when(environment.getProperty("oppslagstjenesten.url")).thenReturn("https://kontaktinfo-ws-test1.difi.eon.no/kontaktinfo-external/ws-v5");
        when(environment.getProperty("oppslagstjenesten.wss4joutinterceptor.sig_prop_file")).thenReturn("oppslagstjenesten/client_sec.properties");
        when(environment.getProperty("oppslagstjenesten.wss4jininterceptor.sig_prop_file")).thenReturn("oppslagstjenesten/server_sec.properties");
        when(environment.getProperty("oppslagstjenesten.wss4jininterceptor.dec_prop_file")).thenReturn("oppslagstjenesten/client_sec.properties");
    }

    @Test
    public void hentPersonFromOppslagstjenestenV5() {
        HentPersonerForespoersel personas = new HentPersonerForespoersel();
        personas.getInformasjonsbehov().add(Informasjonsbehov.KONTAKTINFO);
//        String SSN = "06046000216";
        String SSN = "06045000883";
        personas.getPersonidentifikator().add(SSN);
        HentPersonerRespons hentPersonerRespons = oppslagstjenestenProvider.oppslagstjeneste().hentPersoner(personas, null);

        assertNotNull(hentPersonerRespons);
        List<Person> personList = hentPersonerRespons.getPerson();
        assertNotNull(personList);
        assertTrue(personList.size() > 0);
        assertEquals(SSN, personList.get(0).getPersonidentifikator());
        Assert.assertNotNull(personList.get(0).getKontaktinformasjon());
        verifyMessageStartsWith(request.toString(), "Outbound Message");
        verifyMessageStartsWith(response.toString(), "Inbound Message");
    }

    private void verifyMessageStartsWith(String message, String messageStart) {
        assertNotNull(message);
        assertTrue(message.startsWith(messageStart));
    }

}
