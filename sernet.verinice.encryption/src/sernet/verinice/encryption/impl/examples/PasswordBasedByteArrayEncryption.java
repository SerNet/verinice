package sernet.verinice.encryption.impl.examples;

import sernet.verinice.encryption.impl.EncryptionService;
import sernet.verinice.interfaces.encryption.IEncryptionService;

/**
 * Example application that shows how to encrypt and decrypt an array of bytes with a password.
 * 
 * @author Sebastian Engel <s.engel@tarent.de>
 *
 */
class PasswordBasedByteArrayEncryption {
	
	/**
	 * The secret message that shall be encrypted.
	 */
	private static final String SECRET_MESSAGE = "Attack the mars at 4 o'clock.";
	
	/**
	 * The password used for symmetric encryption
	 */
	private static final String PASSWORD = "s3cr3tPassw0rd";
	
	private static IEncryptionService encryptionService = new EncryptionService();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		System.out.println("Password Based Encryption example for byte arrays");
		System.out.println("==================================================");
		System.out.println();
		System.out.println("Secret message is:");
		System.out.println("===================");
		System.out.println(SECRET_MESSAGE);
		
		System.out.println("\n");
		
		// Encrypt message
		byte[] encryptedMessage = 
			encryptionService.encrypt(SECRET_MESSAGE.getBytes(), PASSWORD.toCharArray());
		
		System.out.println("Encrypted message is:");
		System.out.println("======================");
		System.out.println(new String(encryptedMessage));
		
		System.out.println("\n");
		
		// Decrypt message
		byte[] decryptedMessage = 
			encryptionService.decrypt(encryptedMessage, PASSWORD.toCharArray());

		System.out.println("Decrypted message is:");
		System.out.println("======================");
		System.out.println(new String(decryptedMessage));
	}

}