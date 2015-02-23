package no.difi.sdp.webclient.service;

import org.junit.Before;
import org.mockito.MockitoAnnotations;

/**
 *
 * Includes before method that must be run before all Mockito tests.
 *
 */
public class BaseTest {

    /**
     * Inject mocks.
     */
    @Before
    public void runBeforeEachTest(){
        MockitoAnnotations.initMocks(this);
    }


}
