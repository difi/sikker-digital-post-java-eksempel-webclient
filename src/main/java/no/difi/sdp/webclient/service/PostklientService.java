package no.difi.sdp.webclient.service;

import java.security.KeyStore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import no.difi.sdp.client.KlientKonfigurasjon;
import no.difi.sdp.client.SikkerDigitalPostKlient;
import no.difi.sdp.client.asice.CreateASiCE;
import no.difi.sdp.client.domain.Forsendelse;
import no.difi.sdp.client.domain.Noekkelpar;
import no.difi.sdp.client.domain.TekniskAvsender;
import no.difi.sdp.webclient.configuration.util.CryptoUtil;

@Service
public class PostklientService {

	@Autowired
	private KeyStore keyStore;
	
	@Autowired
	private CryptoUtil cryptoUtil;
	
	@Autowired
	private KlientKonfigurasjon klientKonfigurasjon;
	
	@Autowired
	private Environment environment;
	
	@Autowired
	private CreateASiCE createAsice;
	
	private Map<String, Map<String, SikkerDigitalPostKlient>> postklientMap = new HashMap<String, Map<String, SikkerDigitalPostKlient>>(); // Cache
	
	private Map<String, Map<String, TekniskAvsender>> tekniskAvsenderMap = new HashMap<String, Map<String, TekniskAvsender>>(); // Cache
	
	private Map<String, Noekkelpar> noekkelparMap = new HashMap<String, Noekkelpar>(); // Cache
	
	/**
	 * Gets SikkerDigitalPostKlient given orgNumber and alias. Creates new SikkerDigitalPostKlient if no matching SikkerDigitalPostKlient was found in cache.
	 * @param orgNumber
	 * @param alias
	 * @return
	 */
	public SikkerDigitalPostKlient get(String orgNumber, String alias) {
		if (! postklientMap.containsKey(orgNumber)) {
			postklientMap.put(orgNumber, new HashMap<String, SikkerDigitalPostKlient>());
		}
		if (! postklientMap.get(orgNumber).containsKey(alias)) {
			postklientMap.get(orgNumber).put(alias, createPostKlient(orgNumber, alias));
		}
		return postklientMap.get(orgNumber).get(alias);	
	}

	/**
	 * Gets TekniskAvsender given orgNumber and alias. Creates new TekniskAvsender if no matching TekniskAvsender was found in cache.
	 * @param orgNumber
	 * @param alias
	 * @return
	 */
	private TekniskAvsender getTekniskAvsender(String orgNumber, String alias) {
		if (! tekniskAvsenderMap.containsKey(orgNumber)) {
			tekniskAvsenderMap.put(orgNumber, new HashMap<String, TekniskAvsender>());
		}
		if (! tekniskAvsenderMap.get(orgNumber).containsKey(alias)) {
			tekniskAvsenderMap.get(orgNumber).put(alias, createTekniskAvsender(orgNumber, alias));
		}
		return tekniskAvsenderMap.get(orgNumber).get(alias);
	}
	
	/**
	 * Gets Noekkelpar given alias. Creates new Noekkelpar if no matching Noekkelpar was found in cache.
	 * @param alias
	 * @return
	 */
	private Noekkelpar getNoekkelpar(String alias) {
		if (! noekkelparMap.containsKey(alias)) {
			noekkelparMap.put(alias, createNoekkelpar(alias));
		}
		return noekkelparMap.get(alias);
	}
	
	private SikkerDigitalPostKlient createPostKlient(String orgNumber, String alias) {
		TekniskAvsender tekniskAvsender = getTekniskAvsender(orgNumber, alias);
    	return new SikkerDigitalPostKlient(tekniskAvsender, klientKonfigurasjon);
	}
	
	private TekniskAvsender createTekniskAvsender(String orgNumber, String alias) {
    	Noekkelpar noekkelpar = getNoekkelpar(alias);
        TekniskAvsender tekniskAvsender = TekniskAvsender.builder(orgNumber, noekkelpar).build();
        return tekniskAvsender;
    }

	private Noekkelpar createNoekkelpar(String alias) {
		return Noekkelpar.fraKeyStore(keyStore, alias, environment.getProperty("meldingsformidler.avsender.key.password"));
	}
	
	public byte[] createAsice(String orgNumber, String alias, Forsendelse forsendelse) {
		TekniskAvsender tekniskAvsender = getTekniskAvsender(orgNumber, alias);
		return createAsice.createAsice(tekniskAvsender, forsendelse).getBytes();
	}
	
	public List<String> getKeypairAliases() {
		return cryptoUtil.getKeypairAliases(keyStore);
	}
	
}
