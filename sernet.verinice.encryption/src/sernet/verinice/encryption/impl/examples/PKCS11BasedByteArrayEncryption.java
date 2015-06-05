/**
 * 
 */
package sernet.verinice.encryption.impl.examples;

import java.io.IOException;
import java.security.cert.CertificateException;

import sernet.verinice.encryption.impl.EncryptionService;
import sernet.verinice.interfaces.encryption.EncryptionException;
import sernet.verinice.interfaces.encryption.IEncryptionService;

/**
 * Example application that shows how to encrypt and decrypt an array of bytes with a X.509
 * certificate from a PKCS#11 library.
 * 
 * @author Sebastian Engel <s.engel@tarent.de>
 * @author Robert Schuster <r.schuster[at]tarent[dot]de>.
 * 
 */
class PKCS11BasedByteArrayEncryption {
    
    private PKCS11BasedByteArrayEncryption(){}

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
	 */
	public static void main(String[] args) throws CertificateException, EncryptionException, IOException {

	    final int numberOfArguments = 3;
	    
		if (args.length < numberOfArguments) {
			System.out.println("Usage: java PKCS11BasedByteArrayEncryption " +
					"<pkcs11 lib> <key alias> <password> ");
			return;
		}
		String pkcs11LibPath = args[0];
		String keyAlias = args[1];
		String password = args[2];
		
		PKCS11Helper.setupSunPKCS11Provider(pkcs11LibPath, password.toCharArray());
		

		System.out.println("PKCS#11-Certificate Based Encryption example for byte arrays");
		System.out.println("=====================================================");
		System.out.println();
		System.out.println("Secret message is: " + SECRET_MESSAGE);

		System.out.println();

		// Encrypt message with given certificate file
		byte[] encryptedMessage = encryptionService.encrypt(SECRET_MESSAGE.getBytes(), keyAlias);
		System.out.println("Encrypted message is:");
		System.out.println("======================");
		System.out.println(new String(encryptedMessage));

		System.out.println();

		// Decrypt message with the given certificate file and the matching private key file
		byte[] decryptedMessage = 
			encryptionService.decrypt(encryptedMessage, keyAlias);
		System.out.println("Decrypted message is:");
		System.out.println("======================");
		System.out.println(new String(decryptedMessage));
	}

}
