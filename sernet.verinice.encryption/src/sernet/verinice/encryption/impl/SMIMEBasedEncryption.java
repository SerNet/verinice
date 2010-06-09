package sernet.verinice.encryption.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

import org.bouncycastle.mail.smime.SMIMEEnvelopedGenerator;
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.mail.smime.SMIMEUtil;

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
	 *            .pem format (BASE64 encoded, beginning with -----BEGIN CERTIFICATE----- and ending
	 *            with -----END CERTIFICATE-----)
	 * @return the encrypted data as array of bytes
	 * @throws FileNotFoundException
	 *             if the given certificate file could not be found
	 * @throws CertificateException
	 *             if the given certificate was not in expected format or if it was not or not yet
	 *             valid
	 * @throws EncryptionException
	 *             if a problem occured during the encryption process
	 */
	public static byte[] encrypt(byte[] unencryptedByteData, File x509CertificateFile)
			throws FileNotFoundException, CertificateException, EncryptionException {

		byte[] encryptedData = new byte[] {};

		X509Certificate certificate = null;
		try {
			FileInputStream certFileInputStream = new FileInputStream(x509CertificateFile);

			CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509", "BC");
			certificate = (X509Certificate) certificateFactory
					.generateCertificate(certFileInputStream);

			// Checks if the certificate is (still) valid.
			// If not throws a CertificateExpiredException or CertificateNotYetValidException
			certificate.checkValidity();

			SMIMEEnvelopedGenerator generator = new SMIMEEnvelopedGenerator();
			generator.addKeyTransRecipient(certificate);
			MimeBodyPart unencryptedContent = SMIMEUtil.toMimeBodyPart(unencryptedByteData);

			// Encrypt
			MimeBodyPart encryptedContent = generator.generate(unencryptedContent,
					SMIMEEnvelopedGenerator.AES256_CBC, "BC");

			// Create a MimeMessage and get encoded bytes from it
			MimeMessage finalbody = new MimeMessage(Session.getInstance(new Properties()));
			finalbody.setContent(encryptedContent.getContent(), encryptedContent.getContentType());
			finalbody.saveChanges();
			ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
			finalbody.writeTo(byteOutStream);
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

		} catch (FileNotFoundException fnfe) {
			throw new FileNotFoundException("The given certificate file was not found.");
		} catch (IOException ioe) {
			throw new EncryptionException(
					"There was an IO problem during the en- or decryption process. "
							+ "See the stacktrace for details.", ioe);
		}
		return encryptedData;
	}
}
