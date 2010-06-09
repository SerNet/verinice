package sernet.verinice.encryption;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.CertificateException;

import sernet.verinice.encryption.impl.EncryptionException;

/**
 * Interface declaring the contract of the EncryptionService.
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

	/**
	 * Encrypts data received from the given OutputStream using the AES algorithm.
	 * 
	 * @param dataStream
	 *            the OutputStream providing the unencrypted data to encrypt
	 * @param password
	 *            the password used for encryption
	 * @return an OutputStream providing the encrypted data
	 * @throws EncryptionException
	 *             when a problem occured during the en- or decryption process
	 * @throws IOException
	 *             when there was a problem reading from the InputStream
	 */
	OutputStream encrypt(OutputStream unencryptedDataStream, char[] password)
			throws EncryptionException, IOException;

	/**
	 * Decrypts data received from the given InputStream using the AES algorithm.
	 * 
	 * @param encryptedInputStream
	 *            the InputStream providing the encrypted data to decrypt
	 * @param password
	 *            the password used for decryption
	 * @return an InputStream providing the decrypted data
	 * @throws EncryptionException
	 *             when a problem occured during the en- or decryption process
	 * @throws IOException
	 *             when there was a problem reading from the InputStream
	 */
	InputStream decrypt(InputStream encryptedInputStream, char[] password)
			throws EncryptionException, IOException;

	// ##### S/MIME Encryption #####

	/**
	 * Encrypts the given byte data with the given X.509 certificate file.
	 * 
	 * @param unencryptedByteData
	 *            the data to encrypt
	 * @param x509CertificateFile
	 *            X.509 certificate file used to encrypt the data
	 * @return the encrypted data as array of bytes
	 * @throws FileNotFoundException
	 *             if the given certificate file could not be found
	 * @throws CertificateException
	 *             if the given certificate was not in expected format or if it was not or not yet
	 *             valid
	 * @throws EncryptionException
	 *             when a problem occured during the encryption process
	 */
	byte[] encrypt(byte[] unencryptedByteData, File x509CertificateFile)
			throws FileNotFoundException, CertificateException, EncryptionException;

}
