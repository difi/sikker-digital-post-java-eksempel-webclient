package no.difi.sdp.testavsender.service;


import no.difi.sdp.client2.domain.*;
import no.difi.sdp.client2.domain.digital_post.DigitalPost;
import no.difi.sdp.client2.domain.digital_post.EpostVarsel;
import no.difi.sdp.client2.domain.digital_post.SmsVarsel;
import no.difi.sdp.client2.domain.fysisk_post.*;
import no.difi.sdp.testavsender.configuration.util.CryptoUtil;
import no.difi.sdp.testavsender.configuration.util.StringUtil;
import no.difi.sdp.testavsender.domain.Document;
import no.difi.sdp.testavsender.domain.Message;
import no.digipost.api.representations.Organisasjonsnummer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * Hjelpeklasse for å bygge Forsendelse som er input til "Sikker digital post"-API-et
 * for sending av fysisk og digital melding til Meldingsformidler.
 */
@Service
public class BuilderService {

    @Autowired
    private CryptoUtil cryptoUtil;

    @Autowired
    private PostklientService postklientService;

    /**
     * Lager fysisk post forsendelse.
     *
     * @param message
     * @param mpcId
     * @return Forsendelse
     */
    public Forsendelse buildFysiskForsendelse(Message message, String mpcId) {

        if(message.isDigital()){
            throw new RuntimeException("Kan ikke lage Forsendelse av digital message: " + message);
        }

        no.difi.sdp.testavsender.domain.FysiskPost fysiskPost = message.getFysiskPost();

        Returhaandtering returhaandtering = Returhaandtering.valueOf(fysiskPost.getReturhaandtering().toString());
        Posttype posttype = Posttype.valueOf(message.getFysiskPost().getPosttype().toString());
        Utskriftsfarge utskriftsfarge = Utskriftsfarge.valueOf(fysiskPost.getUtskriftsfarge().toString());
        TekniskMottaker tekniskMottaker = postklientService.createTekniskMottaker(fysiskPost.getTekniskMottakerSertifikatAlias());

        FysiskPost fysiskpost = FysiskPost.builder()
                .adresse(buildAdressatAdresse(message))
                .retur(returhaandtering, buildReturAdresse(message))
                .sendesMed(posttype)
                .utskrift(utskriftsfarge, tekniskMottaker)
                .build();

        Avsender behandlingsansvarlig = buildBehandlingsansvarlig(message);
        Dokumentpakke dokumentPakke = buildDokumentpakke(message);

        return Forsendelse
                .fysisk(behandlingsansvarlig, fysiskpost, dokumentPakke)
                .mpcId(mpcId)
                .build();
    }

    /**
     * Lager digital post forsendelse.
     *
     * @param message
     * @param mpcId
     * @return Forsendelse
     */
    public Forsendelse buildDigitalForsendelse(Message message, String mpcId) {

        if(!message.isDigital()){
            throw new RuntimeException("Kan ikke lage Forsendelse av fysisk message: " + message);
        }

        Mottaker mottaker = buildMottaker(message);
        DigitalPost digitalPost = DigitalPost
                .builder(mottaker, message.getDigitalPost().getInsensitiveTitle())
                .sikkerhetsnivaa(message.getDigitalPost().getSecurityLevel())
                .aapningskvittering(message.getDigitalPost().getRequiresMessageOpenedReceipt())
                .epostVarsel(buildEpostVarsel(message))
                .smsVarsel(buildSmsVarsel(message))
                .virkningsdato(message.getDigitalPost().getDelayedAvailabilityDate())
                .build();
        Dokumentpakke dokumentPakke = buildDokumentpakke(message);
        Avsender behandlingsansvarlig = buildBehandlingsansvarlig(message);
        return Forsendelse
                .digital(behandlingsansvarlig, digitalPost, dokumentPakke)
                .prioritet(message.getPriority())
                .spraakkode(message.getLanguageCode())
                .mpcId(mpcId)
                .build();
    }

    private Avsender buildBehandlingsansvarlig(Message message) {

        AvsenderOrganisasjonsnummer avsenderOrgnr =
                AktoerOrganisasjonsnummer.of(message.getSenderOrgNumber()).forfremTilAvsender();
        Avsender behandlingsansvarlig = Avsender
                .builder(avsenderOrgnr)
                .avsenderIdentifikator(message.getSenderId())
                .fakturaReferanse(message.getInvoiceReference())
                .build();
        return behandlingsansvarlig;
    }

    private KonvoluttAdresse buildAdressatAdresse(Message message) {
        return buildAdresse(message.getFysiskPost().getAdressat());
    }

    private KonvoluttAdresse buildReturAdresse(Message message) {
        return buildAdresse(message.getFysiskPost().getReturadresse());
    }

    private KonvoluttAdresse buildAdresse(no.difi.sdp.testavsender.domain.KonvoluttAdresse adresseFrom) {
        KonvoluttAdresse adresseTo = null;
        if (adresseFrom.getType() == no.difi.sdp.testavsender.domain.KonvoluttAdresse.Type.NORSK) {

            adresseTo = adresseTo.build(adresseFrom.getNavn())
                    .iNorge(adresseFrom.getAdresselinjer().get(0),
                            adresseFrom.getAdresselinjer().get(1),
                            adresseFrom.getAdresselinjer().get(2),
                            adresseFrom.getPostnummer(),
                            adresseFrom.getPoststed())
                    .build();

        } else if (adresseFrom.getType() == no.difi.sdp.testavsender.domain.KonvoluttAdresse.Type.UTENLANDSK) {
            adresseTo = adresseTo.build(adresseFrom.getNavn())
                    .iUtlandet(adresseFrom.getAdresselinjer().get(0),
                            adresseFrom.getAdresselinjer().get(1),
                            adresseFrom.getAdresselinjer().get(2),
                            adresseFrom.getAdresselinjer().get(3),
                            Landkoder.landkode(adresseFrom.getLandkode()))
                    .build();
        }
        return adresseTo;
    }

    private List<Dokument> buildVedlegg(Message message) {
        List<Dokument> attachments = new ArrayList<>();
        for (Document document : message.getAttachments()) {
            attachments.add(buildDokument(document));
        }
        return attachments;
    }

    private Dokument buildDokument(Document document) {
        ByteArrayInputStream documentContent = new ByteArrayInputStream(document.getContent());
        return Dokument
                .builder(document.getTitle(), document.getFilename(), documentContent)
                .mimeType(document.getMimetype())
                .build();
    }

    private Sertifikat buildSertifikat(Message message) {
        X509Certificate x509Certificate = cryptoUtil.loadX509Certificate(message.getDigitalPost().getPostboxCertificate());
        return Sertifikat.fraCertificate(x509Certificate);
    }

    private Mottaker buildMottaker(Message message) {
        Sertifikat sertifikat = buildSertifikat(message);
        return Mottaker
                .builder(message.getSsn(), message.getDigitalPost().getPostboxAddress(), sertifikat, Organisasjonsnummer.of(message.getDigitalPost().getPostboxVendorOrgNumber()))
                .build();
    }

    private SmsVarsel buildSmsVarsel(Message message) {
        if (message.getDigitalPost().getMobileNotification() == null || message.getDigitalPost().getMobile() == null) {
            return null;
        }
        SmsVarsel.Builder smsVarselBuilder = SmsVarsel.builder(message.getDigitalPost().getMobile(), message.getDigitalPost().getMobileNotification());
        if (message.getDigitalPost().getMobileNotificationSchedule() != null) {
            smsVarselBuilder.varselEtterDager(StringUtil.toIntList(message.getDigitalPost().getMobileNotificationSchedule()));
        }
        return smsVarselBuilder.build();
    }

    private Dokumentpakke buildDokumentpakke(Message message) {
        Dokumentpakke.Builder builder = Dokumentpakke.builder(buildDokument(message.getDocument()));
        if (message.getAttachments().size() == 0) {
            return builder.build();
        }
        builder.vedlegg(buildVedlegg(message));
        return builder.build();
    }

    private EpostVarsel buildEpostVarsel(Message message) {
        if (message.getDigitalPost().getEmailNotification() == null || message.getDigitalPost().getEmail() == null) {
            return null;
        }
        EpostVarsel.Builder epostVarselBuilder = EpostVarsel.builder(message.getDigitalPost().getEmail(), message.getDigitalPost().getEmailNotification());
        if (message.getDigitalPost().getEmailNotificationSchedule() != null) {
            epostVarselBuilder.varselEtterDager(StringUtil.toIntList(message.getDigitalPost().getEmailNotificationSchedule()));
        }
        return epostVarselBuilder.build();
    }


}
