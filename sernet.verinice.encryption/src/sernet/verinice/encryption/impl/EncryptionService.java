package sernet.verinice.encryption.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import sernet.verinice.encryption.IEncryptionService;

/**
 * <p>
 * Service class providing methods for password based and certificate based 
 * encryption and decryption of data.
 * </p>
 * <p>
 * For en- and decryption this class uses the AES algorithm. Since this algorithm is not supported
 * by the <i>Java Cryptography Extension (JCE)</i> the BouncyCastle library is used. Please make
 * sure that this library is on your classpath.<br/>
 * More information on BouncyCastle can be found on <a
 * href="http://www.bouncycastle.org">http://www.bouncycastle.org</a>.
 * 
 * @author sengel <s.engel.@tarent.de>
 * 
 */
public class EncryptionService implements IEncryptionService {

	/**
	 * Creates a new instance of EncryptionService. Adds BouncyCastle as a SecurityProvider.
	 */
	public EncryptionService() {
		// If not already available, add the BounceCastle security provider,
		// since JSE doesn't provide password based encryption with AES.
		if (Security.getProvider("BC") == null) {
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

}
