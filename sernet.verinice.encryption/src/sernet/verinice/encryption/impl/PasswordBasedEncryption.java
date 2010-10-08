package sernet.verinice.encryption.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import sernet.verinice.interfaces.encryption.EncryptionException;
import sernet.verinice.interfaces.encryption.PasswordException;

/**
 * Abstract utility class providing static methods for Password Based Encryption
 * (PBE).
 * 
 * <p>
 * Information on Password Based Encryption can be found in <a
 * href="http://tools.ietf.org/html/rfc2898">RFC2898</a>.
 * </p>
 * 
 * @author Sebastian Engel <s.engel@tarent.de>
 * 
 */
public abstract class PasswordBasedEncryption {

    /**
     * The salt used for Password Based En- and decryption. This should be at
     * least 64 bit long ...
     */
    private static final byte[] SALT = { (byte) 0xa3, (byte) 0x51, (byte) 0x56, (byte) 0x7b, (byte) 0x9d, (byte) 0xf5, (byte) 0xf3, (byte) 0xff };

    /**
     * The iteration count used for Password Based Encryption. A length of at
     * least 1000 is recommended.
     */
    private static final int ITERATION_COUNT = 1200;

    /**
     * The algorithm used for en- and decryption. ASE is only available through
     * the BouncyCastle library.
     */
    private static final String ENCRYPTION_ALGORITHM = "PBEWITHSHA256AND256BITAES-CBC-BC";

    /**
     * Encrypts the given byte data with the given password using the AES
     * algorithm.
     * 
     * @param unencryptedByteData
     *            the byte data to encrypt
     * @param password
     *            the password used for encryption
     * @return the encrypted data as array of bytes
     * @throws EncryptionException
     *             when a problem occured during the encryption process
     */
    public static byte[] encrypt(byte[] unencryptedByteData, char[] password) throws EncryptionException {

        byte[] decryptedData = new byte[] {};

        PBEKeySpec pbeKeySpec = new PBEKeySpec(password);
        PBEParameterSpec pbeParameterSpec = new PBEParameterSpec(SALT, ITERATION_COUNT);

        try {
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(ENCRYPTION_ALGORITHM, "BC");
            SecretKey pbeKey = secretKeyFactory.generateSecret(pbeKeySpec);

            // Generate and initialize a PBE cipher
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM, "BC");
            cipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParameterSpec);

            // encrypt
            decryptedData = cipher.doFinal(unencryptedByteData);

        } catch (GeneralSecurityException e) {
            throw new EncryptionException("There was a problem during the encryption process. See the stacktrace for details.", e);
        }
        return decryptedData;
    }

    /**
     * Decrypts the given byte data with the given password using the AES
     * algorithm.
     * 
     * @param encryptedByteData
     *            the byte data to decrypt
     * @param password
     *            the password used for decryption
     * @return the decrypted data as array of bytes
     * @throws EncryptionException
     *             when a problem occured during the decryption process
     */
    public static byte[] decrypt(byte[] encryptedByteData, char[] password) throws EncryptionException {

        byte[] decryptedData = new byte[] {};

        PBEKeySpec pbeKeySpec = new PBEKeySpec(password);
        PBEParameterSpec pbeParameterSpec = new PBEParameterSpec(SALT, ITERATION_COUNT);

        try {
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(ENCRYPTION_ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);
            SecretKey pbeKey = secretKeyFactory.generateSecret(pbeKeySpec);

            // Generate and initialize a PBE cipher
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);
            cipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParameterSpec);

            // decrypt
            decryptedData = cipher.doFinal(encryptedByteData);

        } catch (InvalidKeyException e) {
            throw new PasswordException("Check your password.", e);
        } catch (BadPaddingException e) {
            throw new PasswordException("Check your password.", e);
        } catch (GeneralSecurityException e) {
            throw new EncryptionException("There was a problem during the decryption process. See the stacktrace for details.", e);
        }
        return decryptedData;
    }

    /**
     * Encrypts data received from the given OutputStream using the AES
     * algorithm.
     * 
     * @param unencryptedOutputStream
     *            the OutputStream providing the unencrypted data to encrypt
     * @param password
     *            the password used for encryption
     * @return an OutputStream providing the encrypted data
     * @throws EncryptionException
     *             when a problem occured during the en- or decryption process
     * @throws IOException
     *             when there was a problem reading from the InputStream
     */
    public static OutputStream encrypt(OutputStream unencryptedOutputStream, char[] password) throws EncryptionException, IOException {

        OutputStream encryptedOutputStream = null;

        PBEKeySpec pbeKeySpec = new PBEKeySpec(password);
        PBEParameterSpec pbeParameterSpec = new PBEParameterSpec(SALT, ITERATION_COUNT);

        try {
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(ENCRYPTION_ALGORITHM, "BC");
            SecretKey pbeKey = secretKeyFactory.generateSecret(pbeKeySpec);

            // Generate and initialize a PBE cipher
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM, "BC");
            cipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParameterSpec);

            encryptedOutputStream = new CipherOutputStream(unencryptedOutputStream, cipher);

        } catch (GeneralSecurityException e) {
            throw new EncryptionException("There was a problem during the encryption process. See the stacktrace for details.", e);
        }
        return encryptedOutputStream;
    }

    /**
     * Decrypts data received from the given InputStream using the AES
     * algorithm.
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
    public static InputStream decrypt(InputStream encryptedInputStream, char[] password) throws EncryptionException, IOException {

        InputStream decryptedInputStream = null;

        PBEKeySpec pbeKeySpec = new PBEKeySpec(password);
        PBEParameterSpec pbeParameterSpec = new PBEParameterSpec(SALT, ITERATION_COUNT);

        try {
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(ENCRYPTION_ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);
            SecretKey pbeKey = secretKeyFactory.generateSecret(pbeKeySpec);

            // Generate and initialize a PBE cipher
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);
            cipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParameterSpec);

            decryptedInputStream = new CipherInputStream(encryptedInputStream, cipher);

        } catch (GeneralSecurityException e) {
            throw new EncryptionException("There was a problem during the decryption process. See the stacktrace for details.", e);
        }
        return decryptedInputStream;
    }

}
