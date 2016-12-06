package no.difi.sdp.testavsender.configuration;

import no.difi.sdp.client2.KlientKonfigurasjon;
import no.difi.sdp.client2.asice.CreateASiCE;
import no.difi.sdp.testavsender.configuration.util.CryptoUtil;
import no.difi.sdp.testavsender.configuration.util.Holder;
import no.difi.sdp.testavsender.configuration.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.*;
import org.springframework.context.support.SimpleThreadScope;
import org.springframework.core.env.Environment;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.hibernate4.HibernateExceptionTranslator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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
import org.springframework.ws.context.MessageContext;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import javax.validation.ValidatorFactory;
import java.io.StringWriter;
import java.security.KeyStore;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Configuration
@ComponentScan({"no.difi.sdp.testavsender.web", "no.difi.sdp.testavsender.service", "no.difi.sdp.testavsender.validation", "no.difi.sdp.testavsender.configuration"})
@PropertySources({
        @PropertySource(value = "classpath:configuration.properties"), // Defaults
        @PropertySource(value = "file:/etc/opt/testavsender/configuration.properties", ignoreResourceNotFound = true) // Optional overrides
})
@EnableWebMvc
@EnableJpaRepositories(basePackages = "no.difi.sdp.testavsender.repository")
@EnableTransactionManagement
@EnableScheduling
@EnableAsync(mode = AdviceMode.PROXY, proxyTargetClass = true, order = 3)
public class SdpClientConfiguration extends WebMvcConfigurerAdapter implements AsyncConfigurer {

    @Autowired
    private Environment environment;


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



    /**
     * Holds the most recently captured postKlient SOAP request for the current thread.
     *
     * @return
     */
    @Bean
    @Scope(value = "thread", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Holder<StringWriter> postKlientSoapRequest() {
        return new Holder<StringWriter>();
    }

    /**
     * Holds the most recently captured postKlient SOAP request payload for the current thread.
     *
     * @return
     */
    @Bean
    @Scope(value = "thread", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Holder<StringWriter> postKlientSoapRequestPayload() {
        return new Holder<StringWriter>();
    }

    /**
     * Holds the most recently captured postKlient SOAP request sent date for the current thread.
     *
     * @return
     */
    @Bean
    @Scope(value = "thread", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Holder<Date> postKlientSoapRequestSentDate() {
        return new Holder<Date>();
    }

    /**
     * Holds the most recently captured postKlient SOAP response received date for the current thread.
     *
     * @return
     */
    @Bean
    @Scope(value = "thread", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Holder<Date> postKlientSoapResponseReceivedDate() {
        return new Holder<Date>();
    }

    /**
     * Holds the most recently captured postKlient SOAP response for the current thread.
     *
     * @return
     */
    @Bean
    @Scope(value = "thread", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Holder<StringWriter> postKlientSoapResponse() {
        return new Holder<StringWriter>();
    }

    /**
     * Holds the most recently captured postKlient SOAP response payload for the current thread.
     *
     * @return
     */
    @Bean
    @Scope(value = "thread", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Holder<StringWriter> postKlientSoapResponsePayload() {
        return new Holder<StringWriter>();
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
    public StringUtil stringUtil() {
        return new StringUtil();
    }

    @Bean
    public ClientInterceptor postKlientSoapInterceptor() {
        return new ClientInterceptor() {

            @Override
            public void afterCompletion(MessageContext messageContext, Exception e) throws WebServiceClientException {
            }

            @Override
            public boolean handleFault(MessageContext messageContext) throws WebServiceClientException {
                return true;
            }

            @Override
            public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {
                // Captures outgoing SOAP requests
                postKlientSoapRequest().setValue(new StringWriter());
                stringUtil().transform(messageContext.getRequest(), postKlientSoapRequest().getValue());
                // Captures outgoing postklient SOAP message payloads (in practice only the message: Digitalpost)
                postKlientSoapRequestPayload().setValue(new StringWriter());
                stringUtil().transform(messageContext.getRequest().getPayloadSource(), postKlientSoapRequestPayload().getValue());
                // Captures request sent date
                postKlientSoapRequestSentDate().setValue(new Date());
                return true;
            }

            @Override
            public boolean handleResponse(MessageContext messageContext) throws WebServiceClientException {
                // Captures response received date
                postKlientSoapResponseReceivedDate().setValue(new Date());
                // Captures incoming SOAP responses
                postKlientSoapResponse().setValue(new StringWriter());
                stringUtil().transform(messageContext.getResponse(), postKlientSoapResponse().getValue());
                // Captures incoming postklient SOAP message payloads (in practice only the messages: LeveringsKvittering, ÅpningKvittering, Varslingfeilet and Feil)
                postKlientSoapResponsePayload().setValue(new StringWriter());
                stringUtil().transform(messageContext.getResponse().getPayloadSource(), postKlientSoapResponsePayload().getValue());
                return true;
            }

        };
    }

    @Bean
    public CreateASiCE createAsice() {
        return new CreateASiCE();
    }

    @Bean
    public KeyStore keyStore() {
        return cryptoUtil().loadKeystore(environment.getProperty("sdp.databehandler.keystore.type"), environment.getProperty("sdp.databehandler.keystore.file"), environment.getProperty("sdp.databehandler.keystore.password"));
    }

    @Bean
    public KeyStore keyStoreTekniskMottaker() {
        return cryptoUtil().loadKeystore(environment.getProperty("sdp.tekniskmottaker.keystore.type"), environment.getProperty("sdp.tekniskmottaker.keystore.file"), environment.getProperty("sdp.tekniskmottaker.keystore.password"));
    }

    @Bean
    public KlientKonfigurasjon klientKonfigurasjon() {

        return KlientKonfigurasjon
                .builder(environment.getProperty("sdp.meldingsformidler.url"))
                .soapInterceptors(postKlientSoapInterceptor())
                .maxConnectionPoolSize(environment.getProperty("sdp.connectionpool.size", Integer.class))
                .connectionRequestTimeout(environment.getProperty("sdp.connectionpool.connectionRequestTimeoutMs", Integer.class), TimeUnit.MILLISECONDS)
                .connectionTimeout(environment.getProperty("sdp.connectionpool.connectionTimeoutMs", Integer.class), TimeUnit.MILLISECONDS)
                .socketTimeout(environment.getProperty("sdp.connectionpool.socketTimeoutMs", Integer.class), TimeUnit.MILLISECONDS)
                .build();
    }

    @Bean
    public DataSource dataSource() {
        org.apache.tomcat.jdbc.pool.DataSource dataSource = new org.apache.tomcat.jdbc.pool.DataSource();
        dataSource.setDriverClassName(environment.getProperty("database.driver"));
        dataSource.setUrl(environment.getProperty("database.url"));
        dataSource.setUsername(environment.getProperty("database.username"));
        dataSource.setPassword(environment.getProperty("database.password"));
        // Connection pool configuration - refer to http://tomcat.apache.org/tomcat-7.0-doc/jdbc-pool.html
        dataSource.setMaxActive(environment.getProperty("database.pool.maxActive", Integer.class));
        dataSource.setMaxIdle(environment.getProperty("database.pool.maxIdle", Integer.class));
        dataSource.setMinIdle(environment.getProperty("database.pool.minIdle", Integer.class));
        dataSource.setInitialSize(environment.getProperty("database.pool.initialSize", Integer.class));
        dataSource.setMaxWait(environment.getProperty("database.pool.maxWait", Integer.class));
        dataSource.setTestOnBorrow(environment.getProperty("database.pool.testOnBorrow", Boolean.class));
        dataSource.setTestOnReturn(environment.getProperty("database.pool.testOnReturn", Boolean.class));
        dataSource.setTestWhileIdle(environment.getProperty("database.pool.testWhileIdle", Boolean.class));
        dataSource.setValidationQuery(environment.getProperty("database.pool.validationQuery"));
        dataSource.setValidationQueryTimeout(environment.getProperty("database.pool.validationQueryTimeout", Integer.class));
        dataSource.setTimeBetweenEvictionRunsMillis(environment.getProperty("database.pool.timeBetweenEvictionRunsMillis", Integer.class));
        dataSource.setMinEvictableIdleTimeMillis(environment.getProperty("database.pool.minEvictableIdleTimeMillis", Integer.class));
        dataSource.setRemoveAbandoned(environment.getProperty("database.pool.removeAbandoned", Boolean.class));
        dataSource.setRemoveAbandonedTimeout(environment.getProperty("database.pool.removeAbandonedTimeout", Integer.class));
        dataSource.setLogAbandoned(environment.getProperty("database.pool.logAbandoned", Boolean.class));
        dataSource.setValidationInterval(environment.getProperty("database.pool.validationInterval", Integer.class));
        dataSource.setFairQueue(environment.getProperty("database.pool.fairQueue", Boolean.class));
        dataSource.setAbandonWhenPercentageFull(environment.getProperty("database.pool.abandonWhenPercentageFull", Integer.class));
        dataSource.setMaxAge(environment.getProperty("database.pool.maxAge", Integer.class));
        dataSource.setLogValidationErrors(environment.getProperty("database.pool.logValidationErrors", Boolean.class));
        return dataSource;
    }

    @Bean
    public EntityManagerFactory entityManagerFactory() {
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setGenerateDdl(true);
        jpaVendorAdapter.setDatabase(environment.getProperty("database.vendor", Database.class));
        LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactory.setJpaVendorAdapter(jpaVendorAdapter);
        entityManagerFactory.setPackagesToScan("no.difi.sdp.testavsender.domain");
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

    @Override
    public Executor getAsyncExecutor() {
        return asyncExecutor();
    }

    @Bean(destroyMethod = "shutdown")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setMaxPoolSize(environment.getProperty("sdp.asyncconnectionpool.size", Integer.class));
        threadPoolTaskExecutor.setCorePoolSize(environment.getProperty("sdp.asyncconnectionpool.size", Integer.class));
        threadPoolTaskExecutor.setQueueCapacity(0); // We don't want to queue when we've reached the max capacity
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public StringWriter xmlRetrievePersonsResponse() {
        return new StringWriter();
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
    public StringWriter xmlRetrievePersonsResponsePayload() {
        return new StringWriter();
    }
}
