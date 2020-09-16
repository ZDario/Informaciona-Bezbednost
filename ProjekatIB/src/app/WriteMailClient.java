package app;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.mail.internet.MimeMessage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xml.security.encryption.EncryptedData;
import org.apache.xml.security.encryption.EncryptedKey;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.Constants;
import org.apache.xml.security.utils.JavaUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.api.services.gmail.Gmail;

import model.mailclient.MailBody;
import keystore.KeyStoreReader;
import util.Base64;
import util.GzipUtil;
import util.IVHelper;
import support.MailHelper;
import support.MailWritter;

public class WriteMailClient extends MailClient {

	private static final String KEY_FILE = "./data/session.key";
	private static final String IV1_FILE = "./data/iv1.bin";
	private static final String IV2_FILE = "./data/iv2.bin";
	private static final String KEY_STORE_FILE = "./data/usera.jks";
	private static final String KEY_STORE_PASS = "123";
	private static final String KEY_STORE_PASS_FOR_PRIVATE_KEY = "123";
	private static final String KEY_STORE_ALIAS = "usera";
	private static final String OUT_FILE = "./data/signedEmail1.xml";
	 static {
		  	//staticka inicijalizacija
		      Security.addProvider(new BouncyCastleProvider());
		      org.apache.xml.security.Init.init();
		  }	
	
	private static KeyStoreReader keyStoreReader = new KeyStoreReader();
	public static void main(String[] args) {
		
        try {
        	Gmail service = getGmailService();
            
        	System.out.println("Insert a reciever:");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String reciever = reader.readLine();
        	
            System.out.println("Insert a subject:");
            String subject = reader.readLine();
            
            
            System.out.println("Insert body:");
            String body = reader.readLine();
            
            
            //Pravljenje XML fajla
        		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        		// root elements
        		Document doc = docBuilder.newDocument();
        		Element rootElement = doc.createElement("email");
        		doc.appendChild(rootElement);
        		

        		// subject elements
        		Element subjectel = doc.createElement("subject");
        		rootElement.appendChild(subjectel);
        		subjectel.appendChild(doc.createTextNode(subject));
        		// subject body
        		Element bodyel = doc.createElement("body");
        		rootElement.appendChild(bodyel);
        		bodyel.appendChild(doc.createTextNode(body));

        		// write the content into xml file
        		TransformerFactory transformerFactory = TransformerFactory.newInstance();
        		Transformer transformer = transformerFactory.newTransformer();
        		DOMSource source = new DOMSource(doc);
        		StreamResult result = new StreamResult(new File("./data/signedEmail.xml"));

        		// Output to console for testing
        		// StreamResult result = new StreamResult(System.out);

        		transformer.transform(source, result);

        		System.out.println("File saved!");

            
            //Compression
            String compressedSubject = Base64.encodeToString(GzipUtil.compress(subject));
            String compressedBody = Base64.encodeToString(GzipUtil.compress(body));
            
            //Key generation
            KeyGenerator keyGen = KeyGenerator.getInstance("AES"); 
			SecretKey secretKey = keyGen.generateKey();
			Cipher aesCipherEnc = Cipher.getInstance("AES/CBC/PKCS5Padding");
			
			//inicijalizacija za sifrovanje 
			IvParameterSpec ivParameterSpec1 = IVHelper.createIV();
			aesCipherEnc.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec1);
			
			IvParameterSpec ivParameterSpec2 = IVHelper.createIV();
			//sifrovanje
			byte[] ciphertext = aesCipherEnc.doFinal(compressedBody.getBytes());
			String ciphertextStr = Base64.encodeToString(ciphertext);
			System.out.println("Kriptovan tekst: " + ciphertextStr);
			
			//Preuzimanje setifikata
			KeyStore keystore= keyStoreReader.readKeyStore(KEY_STORE_FILE, KEY_STORE_PASS.toCharArray());
			Certificate certificate = keyStoreReader.getCertificateFromKeyStore(keystore, "userb");
			
			PublicKey publicKey = keyStoreReader.getPublicKeyFromCertificate(certificate);
			System.out.println("\nProcitan javni kljuc iz sertifikata: " + publicKey);
			Cipher encrypt=Cipher.getInstance("RSA/ECB/PKCS1Padding");
			encrypt.init(Cipher.WRAP_MODE, publicKey);
			byte[] ciphertext1= encrypt.wrap(secretKey);
			String secretKeyStr = Base64.encodeToString(ciphertext1);
			
			System.out.println("Kriptovan kljuc: " + secretKeyStr);
			
			MailBody mailbody=new MailBody(ciphertext, ivParameterSpec1.getIV(),ivParameterSpec2.getIV() , ciphertext1);
			String bodytext=mailbody.toCSV();
			String ceotext= ciphertextStr + ciphertext1;
			System.out.println("bodytext: " + bodytext);
			
			PrivateKey privateKey = keyStoreReader.getPrivateKeyFromKeyStore(keystore, KEY_STORE_ALIAS, KEY_STORE_PASS_FOR_PRIVATE_KEY.toCharArray());
			//POTPISIVANJE XML-a
			System.out.println("Signing....");
			doc = signDocument(doc, privateKey, certificate);
			saveDocument(doc, OUT_FILE);
			System.out.println("Signing of document done");
			//Enkripcija XML-a
			SecretKey secretKey1 = generateDataEncryptionKey();
			System.out.println("Encrypting....");
			doc = encrypt(doc, secretKey1, certificate);
			saveDocument(doc, "./data/emailPotpisanIEnkriptovan.xml");
			String bodytextxml=toString(doc);	
			
			
			byte[] ciphersubject = aesCipherEnc.doFinal(compressedSubject.getBytes());
			String ciphersubjectStr = Base64.encodeToString(ciphersubject);
			System.out.println("Kriptovan subject: " + ciphersubjectStr);
			
			//inicijalizacija za sifrovanje 
			
			aesCipherEnc.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec2);
			//snimaju se bajtovi kljuca i IV.
			JavaUtils.writeBytesToFilename(KEY_FILE, secretKey.getEncoded());
			JavaUtils.writeBytesToFilename(IV1_FILE, ivParameterSpec1.getIV());
			JavaUtils.writeBytesToFilename(IV2_FILE, ivParameterSpec2.getIV());
			
        	MimeMessage mimeMessage = MailHelper.createMimeMessage(reciever, ciphersubjectStr, bodytextxml);
        	MailWritter.sendMessage(service, "me", mimeMessage);
        	
        }catch (Exception e) {
        	e.printStackTrace();
		}
	}
	
	/**
	 * Snima DOM u XML fajl 
	 */
	private static void saveDocument(Document doc, String fileName) {
		try {
			File outFile = new File(fileName);
			FileOutputStream f = new FileOutputStream(outFile);

			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(f);
			
			transformer.transform(source, result);

			f.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static Document signDocument(Document doc, PrivateKey privateKey, Certificate cert) {
	      
	      try {
				Element rootEl = doc.getDocumentElement();
				
				//kreira se signature objekat
				XMLSignature sig = new XMLSignature(doc, null, XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1);
				
				//kreiraju se transformacije nad dokumentom
				Transforms transforms = new Transforms(doc);
				    
				//iz potpisa uklanja Signature element
				//Ovo je potrebno za enveloped tip po specifikaciji
				transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
				
				//normalizacija
				transforms.addTransform(Transforms.TRANSFORM_C14N_WITH_COMMENTS);
				    
				//potpisuje se citav dokument (URI "")
				sig.addDocument("", transforms, Constants.ALGO_ID_DIGEST_SHA1);
				    
				//U KeyInfo se postavalja Javni kljuc samostalno i citav sertifikat
				sig.addKeyInfo(cert.getPublicKey());
				sig.addKeyInfo((X509Certificate) cert);
				    
				//poptis je child root elementa
				rootEl.appendChild(sig.getElement());
				
				//potpisivanje
				sig.sign(privateKey);
				
				return doc;
				
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	private static SecretKey generateDataEncryptionKey() {

		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance("DESede"); // Triple
																			// DES
			return keyGenerator.generateKey();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}
	private static Document encrypt(Document doc, SecretKey key, Certificate certificate) {

		try {

			// cipher za kriptovanje XML-a
			XMLCipher xmlCipher = XMLCipher.getInstance(XMLCipher.TRIPLEDES);
			
			// inicijalizacija za kriptovanje
			xmlCipher.init(XMLCipher.ENCRYPT_MODE, key);

			// cipher za kriptovanje tajnog kljuca,
			// Koristi se Javni RSA kljuc za kriptovanje
			XMLCipher keyCipher = XMLCipher.getInstance(XMLCipher.RSA_v1dot5);
			
			// inicijalizacija za kriptovanje tajnog kljuca javnim RSA kljucem
			keyCipher.init(XMLCipher.WRAP_MODE, certificate.getPublicKey());
			
			// kreiranje EncryptedKey objekta koji sadrzi  enkriptovan tajni (session) kljuc
			EncryptedKey encryptedKey = keyCipher.encryptKey(doc, key);
			
			// u EncryptedData element koji se kriptuje kao KeyInfo stavljamo
			// kriptovan tajni kljuc
			// ovaj element je koreni elemnt XML enkripcije
			EncryptedData encryptedData = xmlCipher.getEncryptedData();
			
			// kreira se KeyInfo element
			KeyInfo keyInfo = new KeyInfo(doc);
			
			// postavljamo naziv 
			keyInfo.addKeyName("Kriptovani tajni kljuc");
			
			// postavljamo kriptovani kljuc
			keyInfo.add(encryptedKey);
			
			// postavljamo KeyInfo za element koji se kriptuje
			encryptedData.setKeyInfo(keyInfo);

			// trazi se element ciji sadrzaj se kriptuje
			NodeList odseci = doc.getElementsByTagName("odsek");
			
			xmlCipher.doFinal(doc, doc.getDocumentElement(), true); // kriptuje sa sadrzaj

			return doc;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 	
	}

	public static String toString(Document doc) {
	    try {
	        StringWriter sw = new StringWriter();
	        TransformerFactory tf = TransformerFactory.newInstance();
	        Transformer transformer = tf.newTransformer();
	        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	
	        transformer.transform(new DOMSource(doc), new StreamResult(sw));
	        return sw.toString();
	    } catch (Exception ex) {
	        throw new RuntimeException("Error converting to String", ex);
	    }
	}	
}