package sernet.verinice.encryption.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMEEnvelopedGenerator;
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.mail.smime.SMIMEUtil;

import sernet.verinice.encryption.impl.util.CertificateUtils;

/**
 * Abstract utility class providing static methods for S/MIME based encryption.
 * 
 * <p>
 * Information on S/MIME can be found in <a href="http://tools.ietf.org/html/rfc3851">RFC2898</a>.
 * </p>
 * 
 * @author sengel <s.engel.@tarent.de>
 * 
 */
public class SMIMEBasedEncryption {

	/**
	 * Encrypts the given byte data with the given X.509 certificate file.
	 * 
	 * 
	 * @param unencryptedByteData
	 *            the data to encrypt
	 * @param x509CertificateFile
	 *            X.509 certificate file used to encrypt the data. The file is expected to be in
	 *            DER or PEM format (BASE64 encoded, beginning with -----BEGIN CERTIFICATE----- 
	 *            and ending with -----END CERTIFICATE-----)
	 * @return the encrypted data as array of bytes
	 * @throws IOException
	 *             <ul>
	 *             <li>if the given file does not exist</li>
	 *             <li>if the given file is cannot be read</li>
	 *             </ul>
	 * @throws CertificateNotYetValidException
	 *             if the certificate is not yet valid
	 * @throws CertificateExpiredException
	 *             if the certificate is not valid anymore
	 * @throws CertificateException
	 *             <ul>
	 *             <li>if the given file is not a certificate file</li>
	 *             <li>if the certificate contained in the given file is not a X.509 certificate</li>
	 *             </ul> 
	 * @throws EncryptionException
	 *             if a problem occured during the encryption process
	 */
	public static byte[] encrypt(byte[] unencryptedByteData, File x509CertificateFile) throws 
		IOException, CertificateNotYetValidException, CertificateExpiredException,
		CertificateException, EncryptionException {

		byte[] encryptedData = new byte[] {};

		X509Certificate x509Certificate = 
			CertificateUtils.loadX509CertificateFromFile(x509CertificateFile);
		
		try {
			SMIMEEnvelopedGenerator generator = new SMIMEEnvelopedGenerator();
			generator.addKeyTransRecipient(x509Certificate);
			MimeBodyPart unencryptedContent = SMIMEUtil.toMimeBodyPart(unencryptedByteData);

			// Encrypt
			MimeBodyPart encryptedContent = generator.generate(unencryptedContent,
					SMIMEEnvelopedGenerator.AES256_CBC, BouncyCastleProvider.PROVIDER_NAME);

			// Create a MimeMessage ...
			MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
			mimeMessage.setContent(encryptedContent.getContent(), encryptedContent.getContentType());
			mimeMessage.saveChanges();

			// ... and get the encoded bytes from it
			ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
			mimeMessage.writeTo(byteOutStream);
			encryptedData = byteOutStream.toByteArray();

		} catch (GeneralSecurityException e) {
			throw new EncryptionException(
					"There was a problem during the en- or decryption process. "
							+ "See the stacktrace for details.", e);
		} catch (SMIMEException smimee) {
			throw new EncryptionException(
					"There was a problem during the en- or decryption process. "
							+ "See the stacktrace for details.", smimee);
		} catch (MessagingException e) {
			throw new EncryptionException(
					"There was a problem during the en- or decryption process. "
							+ "See the stacktrace for details.", e);
		} catch (IOException ioe) {
			throw new EncryptionException(
					"There was an IO problem during the en- or decryption process. "
							+ "See the stacktrace for details.", ioe);
		}
		return encryptedData;
	}

}
