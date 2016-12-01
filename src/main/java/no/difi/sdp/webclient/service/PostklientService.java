package no.difi.sdp.webclient.service;


import no.difi.sdp.client2.KlientKonfigurasjon;
import no.difi.sdp.client2.SikkerDigitalPostKlient;
import no.difi.sdp.client2.asice.CreateASiCE;
import no.difi.sdp.client2.domain.*;
import no.difi.sdp.webclient.configuration.util.CryptoUtil;
import no.digipost.api.representations.Organisasjonsnummer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.security.KeyStore;
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

    private Map<String, Databehandler> tekniskAvsenderMap = new HashMap<>(); // Cache

    private Map<String, Noekkelpar> noekkelparMap = new HashMap<>(); // Cache

    private Map<String, Sertifikat> sertifikatMapTekniskMottaker = new HashMap<>(); // Cache

    private Pattern keyPairAliasOrgNumberPattern = Pattern.compile("^[0-9]{9}"); // Pattern for extracting orgNumber from keyPairAlias

    /**
     * Gets SikkerDigitalPostKlient given keyPairAlias. Creates new SikkerDigitalPostKlient if no matching SikkerDigitalPostKlient was found in cache.
     *
     * @param keyPairAlias
     * @return
     */
    public SikkerDigitalPostKlient get(String keyPairAlias) {
        if (!postklientMap.containsKey(keyPairAlias)) {
            postklientMap.put(keyPairAlias, createPostKlient(keyPairAlias));
        }
        return postklientMap.get(keyPairAlias);
    }

    /**
     * Gets TekniskAvsender given keyPairAlias. Creates new TekniskAvsender if no matching TekniskAvsender was found in cache.
     *
     * @param keyPairAlias
     * @return
     */
    private Databehandler getTekniskAvsender(String keyPairAlias) {
        if (!tekniskAvsenderMap.containsKey(keyPairAlias)) {
            tekniskAvsenderMap.put(keyPairAlias, createDatabehandler(keyPairAlias));
        }
        return tekniskAvsenderMap.get(keyPairAlias);
    }

    /**
     * Gets Noekkelpar given keyPairAlias. Creates new Noekkelpar if no matching Noekkelpar was found in cache.
     *
     * @param keyPairAlias
     * @return
     */
    private Noekkelpar getNoekkelpar(String keyPairAlias) {
        if (!noekkelparMap.containsKey(keyPairAlias)) {
            noekkelparMap.put(keyPairAlias, createNoekkelpar(keyPairAlias));
        }
        return noekkelparMap.get(keyPairAlias);
    }

    /**
     * Gets Noekkelpar given keyPairAlias. Creates new Noekkelpar if no matching Noekkelpar was found in cache.
     *
     * @param keyPairAlias
     * @return
     */
    private Sertifikat getNoekkelparTekniskMottaker(String keyPairAlias) {
        if (!sertifikatMapTekniskMottaker.containsKey(keyPairAlias)) {
            sertifikatMapTekniskMottaker.put(keyPairAlias, createSertifikatTekniskMottaker(keyPairAlias));
        }
        return sertifikatMapTekniskMottaker.get(keyPairAlias);
    }

    private SikkerDigitalPostKlient createPostKlient(String keyPairAlias) {

        Databehandler databehandler = createDatabehandler(keyPairAlias);
        return new SikkerDigitalPostKlient(databehandler, klientKonfigurasjon);
    }


    private Databehandler createDatabehandler(String keyPairAlias) {
        Noekkelpar noekkelpar = getNoekkelpar(keyPairAlias);
        String orgNumber = extractOrgNumbeFromKeyPairAlias(keyPairAlias);
        DatabehandlerOrganisasjonsnummer databehandlerOrgnr =
                AktoerOrganisasjonsnummer.of(orgNumber).forfremTilDatabehandler();
        Databehandler databehandler = Databehandler
                .builder(databehandlerOrgnr, noekkelpar)
                .build();

        return databehandler;
    }

    public TekniskMottaker createTekniskMottaker(String certificateAlias) {
        Sertifikat sertifikat = getNoekkelparTekniskMottaker(certificateAlias);
        String orgNumber = extractOrgNumbeFromKeyPairAlias(certificateAlias);
        return new TekniskMottaker(Organisasjonsnummer.of(orgNumber), sertifikat);
    }


    private Noekkelpar createNoekkelpar(String keyPairAlias) {
        return Noekkelpar.fraKeyStore(keyStore, keyPairAlias, environment.getProperty("sdp.databehandler.keypair.password"));
    }

    private Sertifikat createSertifikatTekniskMottaker(String keyPairAlias) {
        return Sertifikat.fraKeyStore(keyStoreTekniskMottaker, keyPairAlias);
    }

    private String extractOrgNumbeFromKeyPairAlias(String keyPairAlias) {
        Matcher m = keyPairAliasOrgNumberPattern.matcher(keyPairAlias);
        m.find();
        return m.group();
    }

    public byte[] createAsice(String keyPairAlias, Forsendelse forsendelse) {
        Databehandler tekniskAvsender = getTekniskAvsender(keyPairAlias);
        return createAsice.createAsice(tekniskAvsender, forsendelse).getBytes();
    }

    public List<String> getKeypairAliases() {
        return cryptoUtil.getKeypairAliases(keyStore);
    }

    public List<String> getKeyStoreTekniskMottakerAliases() {
        return cryptoUtil.getCertificateAliases(keyStoreTekniskMottaker);
    }

}
