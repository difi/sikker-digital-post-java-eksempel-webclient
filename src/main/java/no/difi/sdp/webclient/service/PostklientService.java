package no.difi.sdp.webclient.service;

import no.difi.sdp.client.KlientKonfigurasjon;
import no.difi.sdp.client.SikkerDigitalPostKlient;
import no.difi.sdp.client.asice.CreateASiCE;
import no.difi.sdp.client.domain.Forsendelse;
import no.difi.sdp.client.domain.Noekkelpar;
import no.difi.sdp.client.domain.TekniskAvsender;
import no.difi.sdp.webclient.configuration.util.CryptoUtil;
import no.difi.sdp.webclient.domain.TekniskMottaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PostklientService {

	@Autowired
	private KeyStore keyStore;

	@Autowired
	private KeyStore keyStoreTekniskMottaker;

	@Autowired
	private CryptoUtil cryptoUtil;
	
	@Autowired
	private KlientKonfigurasjon klientKonfigurasjon;
	
	@Autowired
	private Environment environment;
	
	@Autowired
	private CreateASiCE createAsice;
	
	private Map<String, SikkerDigitalPostKlient> postklientMap = new HashMap<String, SikkerDigitalPostKlient>(); // Cache
	
	private Map<String, TekniskAvsender> tekniskAvsenderMap = new HashMap<String, TekniskAvsender>(); // Cache
	
	private Map<String, Noekkelpar> noekkelparMap = new HashMap<>(); // Cache

	private Map<String, Noekkelpar> noekkelparMapTekniskMottaker = new HashMap<>(); // Cache

	private Pattern keyPairAliasOrgNumberPattern = Pattern.compile("^[0-9]{9}"); // Pattern for extracting orgNumber from keyPairAlias
	
	/**
	 * Gets SikkerDigitalPostKlient given keyPairAlias. Creates new SikkerDigitalPostKlient if no matching SikkerDigitalPostKlient was found in cache.
	 * @param keyPairAlias
	 * @return
	 */
	public SikkerDigitalPostKlient get(String keyPairAlias) {
		if (! postklientMap.containsKey(keyPairAlias)) {
			postklientMap.put(keyPairAlias, createPostKlient(keyPairAlias));
		}
		return postklientMap.get(keyPairAlias);	
	}

	/**
	 * Gets TekniskAvsender given keyPairAlias. Creates new TekniskAvsender if no matching TekniskAvsender was found in cache.
	 * @param keyPairAlias
	 * @return
	 */
	private TekniskAvsender getTekniskAvsender(String keyPairAlias) {
		if (! tekniskAvsenderMap.containsKey(keyPairAlias)) {
			tekniskAvsenderMap.put(keyPairAlias, createTekniskAvsender(keyPairAlias));
		}
		return tekniskAvsenderMap.get(keyPairAlias);
	}
	
	/**
	 * Gets Noekkelpar given keyPairAlias. Creates new Noekkelpar if no matching Noekkelpar was found in cache.
	 * @param keyPairAlias
	 * @return
	 */
	private Noekkelpar getNoekkelpar(String keyPairAlias) {
		if (! noekkelparMap.containsKey(keyPairAlias)) {
			noekkelparMap.put(keyPairAlias, createNoekkelpar(keyPairAlias));
		}
		return noekkelparMap.get(keyPairAlias);
	}

	/**
	 * Gets Noekkelpar given keyPairAlias. Creates new Noekkelpar if no matching Noekkelpar was found in cache.
	 * @param keyPairAlias
	 * @return
	 */
	private Noekkelpar getNoekkelparTekniskMottaker(String keyPairAlias) {
		if (! noekkelparMapTekniskMottaker.containsKey(keyPairAlias)) {
			noekkelparMapTekniskMottaker.put(keyPairAlias, createNoekkelparTekniskMottaker(keyPairAlias));
		}
		return noekkelparMapTekniskMottaker.get(keyPairAlias);
	}

	private SikkerDigitalPostKlient createPostKlient(String keyPairAlias) {
		TekniskAvsender tekniskAvsender = getTekniskAvsender(keyPairAlias);
    	return new SikkerDigitalPostKlient(tekniskAvsender, klientKonfigurasjon);
	}
	
	private TekniskAvsender createTekniskAvsender(String keyPairAlias) {
    	Noekkelpar noekkelpar = getNoekkelpar(keyPairAlias);
    	String orgNumber = extractOrgNumbeFromKeyPairAlias(keyPairAlias);
        TekniskAvsender tekniskAvsender = TekniskAvsender.builder(orgNumber, noekkelpar).build();
        return tekniskAvsender;
    }


	public TekniskMottaker createTekniskMottaker(String keyPairAlias) {
		X509Certificate sertifikat = getNoekkelparTekniskMottaker(keyPairAlias).getSertifikat().getX509Certificate();
		String orgNumber = extractOrgNumbeFromKeyPairAlias(keyPairAlias);
		return new TekniskMottaker(orgNumber, sertifikat);
	}


	private Noekkelpar createNoekkelpar(String keyPairAlias) {
		return Noekkelpar.fraKeyStore(keyStore, keyPairAlias, environment.getProperty("sdp.databehandler.keypair.password"));
	}

	private Noekkelpar createNoekkelparTekniskMottaker(String keyPairAlias) {
		return Noekkelpar.fraKeyStore(keyStoreTekniskMottaker, keyPairAlias, environment.getProperty("sdp.tekniskmottaker.keypair.password"));
	}
	
	private String extractOrgNumbeFromKeyPairAlias(String keyPairAlias) {
		Matcher m = keyPairAliasOrgNumberPattern.matcher(keyPairAlias);
		m.find();
		return m.group();
	}
	
	public byte[] createAsice(String keyPairAlias, Forsendelse forsendelse) {
		TekniskAvsender tekniskAvsender = getTekniskAvsender(keyPairAlias);
		return createAsice.createAsice(tekniskAvsender, forsendelse).getBytes();
	}
	
	public List<String> getKeypairAliases() {
		return cryptoUtil.getKeypairAliases(keyStore);
	}

	public List<String> getKeyStoreTekniskMottakerAliases() {
		return cryptoUtil.getKeypairAliases(keyStoreTekniskMottaker);
	}

}
