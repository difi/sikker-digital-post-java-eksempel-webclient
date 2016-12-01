package no.difi.sdp.webclient.web;

import no.difi.begrep.Reservasjon;
import no.difi.begrep.Status;
import no.difi.sdp.client2.domain.Prioritet;
import no.difi.sdp.client2.domain.digital_post.Sikkerhetsnivaa;
import no.difi.sdp.webclient.domain.DigitalPost;
import no.difi.sdp.webclient.domain.Document;
import no.difi.sdp.webclient.domain.Message;
import no.difi.sdp.webclient.service.MessageService;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Controller for ytelsetest av digital melding til digipost eller eBoks.
 */
@Controller
public class PerformanceTestController {

    @Autowired
    private Environment environment;

    @Autowired
    private MessageService messageService;


    @RequestMapping(method = RequestMethod.GET, value = "/performance")
    @ResponseStatus(value = HttpStatus.OK)
    public void performanceTestSendMessage(@RequestParam String ssn, @RequestParam PerformanceTestSize size, @RequestParam(required = false) String postboxAddress, @RequestParam(required = false) PostboxVendor postboxVendor, @RequestParam(required = false) Prioritet prioritet) throws IOException {
        Message message = new Message(true);
        message.setSsn(ssn);
        if(prioritet==null) {
            message.setPriority(Prioritet.NORMAL);
        }else{
            message.setPriority(prioritet);
        }
        message.setLanguageCode("NO");


        message.setSenderOrgNumber(environment.getProperty("sdp.behandlingsansvarlig.organisasjonsnummer"));
        message.setKeyPairAlias(environment.getProperty("sdp.databehandler.keypair.alias"));

        String pdfInputFileName = getPdfNameAndAddAttachementsToMessage(size, message);
        InputStream pdfInputStream = this.getClass().getClassLoader().getResourceAsStream(pdfInputFileName);
        message.setDocument(createDocument(ssn, pdfInputStream));

        DigitalPost digitalPost = new DigitalPost("Brev til " + ssn + " " + new Date());
        digitalPost.setSecurityLevel(Sikkerhetsnivaa.NIVAA_3);
        digitalPost.setRetrieveContactDetails(postboxAddress == null || postboxVendor == null);
        message.setSaveBinaryContent(false);
        if (!digitalPost.getRetrieveContactDetails()) {
            // Uses the provided contact details (skips retrieval of contact details from oppslagstjenesten)
            digitalPost.setContactRegisterStatus(Status.AKTIV);
            digitalPost.setReservationStatus(Reservasjon.NEI);
            digitalPost.setPostboxAddress(postboxAddress);
            setPostboxVendor(digitalPost, postboxVendor);
        }
        message.setDigitalPost(digitalPost);
        messageService.sendMessage(message);
    }

    private String getPdfNameAndAddAttachementsToMessage(PerformanceTestSize size, Message message) throws IOException {
        Set<Document> attachments = new HashSet<Document>();
        String pdfInputFileName;
        switch (size) {

            case SIZE_10KB:
                pdfInputFileName = "SDP-Litedok_NAV-10kB.pdf";
                break;
            case SIZE_80KB:
                pdfInputFileName = "SDP-MiddelsLiteDok_kreftreg-80kB.pdf";
                break;
            case SIZE_800KB:
                pdfInputFileName = "SDP-MiddelsStortdok_SI-800kB.pdf";
                break;
            case SIZE_8MB:
                pdfInputFileName = "SDP-StortDokument-4MB.pdf";

                attachments.add(getDocumentByFilename(message, "SDP-Vedlegg1-2MB.pdf"));
                attachments.add(getDocumentByFilename(message, "SDP-Vedlegg2-2MB.pdf"));
                break;
            default:
                throw new RuntimeException("Size not supported: " + size.toString());
        }
        message.setAttachments(attachments);
        return pdfInputFileName;
    }

    private void setPostboxVendor(DigitalPost digitalPost, PostboxVendor postboxVendor) {
        switch (postboxVendor) {
            case DIGIPOST:
                digitalPost.setPostboxVendorOrgNumber(environment.getProperty("performancetest.digipost.orgnr"));
                digitalPost.setPostboxCertificate(Base64.decodeBase64(environment.getProperty("performancetest.digipost.postbox.certificate")));
                break;
            case EBOKS:
                digitalPost.setPostboxVendorOrgNumber(environment.getProperty("performancetest.eboks.orgnr"));
                digitalPost.setPostboxCertificate(Base64.decodeBase64(environment.getProperty("performancetest.eboks.postbox.certificate")));
                break;
            default:
                throw new RuntimeException("Postbox vendor not supported: " + postboxVendor.toString());
        }
    }

    private Document createDocument(String ssn, InputStream pdfInputStream) throws IOException {
        byte[] pdf = IOUtils.toByteArray(pdfInputStream);
        Document document = new Document();
        document.setFilename("testfil.pdf");
        document.setContent(pdf);
        document.setMimetype("application/pdf");
        document.setTitle("Brev til " + ssn + " " + new Date());
        return document;
    }


    private Document getDocumentByFilename(Message message, String filename) throws IOException {
        InputStream pdfInputStream = this.getClass().getClassLoader().getResourceAsStream(filename);
        byte[] PDF = IOUtils.toByteArray(pdfInputStream);
        Document attachment = new Document();
        attachment.setTitle("Testpdf " + filename);
        attachment.setContent(PDF);
        attachment.setFilename(filename);
        attachment.setMimetype("application/pdf");
        attachment.setMessage(message);
        return attachment;
    }


    public enum PerformanceTestSize {
        SIZE_10KB,
        SIZE_80KB,
        SIZE_800KB,
        SIZE_8MB
    }

    public enum PostboxVendor {
        EBOKS,
        DIGIPOST
    }
}
