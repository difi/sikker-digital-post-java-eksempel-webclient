package no.difi.sdp.webclient.service;

import no.difi.kontaktinfo.xsd.oppslagstjeneste._16_02.HentPersonerForespoersel;
import no.difi.kontaktinfo.xsd.oppslagstjeneste._16_02.HentPersonerRespons;
import no.difi.kontaktinfo.xsd.oppslagstjeneste._16_02.Informasjonsbehov;
import no.difi.sdp.webclient.configuration.SdpClientConfiguration;
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

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes=SdpClientConfiguration.class)
public class OppslagstjenestenProviderTestIntegration {

    @Mock
    private Environment environment;

    @InjectMocks
    private OppslagstjenestenProvider oppslagstjenestenProvider;

    private StringWriter request;
    private StringWriter response;


    @Before
    public void setupEnvironmentVariables(){
        when(environment.getProperty("oppslagstjenesten.wss4jininterceptor.dec_key_alias")).thenReturn("client-alias");
        when(environment.getProperty("oppslagstjenesten.wss4joutinterceptor.sig_key_alias")).thenReturn("client-alias");
        when(environment.getProperty("oppslagstjenesten.wss4jininterceptor.dec_key_password")).thenReturn("changeit");
        when(environment.getProperty("oppslagstjenesten.wss4joutinterceptor.sig_key_password")).thenReturn("changeit");

        when(environment.getProperty("oppslagstjenesten.url")).thenReturn("https://kontaktinfo-ws-test1.difi.eon.no/kontaktinfo-external/ws-v5");
        when(environment.getProperty("oppslagstjenesten.wss4joutinterceptor.sig_prop_file")).thenReturn("oppslagstjenesten/client_sec.properties");
        when(environment.getProperty("oppslagstjenesten.wss4jininterceptor.sig_prop_file")).thenReturn("oppslagstjenesten/server_sec.properties");
        when(environment.getProperty("oppslagstjenesten.wss4jininterceptor.dec_prop_file")).thenReturn("oppslagstjenesten/client_sec.properties");

        request = new StringWriter();
        oppslagstjenestenProvider.setXmlRetrievePersonsRequest(request);
        response = new StringWriter();
        oppslagstjenestenProvider.setXmlRetrievePersonsResponse(response);
    }

    @Test
    public void hentPersonFromOppslagstjenestenV5(){
        HentPersonerForespoersel personas = new HentPersonerForespoersel();
        personas.getInformasjonsbehov().add(Informasjonsbehov.KONTAKTINFO);
        String SSN = "06046000216";
        personas.getPersonidentifikator().add(SSN);
        HentPersonerRespons hentPersonerRespons = oppslagstjenestenProvider.oppslagstjeneste().hentPersoner(personas, null);
        assertNotNull(hentPersonerRespons);
        assertNotNull(hentPersonerRespons.getPerson());
        assertTrue(hentPersonerRespons.getPerson().size()>0);
        assertEquals(SSN, hentPersonerRespons.getPerson().get(0).getPersonidentifikator());
        assertNotNull(request.toString());
        assertNotNull(response.toString());
    }

}
