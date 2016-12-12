package no.difi.sdp.testavsender.configuration.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.wss4j.common.ext.WSPasswordCallback;

public class ClientKeystorePasswordCallbackHandler implements CallbackHandler {

	private static Map<String, String> aliasPasswordMap = new HashMap<String, String>();
	
	@Override
	public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
		for (Callback callback : callbacks) {
        	WSPasswordCallback pc = (WSPasswordCallback) callback;
        	pc.setPassword(aliasPasswordMap.get(pc.getIdentifier()));
        }
	}
	
	public static void addPrivateKey(String keyAlias, String keyPassword) {
		aliasPasswordMap.put(keyAlias, keyPassword);
	}
	
}