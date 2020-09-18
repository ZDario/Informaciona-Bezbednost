package ib.project.keystore;

import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;

import ib.project.model.IssuerData;

import org.bouncycastle.asn1.x500.X500Name;

public class KeyStoreReader {
	KeyStore keyStore;

	public KeyStoreReader() throws KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException, IOException {
		keyStore = keyStore.getInstance("JKS", "SUN");
		keyStore.load(null);
	}
	
	public void load(InputStream is, String password) throws NoSuchAlgorithmException, CertificateException, IOException {
		keyStore.load(is, password.toCharArray());
	}
	
	public Certificate getCertificate(String alias) {
		try {
			
			return keyStore.getCertificate(alias);
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}
	
	public PrivateKey getKey(String alias, String password) {
		try {
			return (PrivateKey)keyStore.getKey(alias, password.toCharArray());
		} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public PublicKey getPublicKeyFromCertificate(Certificate certificate) {
		return certificate.getPublicKey();
	}
	
	
	  public IssuerData getIssuerFromCertificate(Certificate certificate,
	  PrivateKey privateKey) { try { X509Certificate x509Certificate =
	  (X509Certificate) certificate; JcaX509CertificateHolder certificateHolder =
	  new JcaX509CertificateHolder(x509Certificate);
	  
	  X500Name issuerName = certificateHolder.getIssuer(); return new
	  IssuerData(privateKey, issuerName); } catch (CertificateEncodingException e)
	  { e.printStackTrace(); }
	  
	  return null; }
	 
	

}
