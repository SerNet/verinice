package sernet.verinice.encryption.impl.examples;

import org.apache.commons.lang.RandomStringUtils;

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
     * The generic salt
     */
    private static final String SALT = "verinice";

    /**
     * The password used for symmetric encryption
     */
    private static final String PASSWORD = "s3cr3tPassw0rd";
	
	private static IEncryptionService encryptionService = new EncryptionService();
	
    private static final int SALT_LENGTH = IEncryptionService.CRYPTO_SALT_DEFAULT_LENGTH;

	private PasswordBasedByteArrayEncryption(){}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

        testPBEWithoutSalt();
        testPBEWithSalt();
    }

    private static void testPBEWithSalt() {

        String saltString = RandomStringUtils.random(SALT_LENGTH, true, true);
        System.out.println("Password Based Encryption (with generic salt) example for byte arrays");
        System.out.println("==================================================");
        System.out.println();
        System.out.println("Secret message is:");
        System.out.println("===================");
        System.out.println(SECRET_MESSAGE);
        System.out.println("===================");
        System.out.println("Salt is:");
        System.out.println(saltString);
        System.out.println("===================");

        System.out.println("\n");

        try {


            byte[] encryptedMessage = encryptionService.encrypt(SECRET_MESSAGE.getBytes(), PASSWORD.toCharArray(), saltString.getBytes());

            System.out.println("Encrypted message(with Salt) is:");
            System.out.println("======================");
            System.out.println(new String(encryptedMessage) + "(" + String.valueOf(encryptedMessage.length) + ")");

            System.out.println("\n");

            byte[] saltBytes = new byte[SALT_LENGTH];
            System.arraycopy(encryptedMessage, 0, saltBytes, 0, SALT_LENGTH);

            System.out.println("From file read Salt is:");
            System.out.println("======================");
            System.out.println(new String(saltBytes) + "(" + String.valueOf(saltBytes.length) + ")");

            System.out.println("\n");

            byte[] cypherText = new byte[encryptedMessage.length - SALT_LENGTH];

            System.arraycopy(encryptedMessage, SALT_LENGTH, cypherText, 0, encryptedMessage.length - SALT_LENGTH);

            System.out.println("Encrypted message(withOut Salt) is:");
            System.out.println("======================");
            System.out.println(new String(cypherText) + "(" + String.valueOf(cypherText.length) + ")");

            System.out.println("\n");

            // Decrypt message
            byte[] decryptedMessage = encryptionService.decrypt(cypherText, PASSWORD.toCharArray(), saltBytes);

            System.out.println("Decrypted message is:");
            System.out.println("======================");
            System.out.println(new String(decryptedMessage));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testPBEWithoutSalt() {
        System.out.println("Password Based Encryption (with static salt) example for byte arrays");
        System.out.println("==================================================");
        System.out.println();
        System.out.println("Secret message is:");
        System.out.println("===================");
        System.out.println(SECRET_MESSAGE);

        System.out.println("\n");

        // Encrypt message
        byte[] encryptedMessage = encryptionService.encrypt(SECRET_MESSAGE.getBytes(), PASSWORD.toCharArray());

        System.out.println("Encrypted message is:");
        System.out.println("======================");
        System.out.println(new String(encryptedMessage));

        System.out.println("\n");

        // Decrypt message
        byte[] decryptedMessage = encryptionService.decrypt(encryptedMessage, PASSWORD.toCharArray());

        System.out.println("Decrypted message is:");
        System.out.println("======================");
        System.out.println(new String(decryptedMessage));
    }

}