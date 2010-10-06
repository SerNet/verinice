package sernet.verinice.encryption.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import sernet.verinice.interfaces.encryption.EncryptionException;
import sernet.verinice.interfaces.encryption.IEncryptionService;

/**
 * <p>
 * Service class providing methods for password based and certificate based encryption and
 * decryption of data.
 * </p>
 * <p>
 * For en- and decryption this class uses the AES algorithm. Since this algorithm is not supported
 * by the <i>Java Cryptography Extension (JCE)</i> the BouncyCastle library is used. Please make
 * sure that this library is on your classpath.<br/>
 * More information on BouncyCastle can be found on <a
 * href="http://www.bouncycastle.org">http://www.bouncycastle.org</a>.
 * 
 * @author Sebastian Engel <s.engel@tarent.de>
 * 
 */
public class EncryptionService implements IEncryptionService {

	/**
	 * Creates a new instance of EncryptionService. Adds BouncyCastle as a SecurityProvider.
	 */
	public EncryptionService() {
		// If not already available, add the BounceCastle security provider,
		// since JSE doesn't provide password based encryption with AES.
		if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
			Security.addProvider(new BouncyCastleProvider());
		}
	}

	@Override
	public byte[] encrypt(byte[] unencryptedByteData, char[] password) throws EncryptionException {
		return PasswordBasedEncryption.encrypt(unencryptedByteData, password);
	}

	@Override
	public byte[] decrypt(byte[] encryptedByteData, char[] password) throws EncryptionException {
		return PasswordBasedEncryption.decrypt(encryptedByteData, password);
	}

	@Override
	public OutputStream encrypt(OutputStream unencryptedDataStream, char[] password)
			throws EncryptionException, IOException {
		return PasswordBasedEncryption.encrypt(unencryptedDataStream, password);
	}

	@Override
	public InputStream decrypt(InputStream encryptedInputStream, char[] password)
			throws EncryptionException, IOException {
		return PasswordBasedEncryption.decrypt(encryptedInputStream, password);
	}

	@Override
	public byte[] encrypt(byte[] unencryptedByteData, File x509CertificateFile)
			throws CertificateNotYetValidException, CertificateExpiredException,
			CertificateException, EncryptionException, IOException {
		return SMIMEBasedEncryption.encrypt(unencryptedByteData, x509CertificateFile);
	}

	@Override
	public byte[] decrypt(byte[] encryptedByteData, File x509CertificateFile, File privateKeyPemFile)
			throws IOException, CertificateNotYetValidException, CertificateExpiredException,
			CertificateException, EncryptionException {
		return SMIMEBasedEncryption.decrypt(encryptedByteData, x509CertificateFile,
				privateKeyPemFile);
	}
	
	
	/* (non-Javadoc)
     * @see sernet.verinice.interfaces.encryption.IEncryptionService#decrypt(java.io.InputStream, java.io.File, java.io.File, java.lang.String)
     */
    @Override
    public InputStream decrypt(InputStream encryptedDataStream, File x509CertificateFile, File privateKeyPemFile, String privateKeyPassword) 
        throws IOException, CertificateNotYetValidException, CertificateExpiredException, 
        CertificateException, EncryptionException {
        return SMIMEBasedEncryption.decrypt(encryptedDataStream, x509CertificateFile,
                privateKeyPemFile,privateKeyPassword);
    }
	
	@Override
    public byte[] decrypt(byte[] encryptedByteData, File x509CertificateFile, File privateKeyPemFile, final String privateKeyPassword)
            throws IOException, CertificateNotYetValidException, CertificateExpiredException,
            CertificateException, EncryptionException {
        return SMIMEBasedEncryption.decrypt(encryptedByteData, x509CertificateFile,
                privateKeyPemFile, privateKeyPassword);
    }
	
	@Override
	public OutputStream encrypt(OutputStream unencryptedDataStream, File x509CertificateFile)
		throws IOException, CertificateNotYetValidException, CertificateExpiredException, 
		CertificateException, EncryptionException {
		return SMIMEBasedEncryption.encrypt(unencryptedDataStream, x509CertificateFile);
	}
	
	@Override
	public InputStream decrypt(InputStream encryptedDataStream, File x509CertificateFile, 
			File privateKeyFile) throws IOException, CertificateNotYetValidException, 
			CertificateExpiredException, CertificateException, EncryptionException {
		return SMIMEBasedEncryption.decrypt(encryptedDataStream, x509CertificateFile, 
				privateKeyFile);
	}
}
