package sernet.verinice.service.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;

import sernet.gs.service.VeriniceCharset;
import sernet.verinice.interfaces.encryption.EncryptionException;
import sernet.verinice.interfaces.encryption.IEncryptionService;
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
public final class PasswordBasedEncryption {

    /**
     * The salt used for Password Based En- and decryption. This should be at
     * least 64 bit long ... Since V 1.12. the static use of SALT for
     * initializing the AES is deprecated and a generic salt is generated for
     * each encryption. The generic salt is stored as a prefix in the byte[]
     * that represents the cyphertext and will be removed before decryption. See
     * {@link sernet.verinice.service.crypto.PasswordBasedEncryption.decrypt(byte[],
     * char[], byte[]) } and
     * {@link sernet.verinice.service.crypto.PasswordBasedEncryption.encrypt(byte[],
     * char[], byte[])}
     */
    @Deprecated
    private static final byte[] SALT = { (byte) 0xa3, (byte) 0x51, (byte) 0x56, (byte) 0x7b, (byte) 0x9d, (byte) 0xf5, (byte) 0xf3, (byte) 0xff };

    /**
     * The iteration count used for Password Based Encryption. A length of at
     * least 1000 is recommended.
     */
    private static final int ITERATION_COUNT = 1200;

    /**
     * The algorithm used for en- and decryption. AES is only available through
     * the BouncyCastle library.
     */
    private static final String ENCRYPTION_ALGORITHM = "PBEWITHSHA256AND256BITAES-CBC-BC";
    
    /**
     * The Bouncycastle Providername
     */
    private static final String CRYPTOPROVIDER = BouncyCastleProvider.PROVIDER_NAME;
    
    /**
     * Encrypts the given byte data with the given password using the AES
     * algorithm.
     * 
     * Method is deprecated, because usage of static attribute {@value SALT}
     * 
     * @param unencryptedByteData
     *            the byte data to encrypt
     * @param password
     *            the password used for encryption
     * @return the encrypted data as array of bytes
     * @throws EncryptionException
     *             when a problem occured during the encryption process
     */
    @Deprecated
    public static byte[] encrypt(byte[] unencryptedByteData, char[] password) throws EncryptionException {
        return encryptData(unencryptedByteData, password, SALT);
    }

    /**
     * Encrypts the given byte data with the given password using the AES
     * algorithm.
     * 
     * @param unencryptedByteData
     *            the byte data to encrypt
     * @param password
     *            the password used for encryption
     * @param salt
     *            a generic salt passed to the parameters of the crypto-engine
     * @param attachSaltToCypherText if true, salt gets attached as a prefix to 
     * the cyphertext (returnValue of the method
     * @return the encrypted data as array of bytes
     * @throws EncryptionException
     *             when a problem occured during the encryption process
     */
    public static byte[] encrypt(byte[] unencryptedByteData, char[] password, byte[] salt, boolean attachSaltToCypherText) throws EncryptionException {
        byte[] encryptedData = encryptData(unencryptedByteData, password, salt);
        encryptedData = (encryptedData == null) ? new byte[] {} : encryptedData;

        // attach (generic) salt to cyphertext as a prefix
        if (attachSaltToCypherText){
            byte[] encryptedDataWithSaltPrefix = new byte[encryptedData.length + IEncryptionService.CRYPTO_SALT_DEFAULT_LENGTH];
            System.arraycopy(salt, 0, encryptedDataWithSaltPrefix, 0, salt.length);
            System.arraycopy(encryptedData, 0, encryptedDataWithSaltPrefix, salt.length, encryptedData.length);

            return encryptedDataWithSaltPrefix;
        } else return encryptedData;
    }
    
    private static byte[] encryptData(byte[] unencryptedByteData, char[] password, byte[] salt) {
        byte[] encryptedData = null;
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password);
        PBEParameterSpec pbeParameterSpec = new PBEParameterSpec(salt, ITERATION_COUNT);

        try {
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(ENCRYPTION_ALGORITHM, CRYPTOPROVIDER);
            SecretKey pbeKey = secretKeyFactory.generateSecret(pbeKeySpec);

            // Generate and initialize a PBE cipher
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM, CRYPTOPROVIDER);
            cipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParameterSpec);

            // encrypt
            encryptedData = cipher.doFinal(unencryptedByteData);

            pbeKeySpec.clearPassword();
        } catch (GeneralSecurityException e) {
            throw new EncryptionException("There was a problem during the encryption process. See the stacktrace for details.", e);
        }
        return encryptedData;
    }

    /**
     * Decrypts the given byte data with the given password using the AES
     * algorithm.
     * 
     * Method is deprecated because usage of static attribute {@value SALT} but
     * do NOT REMOVE this, as it is still used to import vna generated with
     * versions < 1.12
     * 
     * @param encryptedByteData
     *            the byte data to decrypt
     * @param password
     *            the password used for decryption
     * @return the decrypted data as array of bytes
     * @throws EncryptionException
     *             when a problem occured during the decryption process
     */
    @Deprecated
    public static byte[] decrypt(byte[] encryptedByteData, char[] password) throws EncryptionException {

        byte[] decryptedData = decryptData(password, SALT, encryptedByteData);

        return (decryptedData == null) ? new byte[] {} : decryptedData;
    }

    /**
     * Decrypts the given byte data with the given password using the AES
     * algorithm.
     * 
     * @param encryptedByteData
     *            the byte data to decrypt
     * @param password
     *            the password used for decryption
     * @param salt
     *            a generic salt passed to the parameters of the crypto-engine
     * @param isGenericSaltAttached
     *          defines if the cyphertext (encryptedByteData) contains the
     *          generic generated salt in the first 8 byte
     *          (is not the case for vnl-data)
     * @return the decrypted data as array of bytes
     * @throws EncryptionException
     *             when a problem occured during the decryption process
     */
    public static byte[] decrypt(byte[] encryptedByteData, char[] password, byte[] salt, boolean isGenericSaltAttached) throws EncryptionException {

        if(isGenericSaltAttached){
            // remove salt prefix from cyphertext
            byte[] saltBytes = new byte[IEncryptionService.CRYPTO_SALT_DEFAULT_LENGTH];
            System.arraycopy(encryptedByteData, 0, saltBytes, 0, IEncryptionService.CRYPTO_SALT_DEFAULT_LENGTH);

            byte[] cypherText = new byte[encryptedByteData.length - IEncryptionService.CRYPTO_SALT_DEFAULT_LENGTH];

            System.arraycopy(encryptedByteData, IEncryptionService.CRYPTO_SALT_DEFAULT_LENGTH, cypherText, 0, encryptedByteData.length - IEncryptionService.CRYPTO_SALT_DEFAULT_LENGTH);
            return decryptData(password, salt, cypherText);
        } else return decryptData(password, salt, encryptedByteData); 
    }

    private static byte[] decryptData(char[] password, byte[] salt, byte[] cypherText) {
        byte[] decryptedData = null;
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password);
        PBEParameterSpec pbeParameterSpec = new PBEParameterSpec(salt, ITERATION_COUNT);

        try {
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(ENCRYPTION_ALGORITHM, CRYPTOPROVIDER);
            SecretKey pbeKey = secretKeyFactory.generateSecret(pbeKeySpec);

            // Generate and initialize a PBE cipher
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM, CRYPTOPROVIDER);
            cipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParameterSpec);

            // decrypt
            decryptedData = cipher.doFinal(cypherText);

        } catch (InvalidKeyException e) {
            throw new PasswordException("Check your password.", e);
        } catch (BadPaddingException e) {
            throw new PasswordException("Check your password.", e);
        } catch (GeneralSecurityException e) {
            throw new EncryptionException("There was a problem during the decryption process. See the stacktrace for details.", e);
        }
        return (decryptedData == null) ? new byte[] {} : decryptedData;
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
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(ENCRYPTION_ALGORITHM, CRYPTOPROVIDER);
            SecretKey pbeKey = secretKeyFactory.generateSecret(pbeKeySpec);

            // Generate and initialize a PBE cipher
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM, CRYPTOPROVIDER);
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
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(ENCRYPTION_ALGORITHM, CRYPTOPROVIDER);
            SecretKey pbeKey = secretKeyFactory.generateSecret(pbeKeySpec);

            // Generate and initialize a PBE cipher
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM, CRYPTOPROVIDER);
            cipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParameterSpec);

            decryptedInputStream = new CipherInputStream(encryptedInputStream, cipher);

        } catch (GeneralSecurityException e) {
            throw new EncryptionException("There was a problem during the decryption process. See the stacktrace for details.", e);
        }
        return decryptedInputStream;
    }
    
    public static String decryptLicenserestrictedProperty(String password, String cypherText)
            throws EncryptionException {

        SecretKeyFactory secKeyFac;
        try {
            secKeyFac = SecretKeyFactory.getInstance(
                    ENCRYPTION_ALGORITHM,
                    CRYPTOPROVIDER);


            char[] keyChar = new char[password.length()];
            password.getChars(0, password.length(), keyChar, 0);
            PBEKeySpec pbeKeySpec = new PBEKeySpec(keyChar);

            final byte[] bytes = Base64
                    .decode(cypherText.getBytes(IEncryptionService.CRYPTO_DEFAULT_ENCODING));
            final byte[] salt = Arrays.copyOf(bytes, IEncryptionService.CRYPTO_SALT_DEFAULT_LENGTH);
            final byte[] cipherTextBytes = Arrays.copyOfRange(bytes,
                    IEncryptionService.CRYPTO_SALT_DEFAULT_LENGTH,
                    bytes.length);

            PBEParameterSpec bEParameterSpec = new PBEParameterSpec(salt, ITERATION_COUNT);
            SecretKey secret;
            secret = secKeyFac.generateSecret(pbeKeySpec);

            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM, CRYPTOPROVIDER);
            cipher.init(Cipher.DECRYPT_MODE, secret, bEParameterSpec);
            byte[] decrypted = cipher.doFinal(cipherTextBytes);
            return new String(decrypted, VeriniceCharset.CHARSET_UTF_8.name());
        } catch (IllegalBlockSizeException e) {
            throw new EncryptionException(e);
        } catch (BadPaddingException e) {
            throw new EncryptionException(e);
        } catch (UnsupportedEncodingException e) {
            throw new EncryptionException(e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new EncryptionException(e);
        } catch (InvalidKeyException e) {
            throw new EncryptionException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new EncryptionException(e);
        } catch (NoSuchProviderException e) {
            throw new EncryptionException(e);
        } catch (NoSuchPaddingException e) {
            throw new EncryptionException(e);
        } catch (InvalidKeySpecException e) {
            throw new EncryptionException(e);
        }
    }

    private PasswordBasedEncryption() {

    }
}
