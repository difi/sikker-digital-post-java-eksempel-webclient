package no.difi.sdp.webclient.service;

import no.difi.sdp.webclient.configuration.SdpClientConfiguration;
import no.difi.sdp.webclient.service.MessageService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SdpClientConfiguration.class})
@WebAppConfiguration
public class MessageServiceTest {

	@Autowired
	private MessageService messageService;

	@Test
    public void test_that_the_spring_context_is_successfully_initialized() {
    	messageService.getCountByStatus(); // Any method call
    }
    
}
