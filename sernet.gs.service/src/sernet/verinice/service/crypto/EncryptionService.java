package sernet.verinice.service.crypto;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateException;

import org.bouncycastle.util.encoders.Base64;

import sernet.gs.service.VeriniceCharset;
import sernet.verinice.interfaces.encryption.EncryptionException;
import sernet.verinice.interfaces.encryption.IEncryptionService;

/**
 * <p>
 * Service class providing methods for password based and certificate based
 * encryption and decryption of data.
 * </p>
 * <p>
 * For en- and decryption this class uses the AES algorithm. Since this
 * algorithm is not supported by the <i>Java Cryptography Extension (JCE)</i>
 * the BouncyCastle library is used. Please make sure that this library is on
 * your classpath.<br/>
 * More information on BouncyCastle can be found on
 * <a href="http://www.bouncycastle.org">http://www.bouncycastle.org</a>.
 * 
 * @author Sebastian Engel <s.engel@tarent.de>
 * @author Sebastian Hagedorn sh[at]sernet.de - Refactoring
 * 
 */
public class EncryptionService implements IEncryptionService {

    public byte[] encrypt(byte[] unencryptedByteData, char[] password, byte[] salt)
            throws EncryptionException {
        return PasswordBasedEncryption.encrypt(unencryptedByteData, password, salt, true);
    }

    @Override
    public byte[] decrypt(byte[] encryptedByteData, char[] password) throws EncryptionException {
        return PasswordBasedEncryption.decrypt(encryptedByteData, password);
    }

    @Override
    public byte[] decrypt(byte[] encryptedByteData, char[] password, byte[] salt)
            throws EncryptionException {
        return PasswordBasedEncryption.decrypt(encryptedByteData, password, salt, true);
    }

    @Override
    public byte[] encrypt(byte[] unencryptedByteData, File x509CertificateFile)
            throws CertificateException, EncryptionException, IOException {
        return SMIMEBasedEncryption.encrypt(unencryptedByteData, x509CertificateFile);
    }

    @Override
    public byte[] decrypt(byte[] encryptedByteData, File x509CertificateFile,
            File privateKeyPemFile, final String privateKeyPassword)
            throws IOException, CertificateException, EncryptionException {
        return SMIMEBasedEncryption.decrypt(encryptedByteData, x509CertificateFile,
                privateKeyPemFile, privateKeyPassword);
    }

    @Override
    public byte[] encrypt(byte[] unencryptedByteData, String keyAlias)
            throws CertificateException, EncryptionException, IOException {
        return SMIMEBasedEncryption.encrypt(unencryptedByteData, keyAlias);
    }

    @Override
    public String decrypt(String cypherText, char[] password, String salt)
            throws EncryptionException {
        byte[] cypherTextBytes = new byte[0];
        try {
            cypherTextBytes = decodeBase64(
                    cypherText.getBytes(IEncryptionService.CRYPTO_DEFAULT_ENCODING));
        } catch (UnsupportedEncodingException e) {
            throw new EncryptionException("Unsupported encoding", e);
        }
        byte[] saltBytes = salt.getBytes();
        byte[] plainTextBytes = PasswordBasedEncryption.decrypt(cypherTextBytes, password,
                saltBytes, false);
        return new String(plainTextBytes, VeriniceCharset.CHARSET_UTF_8);
    }

    @Override
    public String decryptLicenseRestrictedProperty(String password, String value)
            throws EncryptionException {

        return PasswordBasedEncryption.decryptLicenserestrictedProperty(password, value);
    }

    @Override
    public byte[] decodeBase64(byte[] value) {
        return Base64.decode(value);
    }

}