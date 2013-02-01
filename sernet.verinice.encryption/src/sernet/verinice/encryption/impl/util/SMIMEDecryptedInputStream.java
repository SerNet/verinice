package sernet.verinice.encryption.impl.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.RecipientId;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.RecipientInformationStore;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientId;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMEEnveloped;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;
import org.bouncycastle.util.encoders.Base64;

import sernet.verinice.interfaces.encryption.EncryptionException;

/**
 * Class representing a S/MIME encrypted InputStream that is decrypted using the 
 * given X.509 certificate file that was used for encryption and the matching 
 * private key file.
 * 
 * <p>
 * Use the available() method of this class to determine the number of bytes available to read.
 * </p>
 * 
 * @author Sebastian Engel <sengel@tarent.de>
 * 
 */
public class SMIMEDecryptedInputStream extends FilterInputStream {

	private byte[] decryptedByteData = new byte[] {};
	
	private static final String STD_ERR_MSG = "There was an IO problem during the en- or decryption process. See the stacktrace for details.";

	/**
     * Creates a new S/MIME encrypted InputStream that is decrypted using the 
     * given X.509 public certificate file that was used for encryption and the
     * matching private key file. Both files are expected to be in PEM format. 
     * 
     * @param encryptedInputStream the InputStream to decrypt
     * @param x509CertificateFile the X.509 public certificate that was used for encryption 
     * @param privateKeyFile the matching private key file needed for decryption
     * @throws IOException
     *             <ul>
     *             <li>if any of the given files does not exist</li>
     *             <li>if any of the given files cannot be read</li>
     *             </ul>
     * @throws CertificateNotYetValidException
     *             if the certificate is not yet valid
     * @throws CertificateExpiredException
     *             if the certificate is not valid anymore
     * @throws CertificateException
     *             <ul>
     *             <li>if the given certificate file does not contain a certificate</li>
     *             <li>if the certificate contained in the given file is not a X.509 certificate</li>
     *             </ul>
     * @throws EncryptionException
     *             if a problem occured during the encryption process
     */
    public SMIMEDecryptedInputStream(InputStream encryptedInputStream, File x509CertificateFile, 
            File privateKeyFile) 
        throws CertificateException, IOException, EncryptionException {
        this(encryptedInputStream,x509CertificateFile,privateKeyFile,null);
    }
	
	/**
	 * Creates a new S/MIME encrypted InputStream that is decrypted using the 
	 * given X.509 public certificate file that was used for encryption and the
	 * matching private key file. Both files are expected to be in PEM format. 
	 * 
	 * @param encryptedInputStream the InputStream to decrypt
	 * @param x509CertificateFile the X.509 public certificate that was used for encryption 
	 * @param privateKeyFile the matching private key file needed for decryption
	 * @param privateKeyPassword password to encrypt private key
	 * @throws IOException
	 *             <ul>
	 *             <li>if any of the given files does not exist</li>
	 *             <li>if any of the given files cannot be read</li>
	 *             </ul>
	 * @throws CertificateNotYetValidException
	 *             if the certificate is not yet valid
	 * @throws CertificateExpiredException
	 *             if the certificate is not valid anymore
	 * @throws CertificateException
	 *             <ul>
	 *             <li>if the given certificate file does not contain a certificate</li>
	 *             <li>if the certificate contained in the given file is not a X.509 certificate</li>
	 *             </ul>
	 * @throws EncryptionException
	 *             if a problem occured during the encryption process
	 */
	public SMIMEDecryptedInputStream(InputStream encryptedInputStream, File x509CertificateFile, 
			File privateKeyFile, final String privateKeyPassword) 
		throws CertificateException, IOException, EncryptionException {
		super(encryptedInputStream);
		
		X509Certificate x509Certificate = CertificateUtils.loadX509CertificateFromFile(x509CertificateFile);
		
		// The recipient's private key
		FileReader fileReader = new FileReader(privateKeyFile);
		PasswordFinder passwordFinder = new PasswordFinder() { 
            @Override
            public char[] getPassword() {
                return (privateKeyPassword!=null) ? privateKeyPassword.toCharArray() : null;
            }
        };
        PEMReader pemReader = null;
        if(passwordFinder.getPassword()!=null) {
            pemReader = new PEMReader(fileReader,passwordFinder);
        } else {
           pemReader = new PEMReader(fileReader);
        }
		KeyPair keyPair = (KeyPair) pemReader.readObject();
		PrivateKey privateKey = keyPair.getPrivate();
		
		try {
			MimeBodyPart encryptedMimeBodyPart = new MimeBodyPart(encryptedInputStream);
			SMIMEEnveloped enveloped = new SMIMEEnveloped(encryptedMimeBodyPart);

			// look for our recipient identifier
			RecipientId recipientId = new JceKeyTransRecipientId(x509Certificate);

			RecipientInformationStore recipients = enveloped.getRecipientInfos();
			RecipientInformation recipientInfo = recipients.get(recipientId);

			if (recipientInfo != null) {
            	JceKeyTransRecipient rec = new JceKeyTransEnvelopedRecipient(privateKey);
            	rec.setProvider("SunPKCS11-verinice");
            	rec.setContentProvider(BouncyCastleProvider.PROVIDER_NAME);
				decryptedByteData = recipientInfo.getContent(rec);
				decryptedByteData = Base64.decode(decryptedByteData);
			}
			ByteArrayInputStream byteInStream = new ByteArrayInputStream(decryptedByteData);
			super.in = byteInStream;
		} catch (MessagingException e) {
			throw new EncryptionException(STD_ERR_MSG, e);
		} catch (CMSException e) {
			throw new EncryptionException(STD_ERR_MSG, e);
		}
	}

	public SMIMEDecryptedInputStream(InputStream encryptedInputStream, String keyAlias) 
		throws CertificateException, IOException, EncryptionException {
		super(encryptedInputStream);
		
		try {
			KeyStore ks = KeyStore.getInstance("PKCS11", "SunPKCS11-verinice");
			ks.load(null, null);
			Certificate cert = ks.getCertificate(keyAlias);
			X509Certificate x509Certificate = (X509Certificate) cert;

			PrivateKey privateKey = (PrivateKey) ks.getKey(keyAlias, null);

			MimeBodyPart encryptedMimeBodyPart = new MimeBodyPart(encryptedInputStream);
			SMIMEEnveloped enveloped = new SMIMEEnveloped(encryptedMimeBodyPart);

			// look for our recipient identifier
			RecipientId recipientId = new JceKeyTransRecipientId(x509Certificate);

			RecipientInformationStore recipients = enveloped.getRecipientInfos();
			RecipientInformation recipientInfo = recipients.get(recipientId);

			if (recipientInfo != null) {
            	JceKeyTransRecipient rec = new JceKeyTransEnvelopedRecipient(privateKey);
            	rec.setProvider("SunPKCS11-verinice");
            	rec.setContentProvider(BouncyCastleProvider.PROVIDER_NAME);
				decryptedByteData = recipientInfo.getContent(rec);
				decryptedByteData = Base64.decode(decryptedByteData);
			}
			ByteArrayInputStream byteInStream = new ByteArrayInputStream(decryptedByteData);
			super.in = byteInStream;
		} catch (GeneralSecurityException e) {
			throw new EncryptionException(STD_ERR_MSG, e);
		} catch (MessagingException e) {
			throw new EncryptionException(STD_ERR_MSG, e);
		} catch (CMSException e) {
			throw new EncryptionException(STD_ERR_MSG, e);
		}
	}
	
	/**
	 * @return the number of bytes that can be read from this InputStream.
	 */
	@Override
	public int available() throws IOException {
		return decryptedByteData.length;
	}
}
