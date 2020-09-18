package ib.project.keystore;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import ib.project.model.IssuerData;
import ib.project.model.SubjectData;

/**
 * Klasa koja sluzi za generisanje Self-Signed sertifikata (vlasnik sam sebi potpisuje sertifikat).
 *
 */
public class CertificateGenerator {

	// static blok - sluzi sa staticku inicijalizaciju klase. 
	// Ovaj kod se izvrsava samo jednom - pre prvog instanciranja objekta klase CertificateGenerator.
	// Vise informacija: https://www.geeksforgeeks.org/g-fact-79/ i https://www.edureka.co/blog/static-block-in-java/

	// registracija providera - koristimo BouncyCastle provider
	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	/**
	 * Metoda koja sluzi za generisanje sertifikata.
	 * 
	 * @param issuerData - podaci o izdavaocu sertifikata
	 * @param subjectData - podaci o fizickom ili pravnom licu kojem se izdjae sertifikat
	 * 
	 * @return X509 sertifikat
	 * 
	 */
	public X509Certificate generateCertificate(IssuerData issuerData, SubjectData subjectData) {
		try {
			 // Posto klasa za generisanje sertifiakta ne moze da primi direktno privatni
			 // kljuc, pravi se builder za objekat koji ce sadrzati privatni kljuc. 	
			 // Objekat ce se ce se koristitit za potpisivanje sertifikata. 
			 // Parametar konstruktora je definicija algoritma koji ce se koristi za potpisivanje sertifiakta.
			JcaContentSignerBuilder builder = new JcaContentSignerBuilder("SHA256WithRSAEncryption");

			// postavljanje provider-a koji se koristi - BouncyCastleProvider
			builder = builder.setProvider("BC");

			// objekat koji sadrzi privatni kljuc i koji se koristi za potpisivanje sertifikata.
			// Koristimo privatni kljuc izdavaca sertifikata!!!
			ContentSigner contentSigner = builder.build(issuerData.getPrivateKey());

			// postavljaju se podaci za generisanje sertifiakta
			X509v3CertificateBuilder certtificateBuilder = new JcaX509v3CertificateBuilder(
					issuerData.getX500name(),						// ime izdavaca sertifikata
					new BigInteger(subjectData.getSerialNumber()), 	// serijski broj sertifikata
					subjectData.getStartDate(), 						// od kog trenutka sertifikat vazi (pre ovog datuma sertifikat ne vazi)
					subjectData.getEndDate(),						// do kog trenutka sertifikat vazi (posle ovog datuma sertifikat ne vazi)
					subjectData.getX500name(), 						// ime lica kojem se sertifikat izdaje
					subjectData.getPublicKey());						// javni kljuc koji se vezuje za sertifikat
			
			// generisanje sertifikata...
			// koristimo certtificateBuilder objekat koji smo inicijalizovali sa svim potrebnim informacijama. Kao parametr build metode
			// prosledjujemo contentSigner objekat koji sadrzi privatni kljuc izdavaca sertifikata kojim se vrsi potpisivanje sertifikata 
			X509CertificateHolder certificateHolder = certtificateBuilder.build(contentSigner);

			// certtificateBuilder generise sertifikat kao objekat klase X509CertificateHolder.
			// Sada je potrebno certificateHolder konvertovati u X509 sertifikat -> za to se koristi certificateConverter objekat
			JcaX509CertificateConverter certificateConverter = new JcaX509CertificateConverter();
			
			// postavljanje provider-a koji se koristi - BouncyCastleProvider
			certificateConverter = certificateConverter.setProvider("BC");

			// konvertuje objekat u X509 sertifikat i vraca ga kao pobvratnu vrednost metode
			return certificateConverter.getCertificate(certificateHolder);

		} catch (IllegalArgumentException | IllegalStateException | OperatorCreationException | CertificateException e){
			e.printStackTrace();
		}
		
		return null;
	}

	/**
	 * Metoda sluzi za generisanje para kljuceva - privatni i javni kljuc.
	 * 
	 * @return - Par kljuceva privatni i javni
	 * 
	 * Napomena: voditi racuna da se privatni i javni kljuc UVEK moraju koristiti u paru, u suprotnom kriptografski algoritmi 
	 * koji koriste privatne i javne kljuceve nece ispravno raditi.
	 */
	public KeyPair generateKeyPair() {
		try {
			// generator para kljuceva
			KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
			
			// inicijalizacija generatora, 1024 bitni kljuc
			keyGenerator.initialize(2048);

			// generise par kljuceva
			KeyPair pair = keyGenerator.generateKeyPair();

			return pair;

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Metoda sluzi za generisanje self-signed sertifikata,
	 * 
	 * @return Self-Signed X509 sertifikat
	 */
	public X509Certificate generateSelfSignedCertificate(KeyPair keyPair, String email) {
//		####### SUBJECT DATA #######
		
		// osnovni podaci o vlasniku sertifikata
		X500Name X500Name = generateX509Name(email, "FTN", "UNS", "RS", email);
		
		// postavljanje datuma vazenja sertifikata
		Date startDate, endDate = null;
		Calendar calendar = GregorianCalendar.getInstance();
		startDate = calendar.getTime(); // sertifikat vazi od trenutnog vremena (datum kreiranja sertifikata)
		calendar.setTime(startDate);
		calendar.add(GregorianCalendar.YEAR, 2); // sertifikat traje 2 godine od datuma kreiranja
		endDate = calendar.getTime();
		
		// serijski broj sertifikata
		String serialNumber = "100";
		
		// kreiranje objekta koji sadrzi sve potrebne informacije o za vlasnika sertifikata
		SubjectData subjectData = new SubjectData(keyPair.getPublic(), X500Name, serialNumber, startDate, endDate);
		
		// ####### ISSUER DATA #######
		// U ovom slucaju je ista osoba zato sto je self-signed
		
		IssuerData issuerData = new IssuerData(keyPair.getPrivate(), X500Name);
		
		// generise se sertifikat
		return generateCertificate(issuerData, subjectData);
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
	public X500Name generateX509Name(String commonName, String organization,
			String organizationUnit, String country, String email) {
		
		X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
		
		builder.addRDN(BCStyle.CN, commonName);
		//builder.addRDN(BCStyle.SURNAME, surname);
		//builder.addRDN(BCStyle.GIVENNAME, givenName);
		builder.addRDN(BCStyle.O, organization);
		builder.addRDN(BCStyle.OU, organizationUnit);
		builder.addRDN(BCStyle.C, country);
		builder.addRDN(BCStyle.E, email);
		//builder.addRDN(BCStyle.UID, UID);
		
		return builder.build();
	}
	
	/**
	 * Metoda koja na konzoli ispisuje sertifikat
	 * 
	 * @param certificate - sertifikat koji zelimo da ispisemo
	 */
	public static void printCertificate(X509Certificate certificate) {
		System.out.println("ISSUER: " + certificate.getIssuerX500Principal().getName());
		System.out.println("SUBJECT: " + certificate.getSubjectX500Principal().getName());
		System.out.println("Sertifikat:");
		System.out.println("\n");
		System.out.println(certificate);
		System.out.println("\n");
	}
	
}