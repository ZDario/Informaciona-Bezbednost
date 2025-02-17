package ib.project.keystore;

import java.security.KeyPair;
import java.security.cert.X509Certificate;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;

import ib.project.model.IssuerData;
import ib.project.model.SubjectData;

/**
 * Klasa koja sluzi za generisanje Signed sertifikata (neko drugi potpisuje sertifikat (Ne vlasnik!)).
 *
 */
public class SignedCertificate {

	private CertificateGenerator certificateGenerator = new CertificateGenerator();

	/**
	 * Metoda sluzi za generisanje sertifikata koji potpisuje neko drugi.
	 * 
	 * @param issuerData - podaci o izdavacu sertifikata
	 * @param subjectData - podaci o vlasniku sertifikata
	 * 
	 * @return Sertifikat
	 */
	public X509Certificate generateSignedCertificate(IssuerData issuerData, SubjectData subjectData) {
		return certificateGenerator.generateCertificate(issuerData, subjectData);
	}
	
	/**
	 * Metoda sluzi za generisanje para kljuceva - privatni i javni kljuc.
	 * 
	 * @return Par kljuceva privatni i javni
	 */
	public KeyPair generateKeyPair() {
		return certificateGenerator.generateKeyPair();
	}

	/**
	 * Metoda sluzi za generisanje X500Name objekta koji se koristi u sertifikatima.
	 * 
	 * @param commonName - puno ime pravnog ili privatnog lica kojem se sertifikat izdaje (vlasnika sertifikata)
	 * @param surname - prezime lica kojem se sertifikat izdaje
	 * @param givenName - ime lica kojem se sertifikat izdaje
	 * @param organization - naziv organizacije lica kojem se sertifikat izdaje
	 * @param organizationUnit - naziv organizacione jedinice lica kojem se sertifikat izdaje
	 * @param country - kod zemlje
	 * @param email - email vlasnika sertifikata
	 * @param UID - user ID
	 * 
	 * @return X500Name
	 */
	public X500Name generateX509Name(String commonName, String surname, String givenName, String organization,
			String organizationUnit, String country, String email, String UID) {
		
		X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
		
		builder.addRDN(BCStyle.CN, commonName);
		builder.addRDN(BCStyle.SURNAME, surname);
		builder.addRDN(BCStyle.GIVENNAME, givenName);
		builder.addRDN(BCStyle.O, organization);
		builder.addRDN(BCStyle.OU, organizationUnit);
		builder.addRDN(BCStyle.C, country);
		builder.addRDN(BCStyle.E, email);
		builder.addRDN(BCStyle.UID, UID);
		
		return builder.build();
	}
}
