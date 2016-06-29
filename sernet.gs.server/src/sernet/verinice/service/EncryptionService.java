package sernet.verinice.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;

import sernet.verinice.encryption.PasswordBasedEncryption;
import sernet.verinice.encryption.SMIMEBasedEncryption;
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
    
    private final static String ENCODING_UTF8 = "UTF-8";
    
    private final static Charset CHARSET_UTF8 = Charset.forName(ENCODING_UTF8);
    
    public EncryptionService(){}
    
	@Override
	public byte[] encrypt(byte[] unencryptedByteData, char[] password) throws EncryptionException {
		return PasswordBasedEncryption.encrypt(unencryptedByteData, password);
	}

    @Override
    public byte[] encrypt(byte[] unencryptedByteData, char[] password, byte[] salt) throws EncryptionException {
        return PasswordBasedEncryption.encrypt(unencryptedByteData, password, salt);
    }

	@Override
	public byte[] decrypt(byte[] encryptedByteData, char[] password) throws EncryptionException {
		return PasswordBasedEncryption.decrypt(encryptedByteData, password);
	}

    @Override
    public byte[] decrypt(byte[] encryptedByteData, char[] password, byte[] salt) throws EncryptionException {
        return PasswordBasedEncryption.decrypt(encryptedByteData, password, salt);
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
        String base64PlainText = new String(org.apache.commons.codec.binary.Base64.encodeBase64(plainText.getBytes()));
        byte[] plainTextBytes = base64PlainText.getBytes();
        byte[] saltBytes = salt.getBytes();
        byte[] cypherTextBytes = PasswordBasedEncryption.encrypt(plainTextBytes, password, saltBytes);
        return new String(org.apache.commons.codec.binary.Base64.encodeBase64(cypherTextBytes));
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.encryption.IEncryptionService#decrypt(java.lang.String, char[], java.lang.String)
     */
    @Override
    public String decrypt(String cypherText, char[] password, String salt) throws EncryptionException {
        byte[] cypherTextBytes = org.apache.commons.codec.binary.Base64.decodeBase64(cypherText.getBytes());
        byte[] saltBytes = salt.getBytes();
        byte[] plainTextBytes = PasswordBasedEncryption.decrypt(cypherTextBytes, password, saltBytes);
        return new String(org.apache.commons.codec.binary.Base64.decodeBase64(plainTextBytes)); 
    }
	
}
