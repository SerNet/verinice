package sernet.verinice.encryption.impl.examples;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import sernet.verinice.encryption.impl.EncryptionService;
import sernet.verinice.interfaces.encryption.IEncryptionService;

/**
 * Example application that shows how to encrypt OutputStreams and decrypt InputStreams 
 * with a password.
 * 
 * @author Sebastian Engel <s.engel@tarent.de>
 *
 */
class PasswordBasedStreamEncryption {

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
	 * 
	 * @param args args[0] - path to file to encrypt
	 */
	public static void main(String[] args) {
		
		if (args.length < 1) {
			System.out.println("Usage: java PasswordBasedStreamEncryption <file-to-encrypt>");
			return;
		}

		// File to save the encrypted message to and load it from.
		File targetFile = new File(args[0]);
		
		System.out.println("Password Based Encryption example for streams");
		System.out.println("==============================================");
		System.out.println();
		System.out.println("Secret message is: " + SECRET_MESSAGE);
		
		System.out.println();
		
		try {
			// === Encrypt message ===
			
			// Situation:	We want to save the encrypted message to a file
			// Solution:	Create FileOutputStream and encrypt it
			FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
			OutputStream encryptedOutputStream = encryptionService.encrypt(fileOutputStream, 
					PASSWORD.toCharArray());
			encryptedOutputStream.write(SECRET_MESSAGE.getBytes());
			encryptedOutputStream.flush();
			encryptedOutputStream.close();

			System.out.println("Saved encrypted message into file: " + args[0]);

			// === Decrypt message ===
			
			// Situation:	We want to load an encrypted message from a file
			// Solution: 	Create a FileInputStream and decrypt it
			FileInputStream fileInputStream = new FileInputStream(targetFile);
			InputStream decryptedInputStream = 
				encryptionService.decrypt(fileInputStream, PASSWORD.toCharArray());
			
			System.out.print("Decrypted message loaded from encrypted file is: " );
			byte data = -1;
			while ((data = (byte) decryptedInputStream.read()) != -1) {
				System.out.print((char)data);
			}

		} catch (FileNotFoundException e) {
			System.err.println("The specified file was not found: " + args[0]);
		} catch (IOException ioe) {
			System.err.println("The specified file could not be read: " + args[0]);
		}
		
	}
}
