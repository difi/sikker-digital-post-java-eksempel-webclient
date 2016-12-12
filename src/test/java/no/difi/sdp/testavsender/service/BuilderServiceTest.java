package no.difi.sdp.testavsender.service;

import no.difi.sdp.client2.domain.Forsendelse;
import no.difi.sdp.client2.domain.Prioritet;
import no.difi.sdp.client2.domain.TekniskMottaker;
import no.difi.sdp.testavsender.BaseTest;
import no.difi.sdp.testavsender.configuration.util.CryptoUtil;
import no.difi.sdp.testavsender.domain.*;
import no.digipost.api.representations.Organisasjonsnummer;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Enhetstest for BuildService.
 */
public class BuilderServiceTest extends BaseTest {

    @Mock
    private CryptoUtil cryptoUtil;

    @Mock
    private PostklientService postklientService;

    @InjectMocks
    private BuilderService builderService;

    @Test
    public void test_build_fysisk_forsendelse() {
        final String mcpId = "mcpId";
        final String alias = "alias";
        final String orgnrDifiLeikanger = "987464291";
        final Organisasjonsnummer orgNr = Organisasjonsnummer.of(orgnrDifiLeikanger);
        when(postklientService.createTekniskMottaker(alias)).thenReturn(new TekniskMottaker(orgNr, null));
        final Forsendelse forsendelse = builderService.buildFysiskForsendelse(createFysiskMessage(alias), mcpId);
        assertNotNull(forsendelse);
        verifyFysiskPost(forsendelse);
        assertNull(forsendelse.getDigitalPost());
        verifyCommonFields(mcpId, forsendelse);
        verify(postklientService).createTekniskMottaker(alias);
        verify(cryptoUtil, never()).loadX509Certificate(any(byte[].class));
    }

    @Test(expected = RuntimeException.class)
    public void test_build_fysisk_forsendelse_with_wrong_input() {
        final String mcpId = "mcpId";
        final byte[] postkasseSertifikat = {(byte) 0xe7, 0x4f};
        builderService.buildFysiskForsendelse(createDigitalMessage(postkasseSertifikat), mcpId);
    }

    private void verifyFysiskPost(Forsendelse forsendelse) {
        final no.difi.sdp.client2.domain.fysisk_post.FysiskPost fysiskPost = forsendelse.getFysiskPost();
        assertNotNull(fysiskPost);
        assertNotNull(fysiskPost.getAdresse());
        assertNotNull(fysiskPost.getAdresse().getAdresselinjer());
        assertNotNull(fysiskPost.getAdresse().getNavn());
        assertEquals(no.difi.sdp.client2.domain.fysisk_post.KonvoluttAdresse.Type.NORSK, fysiskPost.getAdresse().getType());
        assertNotNull(fysiskPost.getReturadresse());
        assertNotNull(fysiskPost.getReturadresse().getAdresselinjer());
        assertNotNull(fysiskPost.getReturadresse().getNavn());
        assertEquals(no.difi.sdp.client2.domain.fysisk_post.KonvoluttAdresse.Type.NORSK, fysiskPost.getReturadresse().getType());
        assertNotNull(fysiskPost.getPosttype());
        assertNotNull(fysiskPost.getReturhaandtering());
        assertNotNull(fysiskPost.getUtskriftsfarge());
        assertNotNull(fysiskPost.getUtskriftsleverandoer());
    }

    private void verifyCommonFields(String mcpId, Forsendelse forsendelse) {
        assertNotNull(forsendelse.getDokumentpakke());
        assertNotNull(forsendelse.getAvsender());
        assertNotNull(forsendelse.getPrioritet());
        assertNotNull(forsendelse.getSpraakkode());
        assertNotNull(forsendelse.getMpcId());
        assertEquals(mcpId, forsendelse.getMpcId());
    }

    @Test
    public void test_build_digital_forsendelse() {
        final String mcpId = "mcpId";
        final byte[] postkasseSertifikat = {(byte) 0xe7, 0x4f};
        final Forsendelse forsendelse = builderService.buildDigitalForsendelse(createDigitalMessage(postkasseSertifikat), mcpId);
        assertNotNull(forsendelse);
        assertNotNull(forsendelse.getDigitalPost());
        assertNotNull(forsendelse.getDigitalPost().getMottaker());
        assertNull(forsendelse.getFysiskPost());
        verifyCommonFields(mcpId, forsendelse);
        verify(postklientService, never()).createTekniskMottaker(anyString());
        verify(cryptoUtil).loadX509Certificate(postkasseSertifikat);
    }

    @Test(expected = RuntimeException.class)
    public void test_build_digital_forsendelse_with_wrong_input() {
        final String mcpId = "mcpId";
        builderService.buildDigitalForsendelse(createFysiskMessage("alias"), mcpId);
    }

    private Message createFysiskMessage(String alias) {
        final Message message = new Message(false);
        message.setFysiskPost(createFysiskPost(alias));
        createCommonMessageAttributes(message);
        return message;
    }

    private FysiskPost createFysiskPost(String alias) {
        final FysiskPost fysiskPost = new FysiskPost();
        fysiskPost.setUtskriftsfarge(Utskriftsfarge.SORT_HVIT);
        fysiskPost.setReturhaandtering(Returhaandtering.DIREKTE_RETUR);
        fysiskPost.setPosttype(Posttype.A_PRIORITERT);
        fysiskPost.setAdressat(createAdresse());
        fysiskPost.setReturadresse(createAdresse());
        fysiskPost.setTekniskMottakerSertifikatAlias(alias);
        return fysiskPost;
    }

    private KonvoluttAdresse createAdresse() {
        final KonvoluttAdresse konvoluttAdresse = new KonvoluttAdresse();
        konvoluttAdresse.setType(KonvoluttAdresse.Type.NORSK);
        konvoluttAdresse.setNavn("navn");
        final List<String> adresselinjer = new ArrayList<String>(4);
        adresselinjer.add("linje1");
        adresselinjer.add("linje2");
        adresselinjer.add("linje3");
        adresselinjer.add(null);
        konvoluttAdresse.setAdresselinjer(adresselinjer);
        return konvoluttAdresse;
    }

    private Message createDigitalMessage(byte[] postkasseSertifikat) {
        final Message message = new Message(true);
        message.setDigitalPost(createDigitalPost(postkasseSertifikat));
        createCommonMessageAttributes(message);
        return message;
    }

    private void createCommonMessageAttributes(Message message) {
        message.setDocument(createDocument());
        message.setAttachments(new HashSet<>());
        message.setPriority(Prioritet.NORMAL);
        message.setLanguageCode("NO");
        String orgNrDigipost = "984661185";
        message.setSenderOrgNumber(orgNrDigipost);
    }

    private DigitalPost createDigitalPost(byte[] postkasseSertifikat) {
        final DigitalPost digitalPost = new DigitalPost();
        digitalPost.setPostboxCertificate(postkasseSertifikat);
        String orgNrDigipost = "984661185";
        digitalPost.setPostboxVendorOrgNumber(orgNrDigipost);
        return digitalPost;
    }

    private Document createDocument() {
        final Document document = new Document();
        final byte[] bytes = {(byte) 0xe7, 0x4f};
        document.setContent(bytes);
        document.setTitle("tittel");
        return document;
    }


}
