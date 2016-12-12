package no.difi.sdp.testavsender.configuration.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class CryptoUtil {

	public X509Certificate loadX509Certificate(byte[] x509CertificateBytes) {
		CertificateFactory certificateFactory = null;
		try {
			certificateFactory = CertificateFactory.getInstance("X.509");
		} catch (CertificateException e) {
			throw new RuntimeException(e);
		}
		try {
			return (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(x509CertificateBytes));
		} catch (CertificateException e) {
			throw new RuntimeException(e);
		}
	}
	
	public KeyStore loadKeystore(String keystoreType, String keystoreName, String keystorePassword) {
		KeyStore keyStore = null; 
    	try {
			keyStore = KeyStore.getInstance(keystoreType);
		} catch (KeyStoreException e) {
			throw new RuntimeException(e);
		}
    	InputStream inputStream = getClass().getClassLoader().getResourceAsStream(keystoreName);
    	char[] password = keystorePassword.toCharArray();
    	try {
			keyStore.load(inputStream, password);
			return keyStore;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public List<String> getKeypairAliases(KeyStore keyStore) {
		List<String> aliases = new ArrayList<String>();
		
		try {
			Enumeration<String> enumeration = keyStore.aliases();
			while (enumeration.hasMoreElements()) {
				String alias = enumeration.nextElement();
				if (keyStore.isKeyEntry(alias)) {
					aliases.add(alias);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return aliases;
	}

	public List<String> getCertificateAliases(KeyStore keyStore) {
		List<String> aliases = new ArrayList<>();

		try {
			Enumeration<String> enumeration = keyStore.aliases();
			while (enumeration.hasMoreElements()) {
				String alias = enumeration.nextElement();
				if (keyStore.isCertificateEntry(alias)) {
					aliases.add(alias);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return aliases;
	}
	
}
