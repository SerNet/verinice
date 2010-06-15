package sernet.verinice.encryption;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;

import sernet.verinice.encryption.impl.EncryptionException;

/**
 * Interface declaring the contract of the EncryptionService.
 * 
 * @author Sebastian Engel <s.engel@tarent.de>
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
	 *             if a problem occured during the encryption process
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
	 *             if a problem occured during the decryption process
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
	 *             if there was a problem reading from the InputStream
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
	 *             if there was a problem reading from the InputStream
	 */
	InputStream decrypt(InputStream encryptedInputStream, char[] password)
			throws EncryptionException, IOException;

	// ##### S/MIME Encryption #####


	/**
	 * Encrypts the given byte data with the given X.509 certificate file.
	 * 
	 * Since encryption is realized through S/MIME, the public certificate of the "receiver" is
	 * required. The certificate is expected to be in DER or PEM format.
	 * 
	 * 
	 * @param unencryptedByteData
	 *            an array of byte data to encrypt
	 * @param x509CertificateFile
	 *            X.509 certificate file used to encrypt the data. The file is expected to be in DER
	 *            or PEM format
	 * @return an array of bytes representing a MimeBodyPart that contains the encrypted content
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
	byte[] encrypt(byte[] unencryptedByteData, File x509CertificateFile)
		throws CertificateNotYetValidException, CertificateExpiredException, 
		CertificateException, EncryptionException, IOException;
	

	/**
	 * Decrypts the given byte data with the given receiver certificate and the private key
	 * 
	 * @param encryptedByteData
	 *            an array of byte data to decrypt
	 * @param x509CertificateFile
	 *            X.509 certificate that was used to encrypt the data. The file is expected to be in
	 *            DER or PEM format
	 * @param privateKeyPemFile
	 *            .pem file that contains the private key used for decryption. This key must fit to
	 *            the public key contained in the public certificate
	 * @return an array of bytes representing the unencrypted byte data.
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
	byte[] decrypt(byte[] encryptedByteData, File x509CertificateFile, File privateKeyPemFile)
		throws IOException, CertificateNotYetValidException, CertificateExpiredException, 
		CertificateException, EncryptionException;
}
