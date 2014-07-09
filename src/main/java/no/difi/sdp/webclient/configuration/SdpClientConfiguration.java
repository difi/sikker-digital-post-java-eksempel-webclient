package no.difi.sdp.webclient.configuration;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import javax.validation.ValidatorFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import no.difi.kontaktinfo.wsdl.oppslagstjeneste_14_05.Oppslagstjeneste1405;
import no.difi.sdp.client.KlientKonfigurasjon;
import no.difi.sdp.client.SikkerDigitalPostKlient;
import no.difi.sdp.client.domain.*;
import no.difi.sdp.webclient.configuration.util.ClearAfterReadStringWriter;
import no.difi.sdp.webclient.configuration.util.ClientKeystorePasswordCallbackHandler;
import no.difi.sdp.webclient.configuration.util.CryptoUtil;
import no.difi.sdp.webclient.service.MessageService;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.protocol.HttpContext;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.support.SimpleThreadScope;
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
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;

@Configuration
@ComponentScan( { "no.difi.sdp.webclient.web", "no.difi.sdp.webclient.service" } )
@PropertySource( {"classpath:configuration.properties"} )
@EnableWebMvc
@EnableJpaRepositories(basePackages = "no.difi.sdp.webclient.repository")
@EnableTransactionManagement
@EnableScheduling
public class SdpClientConfiguration extends WebMvcConfigurerAdapter {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(SdpClientConfiguration.class);
	
	@Autowired
	private Environment environment;
	
	@Autowired
	private MessageService messageService;
	
	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}

	@Bean
	public static CustomScopeConfigurer customScopeConfigurer() {
		CustomScopeConfigurer customScopeConfigurer = new CustomScopeConfigurer();
		Map<String, Object> scopes = new HashMap<String, Object>();
		scopes.put("thread", new SimpleThreadScope());
		customScopeConfigurer.setScopes(scopes);
		return customScopeConfigurer;
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
	@Scope(value = "thread", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public StringWriter xmlRetrieveMessageReceiptRequest() {
		// Uses the thread scope because this message is retrieved async (where there is no request scope)
		// Uses ClearAfterReadStringWriter because many messages of this type will be retrieved on the same thread (because of polling for several messages from the same thread, and also because of thread pooling with thread reuse)
		// There are no concurrency issues with using ClearAfterReadStringWriter because it is scoped to the thread and everything is running sequentially within the thread
		return new ClearAfterReadStringWriter(); 
	}
	
	@Bean
	@Scope(value = "thread", proxyMode = ScopedProxyMode.TARGET_CLASS) // Uses the thread scope because this message is retrieved async
	public StringWriter xmlRetrieveMessageReceiptResponse() {
		// Uses the thread scope because this message is retrieved async (where there is no request scope)
		// Uses ClearAfterReadStringWriter because many messages of this type will be retrieved on the same thread (because of polling for several messages from the same thread, and also because of thread pooling with thread reuse)
		// There are no concurrency issues with using ClearAfterReadStringWriter because it is scoped to the thread and everything is running sequentially within the thread
		return new ClearAfterReadStringWriter();
	}
	
	@Bean
	@Scope(value = "thread", proxyMode = ScopedProxyMode.TARGET_CLASS) // Uses the thread scope because this message is retrieved async
	public StringWriter xmlRetrieveMessageReceiptResponsePayload() {
		// Uses the thread scope because this message is retrieved async (where there is no request scope)
		// Uses ClearAfterReadStringWriter because many messages of this type will be retrieved on the same thread (because of polling for several messages from the same thread, and also because of thread pooling with thread reuse)
		// There are no concurrency issues with using ClearAfterReadStringWriter because it is scoped to the thread and everything is running sequentially within the thread
		return new ClearAfterReadStringWriter();
	}
	
	@Bean
	public LoggingInInterceptor oppslagstjenesteLoggingInInterceptor() {
		LoggingInInterceptor loggingInInterceptor = new LoggingInInterceptor();
		loggingInInterceptor.setPrintWriter(new PrintWriter(xmlRetrievePersonsResponse()));
		loggingInInterceptor.setPrettyLogging(true);
		return loggingInInterceptor;
	}
	
	@Bean
	public LoggingOutInterceptor oppslagstjenesteLoggingOutInterceptor() {
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
    	jaxWsProxyFactoryBean.getInInterceptors().add(oppslagstjenesteLoggingInInterceptor());
    	jaxWsProxyFactoryBean.getOutInterceptors().add(oppslagstjenesteLoggingOutInterceptor());
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
    public ClientInterceptor postKlientSoapInterceptor() {
    	return new ClientInterceptor() {

			@Override
			public void afterCompletion(org.springframework.ws.context.MessageContext messageContext, Exception e) throws WebServiceClientException {
			}

			@Override
			public boolean handleFault(org.springframework.ws.context.MessageContext messageContext) throws WebServiceClientException {
				return true;
			}

			@Override
			public boolean handleRequest(org.springframework.ws.context.MessageContext messageContext) throws WebServiceClientException {
				// Intercepts outgoing postklient SOAP messages containing a payload (in practice only the message: Digitalpost)
				transform(messageContext.getRequest().getPayloadSource(), xmlSendMessageRequestPayload());
				return true;
			}

			@Override
			public boolean handleResponse(org.springframework.ws.context.MessageContext messageContext) throws WebServiceClientException {
				// Intercepts incoming postklient SOAP messages containing a payload (in practice only the messages: LeveringsKvittering, ÅpningKvittering, Varslingfeilet and Feil)
				transform(messageContext.getResponse().getPayloadSource(), xmlRetrieveMessageReceiptResponsePayload());
				return true;
			}
    		
    	};
    }
    
    private void transform(Source source, StringWriter stringWriter) {
    	if (source != null) {
    		try {
	    		Transformer transformer = TransformerFactory.newInstance().newTransformer();
	    		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	    		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
	    		transformer.transform(source, new StreamResult(stringWriter));
    		} catch (Exception e) {
    			LOGGER.error("Failed capturing message", e);
    		}
    	}
    }
    
    @Bean
    public HttpRequestInterceptor postKlientHttpRequestInterceptor() {
    	return new HttpRequestInterceptor() {
			
			@Override
			public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
				// TODO save full http request to xmlSendMessageRequest() 
			}
		};
    }
    
    @Bean
    public HttpResponseInterceptor postKlientHttpResponseInterceptor() {
    	return new HttpResponseInterceptor() {
			
			@Override
			public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
				// TODO save full http response to xmlSendMessageRequest() 
			}
		};
    }
    
    @Bean
    public SikkerDigitalPostKlient postKlient() {
    	KeyStore keyStore = cryptoUtil().loadKeystore(environment.getProperty("meldingsformidler.avsender.keystore.type"), environment.getProperty("meldingsformidler.avsender.keystore.file"), environment.getProperty("meldingsformidler.avsender.keystore.password"));
    	Noekkelpar noekkelpar = Noekkelpar.fraKeyStore(keyStore, environment.getProperty("meldingsformidler.avsender.key.alias"), environment.getProperty("meldingsformidler.avsender.key.password"));
        TekniskAvsender avsender = TekniskAvsender.builder(environment.getProperty("meldingsformidler.avsender.organisasjonsnummer"), noekkelpar).build();
        KlientKonfigurasjon klientKonfigurasjon = KlientKonfigurasjon.builder()
        		.meldingsformidlerRoot(environment.getProperty("meldingsformidler.url"))
        		.soapInterceptors(postKlientSoapInterceptor())
        		.httpRequestInterceptors(postKlientHttpRequestInterceptor())
        		.httpResponseInterceptors(postKlientHttpResponseInterceptor())
        		.build();
        SikkerDigitalPostKlient postklient = new SikkerDigitalPostKlient(avsender, klientKonfigurasjon);
        return postklient;
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
    public void retrieveReceiptPeriodically() {
    	// Note that this scheduled task will run concurrently if it runs for more than 10 seconds
    	while (messageService.getReceipt()) {
    		 // Continues until there are no available receipts
    	}
    }

}
