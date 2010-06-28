/**
 * 
 */
package sernet.verinice.encryption.impl.examples;

import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;

import sernet.verinice.encryption.impl.EncryptionService;
import sernet.verinice.interfaces.encryption.EncryptionException;
import sernet.verinice.interfaces.encryption.IEncryptionService;

/**
 * Example application that shows how to encrypt and decrypt an array of bytes with a X.509
 * certificate loaded from a PEM encoded file and the matching private key loaded from another 
 * PEM file.
 * 
 * @author Sebastian Engel <s.engel@tarent.de>
 * 
 */
class CertificateBasedByteArrayEncryption {

	/**
	 * The secret message that shall be encrypted.
	 */
	private static final String SECRET_MESSAGE = "Attack the mars at 4 o'clock.";

	private static IEncryptionService encryptionService = new EncryptionService();

	/**
	 * @param args
	 *            agrs[0] is expected to be the public certificate file in PEM format. args[1] is
	 *            expected to be the private key file in PEM format.
	 * 
	 * @throws IOException
	 * @throws EncryptionException
	 * @throws CertificateException
	 * @throws CertificateExpiredException
	 * @throws CertificateNotYetValidException
	 */
	public static void main(String[] args) throws CertificateNotYetValidException,
			CertificateExpiredException, CertificateException, EncryptionException, IOException {

		if (args.length < 2) {
			System.out.println("Usage: java CertificateBasedByteArrayEncryption " +
					"<certificate-file> <private-key-file>");
			return;
		}

		File certFile = new File(args[0]);
		File privateKeyFile = new File(args[1]);

		System.out.println("Certificate Based Encryption example for byte arrays");
		System.out.println("=====================================================");
		System.out.println();
		System.out.println("Secret message is: " + SECRET_MESSAGE);

		System.out.println();

		// Encrypt message with given certificate file
		byte[] encryptedMessage = encryptionService.encrypt(SECRET_MESSAGE.getBytes(), certFile);
		System.out.println("Encrypted message is:");
		System.out.println("======================");
		System.out.println(new String(encryptedMessage));

		System.out.println();

		// Decrypt message with the given certificate file and the matching private key file
		byte[] decryptedMessage = 
			encryptionService.decrypt(encryptedMessage, certFile, privateKeyFile);
		System.out.println("Decrypted message is:");
		System.out.println("======================");
		System.out.println(new String(decryptedMessage));
	}

}
