package no.difi.sdp.webclient.configuration;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import javax.validation.ValidatorFactory;

import no.difi.kontaktinfo.wsdl.oppslagstjeneste_14_05.Oppslagstjeneste1405;
import no.difi.sdp.client.KlientKonfigurasjon;
import no.difi.sdp.client.SikkerDigitalPostKlient;
import no.difi.sdp.client.domain.*;
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
