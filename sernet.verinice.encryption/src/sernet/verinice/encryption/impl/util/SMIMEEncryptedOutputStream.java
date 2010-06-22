package sernet.verinice.encryption.impl.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMEEnvelopedGenerator;
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.mail.smime.SMIMEUtil;

import sernet.verinice.encryption.EncryptionException;

/**
 * Class representing an OutputStream that is encrypted using a given x.509 certificate file 
 * and the AES256 CBC algorithm. 
 * 
 * @author Sebastian Engel <sengel@tarent.de>
 * 
 */
public class SMIMEEncryptedOutputStream extends FilterOutputStream {

	private X509Certificate x509Certificate;
	private ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
	private byte[] oneByteArray = new byte[1];
	private byte[] result;

	public SMIMEEncryptedOutputStream(OutputStream out, File x509CertificateFile)
			throws CertificateNotYetValidException, CertificateExpiredException,
			CertificateException, IOException {

		super(out);
		x509Certificate = CertificateUtils.loadX509CertificateFromFile(x509CertificateFile);
	}

	private byte[] encrypt(byte[] unencryptedByteData) throws IOException, EncryptionException {

		byte[] encryptedMimeData = new byte[] {};
		
		try {
			SMIMEEnvelopedGenerator generator = new SMIMEEnvelopedGenerator();
			generator.addKeyTransRecipient(x509Certificate);
			MimeBodyPart unencryptedContent = SMIMEUtil.toMimeBodyPart(unencryptedByteData);

			// Encrypt the byte data and make a MimeBodyPart from it
			MimeBodyPart encryptedMimeBodyPart = generator.generate(unencryptedContent,
					SMIMEEnvelopedGenerator.AES256_CBC, BouncyCastleProvider.PROVIDER_NAME);

			// Finally get the encoded bytes from the MimeMessage and return them
			encryptedMimeBodyPart.writeTo(byteOutStream);
			encryptedMimeData = byteOutStream.toByteArray();

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
		return encryptedMimeData;
	}

	@Override
	public void write(int b) throws IOException {
		oneByteArray[0] = (byte) b;

		result = encrypt(oneByteArray);
		if (result != null) {
			out.write(result);
		}
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if (len == 0) {
			return;
		}

		byte[] tempArray = new byte[len];

		for (int index = 0; index < len; index++) {
			tempArray[index] = b[off];
			off++;
		}
		result = encrypt(tempArray);
		out.write(result);
	}

	@Override
	public void flush() throws IOException {
		super.flush();
	}

	@Override
	public void close() throws IOException {
		if (out != null) {
			super.flush();
			super.close();
		}
	}
}
