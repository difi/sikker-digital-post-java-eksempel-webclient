package no.difi.sdp.testavsender;

import no.difi.sdp.testavsender.web.DigitalPostCommand;
import no.difi.sdp.testavsender.web.MessageCommand;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 *
 */
public class TestDataUtil {

    public static MessageCommand createDigitalMessageCommand(){
        MessageCommand messageCommand = new MessageCommand(MessageCommand.Type.DIGITAL);
        messageCommand.setDigitalPostCommand(createDigitalPostCommand());
        messageCommand.setDocument(createDocument());

        return messageCommand;
    }

    private static CommonsMultipartFile createDocument() {
        DiskFileItem fileItem = mock(DiskFileItem.class);
        when(fileItem.getSize()).thenReturn(100L);
        when(fileItem.isInMemory()).thenReturn(true);
        return new CommonsMultipartFile(fileItem);
    }

    private static DigitalPostCommand createDigitalPostCommand() {
        DigitalPostCommand digitalPostCommand = new DigitalPostCommand();
        return digitalPostCommand;
    }
}
