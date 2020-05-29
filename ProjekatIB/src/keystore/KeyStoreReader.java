package keystore;

import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

public class KeyStoreReader {
	KeyStore keystore;

	public void load(InputStream is, String password) throws NoSuchAlgorithmException, CertificateException, IOException {
		keystore.load(is, password.toCharArray());
	}
	
	public KeyStoreReader() throws KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException, IOException {
		keystore = keystore.getInstance("JKS", "SUN");
		keystore.load(null);
	}
	
	public PrivateKey getKey(String alias, String password) {
		try {
			return (PrivateKey)keystore.getKey(alias, password.toCharArray());
		} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Certificate getCertificate(String alias) {
		try {
			
			return keystore.getCertificate(alias);
		} catch (KeyStoreException e) {
			e.printStackTrace();
			return null;
		}

	}

}