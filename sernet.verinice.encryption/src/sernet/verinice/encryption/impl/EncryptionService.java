package sernet.verinice.encryption.impl;

import java.security.GeneralSecurityException;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

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
 * <p>
 * Information on Password Based Encryption can be found in <a
 * href="http://www.ietf.org/rfc/rfc2898.txt">RFC2898</a>.
 * </p>
 * 
 * @author sengel <s.engel.@tarent.de>
 * 
 */
public class EncryptionService implements IEncryptionService {

	/**
	 * The salt used for Password Based En- and decryption. This should be at least 64 bit long ...
	 */
	private static final byte[] SALT = { (byte) 0xa3, (byte) 0x51, (byte) 0x56, (byte) 0x7b,
			(byte) 0x9d, (byte) 0xf5, (byte) 0xf3, (byte) 0xff };

	/**
	 * The iteration count used for Password Based Encryption. A length of at least 1000 is
	 * recommended.
	 */
	private static final int ITERATION_COUNT = 1200;

	/**
	 * The algorithm used for en- and decryption. ASE in is only available through the BouncyCastle
	 * library.
	 */
	private static final String ENCRYPTION_ALGORITHM = "PBEWITHSHA256AND256BITAES-CBC-BC";

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

	/**
	 * Encrypts the given byte data with the given password using AES.
	 * 
	 * @param unencryptedByteData
	 *            the data to encrypt
	 * @param password
	 *            the password used for encryption
	 * @return the encrypted data as array of bytes
	 * @throws EncryptionException
	 *             when a problem occured during the encryption process
	 */
	public byte[] encrypt(byte[] unencryptedByteData, char[] password) throws EncryptionException {
		return performPasswordEncryption(unencryptedByteData, password, false);
	}

	/**
	 * Decrypts the given byte data with the given password using AES.
	 * 
	 * @param encryptedByteData
	 *            the data to decrypt
	 * @param password
	 *            the password used for decryption
	 * @return the decrypted data as array of bytes
	 * @throws EncryptionException
	 *             when a problem occured during the decryption process
	 */
	public byte[] decrypt(byte[] encryptedByteData, char[] password) throws EncryptionException {
		return performPasswordEncryption(encryptedByteData, password, true);
	}

	// ##################################################################
	// ##### Static methods that actually do the en- and decryption #####
	// ##################################################################

	/**
	 * En- or decrypts the given byte data with the given password using AES.
	 * 
	 * @param byteData
	 *            the byte data to en- or decrypt
	 * @param password
	 *            the password used for en- or decryption
	 * @param decryptMode
	 *            if true, this method performs a decryption instead of encryption on the given data
	 * @return the en- or decrypted data as array of bytes
	 * @throws EncryptionException
	 *             when a problem occured during the encryption process
	 */
	private static byte[] performPasswordEncryption(byte[] byteData, char[] password,
			boolean decryptMode) throws EncryptionException {

		byte[] decryptedData = new byte[] {};

		PBEKeySpec pbeKeySpec = new PBEKeySpec(password);
		PBEParameterSpec pbeParameterSpec = new PBEParameterSpec(SALT, ITERATION_COUNT);

		try {
			SecretKeyFactory secretKeyFactory = 
				SecretKeyFactory.getInstance(ENCRYPTION_ALGORITHM, "BC");
			SecretKey pbeKey = secretKeyFactory.generateSecret(pbeKeySpec);

			// Generate and initialize a PBE cipher
			Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM, "BC");

			if (decryptMode) {
				cipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParameterSpec);
			} else {
				cipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParameterSpec);
			}

			// encrypt
			decryptedData = cipher.doFinal(byteData);

		} catch (GeneralSecurityException e) {
			throw new EncryptionException(
					"There was a problem during the en- or decryption process. "
							+ "See the stacktrace for details.", e);
		}
		return decryptedData;
	}

}
