package no.difi.sdp.testavsender.web;

import no.difi.sdp.testavsender.TestDataUtil;
import no.difi.sdp.testavsender.domain.Message;
import no.difi.sdp.testavsender.service.MessageService;
import no.difi.sdp.testavsender.service.PostklientService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class MessageControllerTest {

    @Mock
    private MessageService messageService;

    @Mock
    @Qualifier("messageCommandValidator")
    private Validator messageValidator;

    @Mock
    @Qualifier("mvcValidator")
    private Validator validator;

    @Mock
    HttpServletRequest request;

    @Mock
    BindingResult bindingResult;

    @Mock
    Model model;

    @Mock
    private PostklientService postklientService;


    @InjectMocks
    private MessageController messageController;

    @Test
    public void testSendMessage() throws IOException {
        MessageCommand messageCommand = createMessageCommand();
        String returnToView = messageController.send_message(messageCommand, bindingResult, model, null, request);
        assertNotNull(returnToView);
        verify(messageValidator).validate(messageCommand, bindingResult);
        verify(messageService).sendMessage(any(Message.class));
    }

    @Test
    public void testSendMessageWhenValidationError() throws IOException {
        MessageCommand messageCommand = createInvalidMessageCommand();
        when(bindingResult.hasErrors()).thenReturn(true);
        String returnToView = messageController.send_message(messageCommand, bindingResult, model, null, request);
        assertNotNull(returnToView);
        assertEquals("send_message_page", returnToView);
        verify(postklientService).getKeypairAliases();
        verify(messageValidator).validate(messageCommand, bindingResult);
        verify(messageService, never()).sendMessage(any(Message.class));
    }

    private MessageCommand createMessageCommand() {
        MessageCommand digitalMessageCommand = TestDataUtil.createDigitalMessageCommand();
        digitalMessageCommand.setRetrieveContactDetails(true);
        return digitalMessageCommand;
    }

    private MessageCommand createInvalidMessageCommand() {
        MessageCommand digitalMessageCommand = TestDataUtil.createDigitalMessageCommand();
        digitalMessageCommand.setRetrieveContactDetails(false);
        return digitalMessageCommand;
    }
}
