package sernet.verinice.encryption.examples;

import sernet.verinice.encryption.IEncryptionService;
import sernet.verinice.encryption.impl.EncryptionService;

/**
 * Example application that shows how to encrypt and decrypt an array of bytes with a password.
 * 
 * @author Sebastian Engel <s.engel@tarent.de>
 *
 */
public class PasswordBasedByteArrayEncryption {
	
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
	 * Encrypts the given message with the given password.
	 * 
	 * @param unencryptedMessage the message to encrypt
	 * @param password the password used for encryption
	 * @return the encrypted message as a String
	 */
	private static byte[] encryptMessage(String unencryptedMessage, String password) {
		return encryptionService.encrypt(unencryptedMessage.getBytes(), password.toCharArray());
	}
	
	/**
	 * Decrypts the given message with the given password.
	 * 
	 * @param encryptedMessage the message to decrypt
	 * @param password the password used for decryption
	 * @return the encrypted message as a String
	 */
	private static byte[] decryptMessage(byte[] encryptedMessage, String password) {
		return encryptionService.decrypt(encryptedMessage, password.toCharArray());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		System.out.println("Password Based Encryption example");
		System.out.println("=================================");
		System.out.println();
		System.out.println("Secret message is: " + SECRET_MESSAGE);
		
		System.out.println();
		
		// Encrypt message
		byte[] encryptedMessage = encryptMessage(SECRET_MESSAGE, PASSWORD);
		System.out.println("Encrypted message is: " + new String(encryptedMessage));
		
		// Decrypt message
		byte[] decryptedMessage = decryptMessage(encryptedMessage, PASSWORD);
		System.out.println("Decrypted message is: " + new String(decryptedMessage));
	}

}
