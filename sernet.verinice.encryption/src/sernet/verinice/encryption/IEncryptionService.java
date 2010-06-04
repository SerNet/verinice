/**
 * 
 */
package sernet.verinice.encryption;

import sernet.verinice.encryption.impl.EncryptionException;

/**
 * 
 * @author sengel <s.engel.@tarent.de>
 *
 */
public interface IEncryptionService {

	/**
	 * Encrypts the given byte data with the given password using the AES algorithm.
	 * 
	 * @param unencryptedByteData
	 *            the data to encrypt
	 * @param password
	 *            the password used for encryption
	 * @return the encrypted data as array of bytes
	 * @throws EncryptionException
	 *             when a problem occured during the encryption process
	 */
	byte[] encrypt(byte[] unencryptedByteData, char[] password) throws EncryptionException;
	
	/**
	 * Decrypts the given byte data with the given password using the AES algorithm.
	 * 
	 * @param encryptedByteData
	 *            the data to decrypt
	 * @param password
	 *            the password used for decryption
	 * @return the decrypted data as array of bytes
	 * @throws EncryptionException
	 *             when a problem occured during the decryption process
	 */
	byte[] decrypt(byte[] encryptedByteData, char[] password) throws EncryptionException;
}
