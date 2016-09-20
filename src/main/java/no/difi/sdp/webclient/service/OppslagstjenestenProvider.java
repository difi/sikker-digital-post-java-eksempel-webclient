package no.difi.sdp.webclient.service;

import no.difi.kontaktinfo.wsdl.oppslagstjeneste_16_02.Oppslagstjeneste1602;
import no.difi.sdp.webclient.configuration.util.ClientKeystorePasswordCallbackHandler;
import org.apache.cxf.interceptor.InterceptorProvider;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 */
@Component
public class OppslagstjenestenProvider {

    @Autowired
    Environment environment;

    @Autowired
    private StringWriter xmlRetrievePersonsRequest;

    @Autowired
    private StringWriter xmlRetrievePersonsResponse;

    @Bean
    public Oppslagstjeneste1602 oppslagstjeneste() {
        ClientKeystorePasswordCallbackHandler.addPrivateKey(environment.getProperty("oppslagstjenesten.wss4jininterceptor.dec_key_alias"), environment.getProperty("oppslagstjenesten.wss4jininterceptor.dec_key_password"));
        ClientKeystorePasswordCallbackHandler.addPrivateKey(environment.getProperty("oppslagstjenesten.wss4joutinterceptor.sig_key_alias"), environment.getProperty("oppslagstjenesten.wss4joutinterceptor.sig_key_password"));

        JaxWsProxyFactoryBean jaxWsProxyFactoryBean = new JaxWsProxyFactoryBean();
        jaxWsProxyFactoryBean.setServiceClass(Oppslagstjeneste1602.class);
        jaxWsProxyFactoryBean.setAddress(environment.getProperty("oppslagstjenesten.url"));
        jaxWsProxyFactoryBean.setBindingId(javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING);
        addWSS4JInterceptors(jaxWsProxyFactoryBean);
        return jaxWsProxyFactoryBean.create(Oppslagstjeneste1602.class);
    }

    private void addWSS4JInterceptors(InterceptorProvider interceptorProvider) {
        interceptorProvider.getInInterceptors().add(getWss4JInInterceptor());
        interceptorProvider.getInInterceptors().add(oppslagstjenesteLoggingInInterceptor());
        interceptorProvider.getOutInterceptors().add(getWss4JOutInterceptor());
        interceptorProvider.getOutInterceptors().add(oppslagstjenesteLoggingOutInterceptor());
    }

    /**
     * For outgoing messages: Signature and timestamp validation.
     *
     * @return
     */
    private WSS4JOutInterceptor getWss4JOutInterceptor() {
        Map<String, Object> outputProperties = new HashMap<>();
        outputProperties.put(WSHandlerConstants.ACTION, WSHandlerConstants.SIGNATURE + " " + WSHandlerConstants.TIMESTAMP);
        outputProperties.put(WSHandlerConstants.USER, environment.getProperty("oppslagstjenesten.wss4joutinterceptor.sig_key_alias"));
        outputProperties.put(WSHandlerConstants.PW_CALLBACK_CLASS, ClientKeystorePasswordCallbackHandler.class.getName());
        outputProperties.put(WSHandlerConstants.SIG_PROP_FILE, environment.getProperty("oppslagstjenesten.wss4joutinterceptor.sig_prop_file"));

        //v5
        outputProperties.put(WSHandlerConstants.SIG_KEY_ID, "DirectReference"); // Using "X509KeyIdentifier" is also supported by oppslagstjenesten
        outputProperties.put(WSHandlerConstants.SIGNATURE_PARTS, "{}{}Body;{}{http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd}Timestamp}");
        outputProperties.put(WSHandlerConstants.SIG_ALGO, "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");

        return new WSS4JOutInterceptor(outputProperties);
    }


    /**
     * For incoming messages. Validate signature and timestamp.
     * Response is encrypted.
     *
     * @return
     */
    private WSS4JInInterceptor getWss4JInInterceptor() {
        Map<String, Object> inputProperties = new HashMap<>();
        inputProperties.put(WSHandlerConstants.ACTION, WSHandlerConstants.SIGNATURE + " " + WSHandlerConstants.TIMESTAMP + " " + WSHandlerConstants.ENCRYPT);
        inputProperties.put(WSHandlerConstants.PW_CALLBACK_CLASS, ClientKeystorePasswordCallbackHandler.class.getName());
        inputProperties.put(WSHandlerConstants.SIG_PROP_FILE, environment.getProperty("oppslagstjenesten.wss4jininterceptor.sig_prop_file"));
        inputProperties.put(WSHandlerConstants.DEC_PROP_FILE, environment.getProperty("oppslagstjenesten.wss4jininterceptor.dec_prop_file"));
//		inputProperties.put(WSHandlerConstants.ALLOW_RSA15_KEY_TRANSPORT_ALGORITHM, "true"); // TODO remove for V5??
        return new WSS4JInInterceptor(inputProperties);
    }

    private LoggingInInterceptor oppslagstjenesteLoggingInInterceptor() {
        LoggingInInterceptor loggingInInterceptor = new LoggingInInterceptor();
        loggingInInterceptor.setPrintWriter(new PrintWriter(xmlRetrievePersonsResponse));
        loggingInInterceptor.setPrettyLogging(true);
        return loggingInInterceptor;
    }

    private LoggingOutInterceptor oppslagstjenesteLoggingOutInterceptor() {
        LoggingOutInterceptor loggingOutInterceptor = new LoggingOutInterceptor();
        loggingOutInterceptor.setPrintWriter(new PrintWriter(xmlRetrievePersonsRequest));
        loggingOutInterceptor.setPrettyLogging(true);
        return loggingOutInterceptor;
    }

    public void setXmlRetrievePersonsRequest(StringWriter xmlRetrievePersonsRequest) {
        this.xmlRetrievePersonsRequest = xmlRetrievePersonsRequest;
    }

    public void setXmlRetrievePersonsResponse(StringWriter xmlRetrievePersonsResponse) {
        this.xmlRetrievePersonsResponse = xmlRetrievePersonsResponse;
    }
}
