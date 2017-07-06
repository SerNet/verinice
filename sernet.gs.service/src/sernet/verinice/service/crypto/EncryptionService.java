package sernet.verinice.service.crypto;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateException;

import org.bouncycastle.util.encoders.Base64;

import sernet.gs.service.VeriniceCharset;
import sernet.verinice.interfaces.encryption.EncryptionException;
import sernet.verinice.interfaces.encryption.IEncryptionService;

/**
 * <p>
 * Service class providing methods for password based and certificate based encryption and
 * decryption of data.
 * </p>
 * <p>
 * For en- and decryption this class uses the AES algorithm. Since this algorithm is not supported
 * by the <i>Java Cryptography Extension (JCE)</i> the BouncyCastle library is used. Please make
 * sure that this library is on your classpath.<br/>
 * More information on BouncyCastle can be found on <a
 * href="http://www.bouncycastle.org">http://www.bouncycastle.org</a>.
 * 
 * @author Sebastian Engel <s.engel@tarent.de>
 * @author Sebastian Hagedorn sh[at]sernet.de - Refactoring
 * 
 */
public class EncryptionService implements IEncryptionService {
    
    public EncryptionService(){}
    
	@Override
	public byte[] encrypt(byte[] unencryptedByteData, char[] password) throws EncryptionException {
		return PasswordBasedEncryption.encrypt(unencryptedByteData, password);
	}

    @Override
    public byte[] encrypt(byte[] unencryptedByteData, char[] password, byte[] salt) throws EncryptionException {
        return PasswordBasedEncryption.encrypt(unencryptedByteData, password, salt, true);
    }

	@Override
	public byte[] decrypt(byte[] encryptedByteData, char[] password) throws EncryptionException {
		return PasswordBasedEncryption.decrypt(encryptedByteData, password);
	}

    @Override
    public byte[] decrypt(byte[] encryptedByteData, char[] password, byte[] salt) throws EncryptionException {
        return PasswordBasedEncryption.decrypt(encryptedByteData, password, salt, true);
    }

	@Override
	public OutputStream encrypt(OutputStream unencryptedDataStream, char[] password)
			throws EncryptionException, IOException {
		return PasswordBasedEncryption.encrypt(unencryptedDataStream, password);
	}

	@Override
	public InputStream decrypt(InputStream encryptedInputStream, char[] password)
			throws EncryptionException, IOException {
		return PasswordBasedEncryption.decrypt(encryptedInputStream, password);
	}

	@Override
	public byte[] encrypt(byte[] unencryptedByteData, File x509CertificateFile)
			throws CertificateException, EncryptionException, IOException {
		return SMIMEBasedEncryption.encrypt(unencryptedByteData, x509CertificateFile);
	}

	@Override
	public byte[] decrypt(byte[] encryptedByteData, File x509CertificateFile, File privateKeyPemFile)
			throws IOException, CertificateException, EncryptionException {
		return SMIMEBasedEncryption.decrypt(encryptedByteData, x509CertificateFile,
				privateKeyPemFile);
	}
	
	
	/* (non-Javadoc)
     * @see sernet.verinice.interfaces.encryption.IEncryptionService#decrypt(java.io.InputStream, java.io.File, java.io.File, java.lang.String)
     */
    @Override
    public InputStream decrypt(InputStream encryptedDataStream, File x509CertificateFile, File privateKeyPemFile, String privateKeyPassword) 
        throws IOException, CertificateException, EncryptionException {
        return SMIMEBasedEncryption.decrypt(encryptedDataStream, x509CertificateFile,
                privateKeyPemFile,privateKeyPassword);
    }
	
	@Override
    public byte[] decrypt(byte[] encryptedByteData, File x509CertificateFile, File privateKeyPemFile, final String privateKeyPassword)
            throws IOException, CertificateException, EncryptionException {
        return SMIMEBasedEncryption.decrypt(encryptedByteData, x509CertificateFile,
                privateKeyPemFile, privateKeyPassword);
    }
	
	@Override
	public OutputStream encrypt(OutputStream unencryptedDataStream, File x509CertificateFile)
		throws IOException, CertificateException, EncryptionException {
		return SMIMEBasedEncryption.encrypt(unencryptedDataStream, x509CertificateFile);
	}
	
	@Override
	public InputStream decrypt(InputStream encryptedDataStream, File x509CertificateFile, 
			File privateKeyFile) throws IOException, CertificateException, EncryptionException {
		return SMIMEBasedEncryption.decrypt(encryptedDataStream, x509CertificateFile, 
				privateKeyFile);
	}

	@Override
	public OutputStream encrypt(OutputStream unencryptedDataStream,
			String keyAlias) throws EncryptionException, IOException, CertificateException {
		return SMIMEBasedEncryption.encrypt(unencryptedDataStream, keyAlias);
	}

	@Override
	public byte[] encrypt(byte[] unencryptedByteData, String keyAlias)
			throws CertificateException,
			EncryptionException, IOException {
		return SMIMEBasedEncryption.encrypt(unencryptedByteData, keyAlias);
	}

	@Override
    public byte[] decrypt(byte[] encryptedByteData, String keyAlias)
            throws IOException, CertificateException, EncryptionException {
        return SMIMEBasedEncryption.decrypt(encryptedByteData, keyAlias);
    }

	@Override
	public InputStream decrypt(InputStream encryptedDataStream, String keyAlias)
		throws IOException, CertificateException, EncryptionException {
		return SMIMEBasedEncryption.decrypt(encryptedDataStream, keyAlias);
	}

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.encryption.IEncryptionService#encrypt(java.lang.String, char[], java.lang.String)
     */
    @Override
    public String encrypt(String plainText, char[] password, String salt) throws EncryptionException {
        byte[] plainTextBytes = plainText.getBytes(VeriniceCharset.CHARSET_UTF_8);
        byte[] saltBytes = salt.getBytes();
        byte[] cypherTextBytes = PasswordBasedEncryption.encrypt(plainTextBytes, password, saltBytes, false);
        return new String(org.apache.commons.codec.binary.Base64.encodeBase64(cypherTextBytes));
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.encryption.IEncryptionService#decrypt(java.lang.String, char[], java.lang.String)
     */
    @Override
    public String decrypt(String cypherText, char[] password, String salt) throws EncryptionException {
        byte[] cypherTextBytes = new byte[0];
        try{
            cypherTextBytes = Base64.decode(cypherText.getBytes(IEncryptionService.CRYPTO_DEFAULT_ENCODING));
        } catch (UnsupportedEncodingException e){
            throw new EncryptionException("Unsupported encoding", e);
        }
        byte[] saltBytes = salt.getBytes();
        byte[] plainTextBytes = PasswordBasedEncryption.decrypt(cypherTextBytes, password, saltBytes, false);
        return new String(plainTextBytes, VeriniceCharset.CHARSET_UTF_8);
    }
    
    @Override
    public String decryptLicenseRestrictedProperty(String password, String value) throws EncryptionException {

        return PasswordBasedEncryption.decryptLicenserestrictedProperty(password, value);


    }

    @Override
    public byte[] encodeBase64(byte[] value) {
        return Base64.encode(value);
    }

    @Override
    public byte[] decodeBase64(byte[] value) {
        return Base64.decode(value);
    }
	
}
