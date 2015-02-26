package no.difi.sdp.webclient.web;

import no.difi.sdp.webclient.domain.Message;
import no.difi.sdp.webclient.service.BaseTest;
import no.difi.sdp.webclient.service.MessageService;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.env.Environment;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;


/**
 * Test of PerformanceTestController.
 */
public class PerformanceTestControllerTest extends BaseTest {

    @InjectMocks
    private PerformanceTestController controller;

    @Mock
    private MessageService service;

    @Mock
    private Environment environment;

    @Test
    public void test_performance_test_send_message() throws IOException {
        final String ssn = "ssn";
        final String postboksAdresse = "postboksAdresse";
        controller.performanceTestSendMessage(ssn, PerformanceTestController.PerformanceTestSize.SIZE_10KB, postboksAdresse, PerformanceTestController.PostboxVendor.DIGIPOST);
        verify(service).sendMessage(argThat(new IsDigitalMessage(ssn,postboksAdresse)));
    }

    @Test
    public void test_performance_test_send_message_without_postbox() throws IOException {
        controller.performanceTestSendMessage("ssn", PerformanceTestController.PerformanceTestSize.SIZE_10KB, "postboksAdresse", null);
        verify(service).sendMessage(any(Message.class));
    }

    @Test
    public void test_performance_test_send_message_without_postbox_and_address() throws IOException {
        controller.performanceTestSendMessage("ssn", PerformanceTestController.PerformanceTestSize.SIZE_10KB, null, null);
        verify(service).sendMessage(any(Message.class));
    }

    class IsDigitalMessage extends ArgumentMatcher<Message> {

        private final String userSnn;
        private final String postboxAddress;

        protected IsDigitalMessage(String userSnn, String postboxAddress) {
            this.userSnn = userSnn;
            this.postboxAddress = postboxAddress;
        }

        public boolean matches(Object o) {
            Message message = (Message) o;
            final boolean matchSsn = message.getSsn() != null && message.getSsn().equals(userSnn);
            final boolean matchPostboxAddress = message.getDigitalPost().getPostboxAddress() != null && message.getDigitalPost().getPostboxAddress().equals(postboxAddress);
            return message.isDigital() && matchSsn && matchPostboxAddress;
        }
    }
}
