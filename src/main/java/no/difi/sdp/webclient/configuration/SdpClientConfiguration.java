package no.difi.sdp.webclient.configuration;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import javax.validation.ValidatorFactory;

import no.difi.kontaktinfo.wsdl.oppslagstjeneste_14_05.Oppslagstjeneste1405;
import no.difi.sdp.client.KlientKonfigurasjon;
import no.difi.sdp.client.SikkerDigitalPostKlient;
import no.difi.sdp.client.domain.*;
import no.difi.sdp.client.domain.digital_post.DigitalPost;
import no.difi.sdp.client.domain.kvittering.ForretningsKvittering;
import no.difi.sdp.client.domain.kvittering.KvitteringForespoersel;
import no.difi.sdp.webclient.configuration.util.ClientKeystorePasswordCallbackHandler;
import no.difi.sdp.webclient.configuration.util.CryptoUtil;
import no.difi.sdp.webclient.configuration.util.MessageContext;
import no.difi.sdp.webclient.service.MessageService;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.hibernate4.HibernateExceptionTranslator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

@Configuration
@ComponentScan( { "no.difi.sdp.webclient.web", "no.difi.sdp.webclient.service" } )
@PropertySource( {"classpath:configuration.properties"} )
@EnableWebMvc
@EnableJpaRepositories(basePackages = "no.difi.sdp.webclient.repository")
@EnableTransactionManagement
@EnableScheduling
public class SdpClientConfiguration extends WebMvcConfigurerAdapter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SdpClientConfiguration.class);
	
	@Autowired
	private Environment environment;
	
	@Autowired
	private MessageService messageService;
	
	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}

	@Bean
	public MessageContext messageContext() {
		return new MessageContext();
	}

	@Bean
	@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
	public StringWriter xmlRetrievePersonsRequest() {
		return new StringWriter();
	}
	
	@Bean
	@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
	public StringWriter xmlRetrievePersonsRequestPayload() {
		return new StringWriter();
	}
	
	@Bean
	@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
	public StringWriter xmlRetrievePersonsResponse() {
		return new StringWriter();
	}
	
	@Bean
	@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
	public StringWriter xmlRetrievePersonsResponsePayload() {
		return new StringWriter();
	}
	
	@Bean
	@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
	public StringWriter xmlSendMessageRequest() {
		return new StringWriter();
	}
	
	@Bean
	@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
	public StringWriter xmlSendMessageRequestPayload() {
		return new StringWriter();
	}
	
	@Bean
	@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
	public StringWriter xmlSendMessageResponse() {
		return new StringWriter();
	}
	
	@Bean
	@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
	public StringWriter xmlRetrieveMessageRecieptRequest() {
		return new StringWriter();
	}
	
	@Bean
	@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
	public StringWriter xmlRetrieveMessageRecieptResponse() {
		return new StringWriter();
	}
	
	@Bean
	@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
	public StringWriter xmlRetrieveMessageRecieptResponsePayload() {
		return new StringWriter();
	}
	
	@Bean
	public LoggingInInterceptor loggingInInterceptor() {
		LoggingInInterceptor loggingInInterceptor = new LoggingInInterceptor();
		loggingInInterceptor.setPrintWriter(new PrintWriter(xmlRetrievePersonsResponse()));
		loggingInInterceptor.setPrettyLogging(true);
		return loggingInInterceptor;
	}
	
	@Bean
	public LoggingOutInterceptor loggingOutInterceptor() {
		LoggingOutInterceptor loggingOutInterceptor = new LoggingOutInterceptor();
		loggingOutInterceptor.setPrintWriter(new PrintWriter(xmlRetrievePersonsRequest()));
		loggingOutInterceptor.setPrettyLogging(true);
		return loggingOutInterceptor;
	}
	
	@Bean
	public Oppslagstjeneste1405 oppslagstjeneste() {
		ClientKeystorePasswordCallbackHandler.addPrivateKey(environment.getProperty("oppslagstjenesten.wss4jininterceptor.dec_key_alias"), environment.getProperty("oppslagstjenesten.wss4jininterceptor.dec_key_password"));
		ClientKeystorePasswordCallbackHandler.addPrivateKey(environment.getProperty("oppslagstjenesten.wss4joutinterceptor.sig_key_alias"), environment.getProperty("oppslagstjenesten.wss4joutinterceptor.sig_key_password"));
		Map<String, Object> inputProperties = new HashMap<String, Object>();
    	inputProperties.put(WSHandlerConstants.ACTION, WSHandlerConstants.SIGNATURE + " " + WSHandlerConstants.TIMESTAMP + " " + WSHandlerConstants.ENCRYPT);
    	inputProperties.put(WSHandlerConstants.PW_CALLBACK_CLASS, ClientKeystorePasswordCallbackHandler.class.getName());
    	inputProperties.put(WSHandlerConstants.SIG_PROP_FILE, environment.getProperty("oppslagstjenesten.wss4jininterceptor.sig_prop_file"));
    	inputProperties.put(WSHandlerConstants.DEC_PROP_FILE, environment.getProperty("oppslagstjenesten.wss4jininterceptor.dec_prop_file"));
    	inputProperties.put(WSHandlerConstants.ALLOW_RSA15_KEY_TRANSPORT_ALGORITHM, "true");
    	Map<String, Object> outputProperties = new HashMap<String, Object>();
        outputProperties.put(WSHandlerConstants.ACTION, WSHandlerConstants.SIGNATURE + " " + WSHandlerConstants.TIMESTAMP);
        outputProperties.put(WSHandlerConstants.USER, environment.getProperty("oppslagstjenesten.wss4joutinterceptor.sig_key_alias"));
        outputProperties.put(WSHandlerConstants.PW_CALLBACK_CLASS, ClientKeystorePasswordCallbackHandler.class.getName());
        outputProperties.put(WSHandlerConstants.SIG_PROP_FILE, environment.getProperty("oppslagstjenesten.wss4joutinterceptor.sig_prop_file"));
        JaxWsProxyFactoryBean jaxWsProxyFactoryBean = new JaxWsProxyFactoryBean();
    	jaxWsProxyFactoryBean.setServiceClass(Oppslagstjeneste1405.class);
    	jaxWsProxyFactoryBean.setAddress(environment.getProperty("oppslagstjenesten.url"));
    	jaxWsProxyFactoryBean.getInInterceptors().add(new WSS4JInInterceptor(inputProperties));
    	jaxWsProxyFactoryBean.getInInterceptors().add(loggingInInterceptor());
    	jaxWsProxyFactoryBean.getOutInterceptors().add(loggingOutInterceptor());
    	jaxWsProxyFactoryBean.getOutInterceptors().add(new WSS4JOutInterceptor(outputProperties));
    	return jaxWsProxyFactoryBean.create(Oppslagstjeneste1405.class);
    }
	
    @Bean
    public InternalResourceViewResolver urlBasedViewResolver() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setViewClass(JstlView.class);
        viewResolver.setPrefix("/WEB-INF/");
        viewResolver.setSuffix(".jspx");
        return viewResolver;
    }

    @Bean
    public MultipartResolver multipartResolver() {
        MultipartResolver multipartResolver = new CommonsMultipartResolver();
        return multipartResolver;
    }

    @Bean
    public ValidatorFactory validator() {
        ValidatorFactory validatorFactory = new LocalValidatorFactoryBean();
        return validatorFactory;
    }

    @Bean
    public CryptoUtil cryptoUtil() {
    	return new CryptoUtil();
    }

    @Bean
    public SikkerDigitalPostKlient postKlient() {
    	KeyStore keyStore = cryptoUtil().loadKeystore(environment.getProperty("meldingsformidler.avsender.keystore.type"), environment.getProperty("meldingsformidler.avsender.keystore.file"), environment.getProperty("meldingsformidler.avsender.keystore.password"));
    	Noekkelpar noekkelpar = Noekkelpar.fraKeyStore(keyStore, environment.getProperty("meldingsformidler.avsender.key.alias"), environment.getProperty("meldingsformidler.avsender.key.password"));
        TekniskAvsender avsender = TekniskAvsender.builder(environment.getProperty("meldingsformidler.avsender.organisasjonsnummer"), noekkelpar).build();
        KlientKonfigurasjon klientKonfigurasjon = KlientKonfigurasjon.builder().meldingsformidlerRoot(environment.getProperty("meldingsformidler.url")).build();
        SikkerDigitalPostKlient postklient = new SikkerDigitalPostKlient(avsender, klientKonfigurasjon);
        return postklient;
    }

    public static void main(String[] args) throws KeyStoreException, InterruptedException {
    	// minimal example for sending post adn recieving reciept
    	CryptoUtil cryptoUtil = new CryptoUtil();
    	KeyStore keyStore = cryptoUtil.loadKeystore("JKS", "avsender.jks", "oBr8YZuZsbic4gpP");
    	Noekkelpar noekkelpar = Noekkelpar.fraKeyStore(keyStore, "avsender", "oBr8YZuZsbic4gpP");
    	LOGGER.info("Avsender X509 certificate subject DN " + noekkelpar.getSertifikat().getX509Certificate().getSubjectDN());
    	LOGGER.info("Avsender X509 certificate issuer DN " + noekkelpar.getSertifikat().getX509Certificate().getIssuerDN());
        TekniskAvsender avsender = TekniskAvsender.builder("991825827", noekkelpar).build();
        KlientKonfigurasjon klientKonfigurasjon = KlientKonfigurasjon.builder().meldingsformidlerRoot("https://qaoffentlig.meldingsformidler.digipost.no/api/").build();
        SikkerDigitalPostKlient postklient = new SikkerDigitalPostKlient(avsender, klientKonfigurasjon);
        Sertifikat mottakerSertifikat = Sertifikat.fraBase64X509String("MIIE7jCCA9agAwIBAgIKGBZrmEgzTHzeJjANBgkqhkiG9w0BAQsFADBRMQswCQYDVQQGEwJOTzEdMBsGA1UECgwUQnV5cGFzcyBBUy05ODMxNjMzMjcxIzAhBgNVBAMMGkJ1eXBhc3MgQ2xhc3MgMyBUZXN0NCBDQSAzMB4XDTE0MDQyNDEyMzA1MVoXDTE3MDQyNDIxNTkwMFowVTELMAkGA1UEBhMCTk8xGDAWBgNVBAoMD1BPU1RFTiBOT1JHRSBBUzEYMBYGA1UEAwwPUE9TVEVOIE5PUkdFIEFTMRIwEAYDVQQFEwk5ODQ2NjExODUwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCLCxU4oBhtGmJxXZWbdWdzO2uA3eRNW/kPdddL1HYl1iXLV/g+H2Q0ELadWLggkS+1kOd8/jKxEN++biMmmDqqCWbzNdmEd1j4lctSlH6M7tt0ywmXIYdZMz5kxcLAMNXsaqnPdikI9uPJZQEL3Kc8hXhXISvpzP7gYOvKHg41uCxu1xCZQOM6pTlNbxemBYqvES4fRh2xvB9aMjwkB4Nz8jrIsyoPI89i05OmGMkI5BPZt8NTa40Yf3yU+SQECW0GWalB5cxaTMeB01tqslUzBJPV3cQx+AhtQG4hkOhQnAMDJramSPVtwbEnqOjQ+lyNmg5GQ4FJO02ApKJTZDTHAgMBAAGjggHCMIIBvjAJBgNVHRMEAjAAMB8GA1UdIwQYMBaAFD+u9XgLkqNwIDVfWvr3JKBSAfBBMB0GA1UdDgQWBBQ1gsJfVC7KYGiWVLP7ZwzppyVYTTAOBgNVHQ8BAf8EBAMCBLAwFgYDVR0gBA8wDTALBglghEIBGgEAAwIwgbsGA1UdHwSBszCBsDA3oDWgM4YxaHR0cDovL2NybC50ZXN0NC5idXlwYXNzLm5vL2NybC9CUENsYXNzM1Q0Q0EzLmNybDB1oHOgcYZvbGRhcDovL2xkYXAudGVzdDQuYnV5cGFzcy5uby9kYz1CdXlwYXNzLGRjPU5PLENOPUJ1eXBhc3MlMjBDbGFzcyUyMDMlMjBUZXN0NCUyMENBJTIwMz9jZXJ0aWZpY2F0ZVJldm9jYXRpb25MaXN0MIGKBggrBgEFBQcBAQR+MHwwOwYIKwYBBQUHMAGGL2h0dHA6Ly9vY3NwLnRlc3Q0LmJ1eXBhc3Mubm8vb2NzcC9CUENsYXNzM1Q0Q0EzMD0GCCsGAQUFBzAChjFodHRwOi8vY3J0LnRlc3Q0LmJ1eXBhc3Mubm8vY3J0L0JQQ2xhc3MzVDRDQTMuY2VyMA0GCSqGSIb3DQEBCwUAA4IBAQCe67UOZ/VSwcH2ov1cOSaWslL7JNfqhyNZWGpfgX1c0Gh+KkO3eVkMSozpgX6M4eeWBWJGELMiVN1LhNaGxBU9TBMdeQ3SqK219W6DXRJ2ycBtaVwQ26V5tWKRN4UlRovYYiY+nMLx9VrLOD4uoP6fm9GE5Fj0vSMMPvOEXi0NsN+8MUm3HWoBeUCLyFpe7/EPsS/Wud5bb0as/E2zIztRodxfNsoiXNvWaP2ZiPWFunIjK1H/8EcktEW1paiPd8AZek/QQoG0MKPfPIJuqH+WJU3a8J8epMDyVfaek+4+l9XOeKwVXNSOP/JSwgpOJNzTdaDOM+uVuk75n2191Fd7");
        LOGGER.info("Mottaker X509 certificate subject DN " + mottakerSertifikat.getX509Certificate().getSubjectDN());
        LOGGER.info("Mottaker X509 certificate issuer DN " + mottakerSertifikat.getX509Certificate().getIssuerDN());
        Mottaker mottaker = Mottaker.builder("04036125433", "ove.jonsen#7U2C", mottakerSertifikat, "984661185").build();
        DigitalPost digitalPost = DigitalPost.builder(mottaker, "ikkeSensitivTittel 2").build();
        Dokument hoveddokument = Dokument.builder("Tittel 3 hei", new File("C:\\Users\\kons-she\\Documents\\1-Test-PDF.pdf")).mimeType("application/pdf").build();
        Dokumentpakke dokumentpakke = Dokumentpakke.builder(hoveddokument).build();
        Behandlingsansvarlig behandlingsansvarlig = Behandlingsansvarlig.builder("").avsenderIdentifikator("991825827").fakturaReferanse("fakturaref").build();
        Forsendelse forsendelse = Forsendelse.digital(behandlingsansvarlig, digitalPost, dokumentpakke).build();
        postklient.send(forsendelse);
        LOGGER.info("venter 10 sek");
        for (int i = 0; i < 6; i++) {
        	Thread.sleep(1000 * 10);
        	ForretningsKvittering forretningsKvittering = postklient.hentKvittering(KvitteringForespoersel.builder(Prioritet.PRIORITERT).build());
        	if (forretningsKvittering == null) {
        		LOGGER.info("ingen kvittering, venter 10 sek");
        	} else {
        		LOGGER.info("er levert: " + forretningsKvittering.applikasjonsKvittering.getKvittering().erLevertTilPostkasse());
        		return;
        	}
        	
        }
        LOGGER.info("gir opp");
    }
    
    @Bean
    public DataSource dataSource() {
        EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
        EmbeddedDatabase embeddedDatabase = builder.setType(EmbeddedDatabaseType.H2).build();
        return embeddedDatabase;
    }

    @Bean
    public EntityManagerFactory entityManagerFactory() {
    	HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setGenerateDdl(true);
        LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactory.setJpaVendorAdapter(jpaVendorAdapter);
        entityManagerFactory.setPackagesToScan("no.difi.sdp.webclient.domain");
        entityManagerFactory.setDataSource(dataSource());
        entityManagerFactory.afterPropertiesSet();
        return entityManagerFactory.getObject();
    }

    @Bean
    public PersistenceExceptionTranslator persistenceExceptionTranslator() {
        return new HibernateExceptionTranslator();
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setEntityManagerFactory(entityManagerFactory());
        return jpaTransactionManager;
    }
    
    @Scheduled(fixedRate = 10000, initialDelay = 10000)
    public void retrieveRecieptPeriodically() {
    	// Note that this scheduled task will run concurrently if it runs for more than 10 seconds
    	while (messageService.getReceipt()) {
    		 // Continues until there are no available reciepts
    	}
    }
    
}
