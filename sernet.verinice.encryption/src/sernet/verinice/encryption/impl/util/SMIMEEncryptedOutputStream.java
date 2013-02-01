package sernet.verinice.encryption.impl.util;

import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;

import sernet.verinice.encryption.impl.SMIMEBasedEncryption;
import sernet.verinice.interfaces.encryption.EncryptionException;

/**
 * Class representing an OutputStream that is encrypted using a given x.509 certificate file 
 * and the AES256 CBC algorithm. 
 * 
 * @author Sebastian Engel <sengel@tarent.de>
 * 
 */
public class SMIMEEncryptedOutputStream extends FilterOutputStream {

	private File x509CertificateFile;
	private String keyAlias;
	
	private byte[] result;

	public SMIMEEncryptedOutputStream(OutputStream out, File x509CertificateFile)
			throws CertificateException, IOException {

		super(out);
		this.x509CertificateFile = x509CertificateFile;
	}

	public SMIMEEncryptedOutputStream(OutputStream out, String keyAlias)
	throws CertificateException, IOException {
		super(out);
		this.keyAlias = keyAlias;
	}

	private byte[] encrypt(byte[] unencryptedByteData) throws IOException, EncryptionException {	
		try {
			if (x509CertificateFile != null){
				return SMIMEBasedEncryption.encrypt(unencryptedByteData, x509CertificateFile);
			} else {
				return SMIMEBasedEncryption.encrypt(unencryptedByteData, keyAlias);
			}
		} catch (GeneralSecurityException e) {
			throw new EncryptionException(
					"There was a problem during the en- or decryption process. See the stacktrace for details.", e);
		} catch (IOException ioe) {
			throw new EncryptionException(
					"There was an IO problem during the en- or decryption process. See the stacktrace for details.", ioe);
		}
	}

	@Override
	public void write(int b) throws IOException {
	    byte[] oneByteArray = new byte[1];
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
		int off_0 = off;
	    if (len == 0) {
			return;
		}

		byte[] tempArray = new byte[len];

		for (int index = 0; index < len; index++) {
			tempArray[index] = b[off_0];
			off_0++;
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
