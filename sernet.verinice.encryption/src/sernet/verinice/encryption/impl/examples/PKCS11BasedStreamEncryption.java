/**
 * 
 */
package sernet.verinice.encryption.impl.examples;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;

import sernet.verinice.encryption.impl.EncryptionService;
import sernet.verinice.interfaces.encryption.EncryptionException;
import sernet.verinice.interfaces.encryption.IEncryptionService;

/**
 * Example application that shows how to encrypt OutputStreams and decrypt InputStreams with a X.509
 * certificate loaded from a PEM encoded file and the matching private key loaded from another PEM
 * file.
 * 
 * <p>
 * For simplicity we use a ByteArrayOutputStream and ByteArrayInputStream as the kind of streams
 * to en- and decrypt.
 * </p>
 * 
 * @author Sebastian Engel <s.engel@tarent.de>
 * 
 */
class PKCS11BasedStreamEncryption {
    
    private PKCS11BasedStreamEncryption(){}

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
	public static void main(String[] args) throws IOException, CertificateException, EncryptionException {

	    final int nrNeededArguments = 3;
	    
		if (args.length < nrNeededArguments) {
			System.out.println("Usage: java PKCS11BasedStreamEncryption " +
					"<pkcs11 lib> <key alias> <password> ");
			return;
		}
		String pkcs11LibPath = args[0];
		String keyAlias = args[1];
		String password = args[2];
		
		PKCS11Helper.setupSunPKCS11Provider(pkcs11LibPath, password.toCharArray());


		System.out.println("Certificate Based Encryption example for streams");
		System.out.println("=====================================================");
		System.out.println();
		System.out.println("Secret message is: " + SECRET_MESSAGE);

		System.out.println();

		// Encrypt message with given certificate file
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		bOut.write(SECRET_MESSAGE.getBytes());

		encryptionService.encrypt(bOut, keyAlias);
		byte[] encryptedMessage = encryptionService.encrypt(SECRET_MESSAGE.getBytes(), keyAlias);
		System.out.println("Encrypted message is:");
		System.out.println("======================");
		System.out.println(new String(encryptedMessage));

		System.out.println();
		
		// Decrypt message with the given certificate file and the matching private key file
		ByteArrayInputStream bIn = new ByteArrayInputStream(encryptedMessage);
		InputStream decryptedInputStream = encryptionService.decrypt(bIn, keyAlias);
		
		System.out.println("Decrypted message is:");
		System.out.println("=====================");
		byte data = -1;
		while ((data = (byte) decryptedInputStream.read()) != -1) {
			System.out.print((char)data);
		}
	}

}
