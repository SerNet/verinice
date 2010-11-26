package sernet.verinice.encryption.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.RecipientId;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.RecipientInformationStore;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMEEnveloped;
import org.bouncycastle.mail.smime.SMIMEEnvelopedGenerator;
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.mail.smime.SMIMEUtil;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;
import org.bouncycastle.util.encoders.Base64;

import sernet.verinice.encryption.impl.util.CertificateUtils;
import sernet.verinice.encryption.impl.util.SMIMEDecryptedInputStream;
import sernet.verinice.encryption.impl.util.SMIMEEncryptedOutputStream;
import sernet.verinice.interfaces.encryption.EncryptionException;

/**
 * Abstract utility class providing static methods for S/MIME based encryption.
 * 
 * <p>
 * S/MIME stands for Secure/Multipurpose Internet Mail Extensions. As defined in
 * <a href="http://tools.ietf.org/html/rfc3851">RFC 2898</a>, <quote>"S/MIME
 * provides a consistent way to send and receive secure MIME data. Based on the
 * popular Internet MIME standard, S/MIME provides the following cryptographic
 * security services for electronic messaging applications: authentication,
 * message integrity and non-repudiation of origin (using digital signatures),
 * and data confidentiality (using encryption)"</quote>.
 * </p>
 * 
 * <p>
 * To encrypt a message (or other data) the public key certificate of the
 * receiver is required. Public key certificates prove that that the public key
 * it contains belongs to a certain identity (person, organisation, etc.). A
 * standard for public key certificates is X.509. This standard specifies that
 * the certificate content is definied in ASN.1. <br/>
 * The two main certificate formats are DER and PEM. The DEM format is a DER
 * (Distingushed Encoding Rules) encoded form of the ASN.1 certificate
 * definition. PEM is a Base64 encoded form of the DEM format with additional
 * ASCII header and footer, which include the encoded content.
 * </p>
 * 
 * <p>
 * -----BEGIN CERTIFICATE----- <br/>
 * Base64 encoded DER certificate content <br/>
 * Base64 encoded DER certificate content <br/>
 * Base64 encoded DER certificate content <br/>
 * -----END CERTIFICATE-----
 * </p>
 * 
 * @author Sebastian Engel <s.engel@tarent.de>
 * 
 */
public class SMIMEBasedEncryption {

    /**
     * Encrypts the given byte data with the given X.509 certificate file.
     * 
     * Since encryption is realized through S/MIME, the public certificate of
     * the "receiver" is required. The certificate is expected to be in DER or
     * PEM format.
     * 
     * 
     * @param unencryptedByteData
     *            an array of byte data to encrypt
     * @param x509CertificateFile
     *            X.509 certificate file used to encrypt the data. The file is
     *            expected to be in DER or PEM format
     * @return an array of bytes representing a MimeBodyPart that contains the
     *         encrypted content
     * @throws IOException
     *             <ul>
     *             <li>if any of the given files does not exist</li>
     *             <li>if any of the given files cannot be read</li>
     *             </ul>
     * @throws CertificateNotYetValidException
     *             if the certificate is not yet valid
     * @throws CertificateExpiredException
     *             if the certificate is not valid anymore
     * @throws CertificateException
     *             <ul>
     *             <li>if the given certificate file does not contain a
     *             certificate</li>
     *             <li>if the certificate contained in the given file is not a
     *             X.509 certificate</li>
     *             </ul>
     * @throws EncryptionException
     *             if a problem occured during the encryption process
     */
    public static byte[] encrypt(byte[] unencryptedByteData, File x509CertificateFile) throws IOException, CertificateNotYetValidException, CertificateExpiredException, CertificateException, EncryptionException {

        byte[] encryptedMimeData = new byte[] {};

        /*
         * InputStream is =
         * SMIMEBasedEncryption.class.getClassLoader().getResourceAsStream
         * ("mailcap"); MailcapCommandMap mc = new MailcapCommandMap(is);
         * CommandMap.setDefaultCommandMap(mc);
         */
        ;

        X509Certificate x509Certificate = CertificateUtils.loadX509CertificateFromFile(x509CertificateFile);

        try {
            SMIMEEnvelopedGenerator generator = new SMIMEEnvelopedGenerator();
            generator.addKeyTransRecipient(x509Certificate);
            unencryptedByteData = Base64.encode(unencryptedByteData);
            MimeBodyPart unencryptedContent = SMIMEUtil.toMimeBodyPart(unencryptedByteData);

            // Encrypt the byte data and make a MimeBodyPart from it
            MimeBodyPart encryptedMimeBodyPart = generator.generate(unencryptedContent, SMIMEEnvelopedGenerator.AES256_CBC, BouncyCastleProvider.PROVIDER_NAME);

            // Finally get the encoded bytes from the MimeMessage and return
            // them
            ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
            encryptedMimeBodyPart.writeTo(byteOutStream);
            encryptedMimeData = byteOutStream.toByteArray();

        } catch (GeneralSecurityException e) {
            throw new EncryptionException("There was a problem during the en- or decryption process. See the stacktrace for details.", e);
        } catch (SMIMEException smimee) {
            throw new EncryptionException("There was a problem during the en- or decryption process. See the stacktrace for details.", smimee);
        } catch (MessagingException e) {
            throw new EncryptionException("There was a problem during the en- or decryption process. See the stacktrace for details.", e);
        } catch (IOException ioe) {
            throw new EncryptionException("There was an IO problem during the en- or decryption process. See the stacktrace for details.", ioe);
        }
        return encryptedMimeData;
    }

    /**
     * Decrypts the given byte data with the given receiver certificate and the
     * private key
     * 
     * @param encryptedByteData
     *            an array of byte data to decrypt
     * @param x509CertificateFile
     *            X.509 certificate that was used to encrypt the data. The file
     *            is expected to be in DER or PEM format
     * @param privateKeyPemFile
     *            .pem file that contains the private key used for decryption.
     *            This key must fit to the public key contained in the public
     *            certificate
     * @return an array of bytes representing the unencrypted byte data.
     * @throws IOException
     *             <ul>
     *             <li>if any of the given files does not exist</li>
     *             <li>if any of the given files cannot be read</li>
     *             </ul>
     * @throws CertificateNotYetValidException
     *             if the certificate is not yet valid
     * @throws CertificateExpiredException
     *             if the certificate is not valid anymore
     * @throws CertificateException
     *             <ul>
     *             <li>if the given certificate file does not contain a
     *             certificate</li>
     *             <li>if the certificate contained in the given file is not a
     *             X.509 certificate</li>
     *             </ul>
     * @throws EncryptionException
     *             if a problem occured during the encryption process
     */
    public static byte[] decrypt(byte[] encryptedByteData, File x509CertificateFile, File privateKeyPemFile) throws IOException, CertificateNotYetValidException, CertificateExpiredException, CertificateException, EncryptionException {
        return decrypt(encryptedByteData, x509CertificateFile, privateKeyPemFile, null);
    }

    /**
     * Decrypts the given byte data with the given receiver certificate and the
     * private key
     * 
     * @param encryptedByteData
     *            an array of byte data to decrypt
     * @param x509CertificateFile
     *            X.509 certificate that was used to encrypt the data. The file
     *            is expected to be in DER or PEM format
     * @param privateKeyPemFile
     *            .pem file that contains the private key used for decryption.
     *            This key must fit to the public key contained in the public
     *            certificate
     * @param privateKeyPassword
     *            password to encrypt private key
     * @return an array of bytes representing the unencrypted byte data.
     * @throws IOException
     *             <ul>
     *             <li>if any of the given files does not exist</li>
     *             <li>if any of the given files cannot be read</li>
     *             </ul>
     * @throws CertificateNotYetValidException
     *             if the certificate is not yet valid
     * @throws CertificateExpiredException
     *             if the certificate is not valid anymore
     * @throws CertificateException
     *             <ul>
     *             <li>if the given certificate file does not contain a
     *             certificate</li>
     *             <li>if the certificate contained in the given file is not a
     *             X.509 certificate</li>
     *             </ul>
     * @throws EncryptionException
     *             if a problem occured during the encryption process
     */
    public static byte[] decrypt(byte[] encryptedByteData, File x509CertificateFile, File privateKeyPemFile, final String privateKeyPassword) throws IOException, CertificateNotYetValidException, CertificateExpiredException, CertificateException, EncryptionException {

        byte[] decryptedByteData = new byte[] {};

        // Get public key certificate
        X509Certificate x509Certificate = CertificateUtils.loadX509CertificateFromFile(x509CertificateFile);

        // The recipient of the S/MIME encrypted message
        RecipientId recipient = new RecipientId();
        recipient.setSerialNumber(x509Certificate.getSerialNumber());
        recipient.setIssuer(x509Certificate.getIssuerX500Principal());

        // The recipient's private key
        FileReader fileReader = new FileReader(privateKeyPemFile);
        PasswordFinder passwordFinder = new PasswordFinder() {
            @Override
            public char[] getPassword() {
                return (privateKeyPassword != null) ? privateKeyPassword.toCharArray() : null;
            }
        };
        PEMReader pemReader = null;
        if (passwordFinder.getPassword() != null && passwordFinder.getPassword().length > 0) {
            pemReader = new PEMReader(fileReader, passwordFinder);
        } else {
            pemReader = new PEMReader(fileReader);
        }
        KeyPair keyPair = (KeyPair) pemReader.readObject();
        PrivateKey privateKey = keyPair.getPrivate();

        try {
            MimeBodyPart encryptedMimeBodyPart = new MimeBodyPart(new ByteArrayInputStream(encryptedByteData));
            SMIMEEnveloped enveloped = new SMIMEEnveloped(encryptedMimeBodyPart);

            // look for our recipient identifier
            RecipientId recipientId = new RecipientId();

            recipientId.setSerialNumber(x509Certificate.getSerialNumber());
            recipientId.setIssuer(x509Certificate.getIssuerX500Principal());

            RecipientInformationStore recipients = enveloped.getRecipientInfos();
            RecipientInformation recipientInfo = recipients.get(recipientId);

            if (recipientInfo != null) {
                decryptedByteData = recipientInfo.getContent(privateKey, BouncyCastleProvider.PROVIDER_NAME);
            }
        } catch (MessagingException e) {
            throw new EncryptionException("There was an IO problem during the en- or decryption process. See the stacktrace for details.", e);
        } catch (CMSException e) {
            throw new EncryptionException("There was an IO problem during the en- or decryption process. See the stacktrace for details.", e);
        } catch (NoSuchProviderException e) {
            throw new EncryptionException("There was an IO problem during the en- or decryption process. See the stacktrace for details.", e);
        }

        decryptedByteData = Base64.decode(decryptedByteData);
        return decryptedByteData;
    }

    /**
     * Encrypts the given OutputStream using the given X.509 certificate file.
     * 
     * @param unencryptedDataStream
     * @param x509CertificateFile
     *            X.509 certificate file used to encrypt the data. The file is
     *            expected to be in DER or PEM format
     * @return the encrypted OutputStream
     * @throws IOException
     *             <ul>
     *             <li>if any of the given files does not exist</li>
     *             <li>if any of the given files cannot be read</li>
     *             </ul>
     * @throws CertificateNotYetValidException
     *             if the certificate is not yet valid
     * @throws CertificateExpiredException
     *             if the certificate is not valid anymore
     * @throws CertificateException
     *             <ul>
     *             <li>if the given certificate file does not contain a
     *             certificate</li>
     *             <li>if the certificate contained in the given file is not a
     *             X.509 certificate</li>
     *             </ul>
     * @throws EncryptionException
     *             if a problem occured during the encryption process
     */
    public static OutputStream encrypt(OutputStream unencryptedDataStream, File x509CertificateFile) throws IOException, CertificateNotYetValidException, CertificateExpiredException, CertificateException, EncryptionException {

        return new SMIMEEncryptedOutputStream(unencryptedDataStream, x509CertificateFile);
    }

    /**
     * Decrypts the given InputStream using the given X.509 certificate file
     * that was used for encryption and the matching private key file.
     * 
     * @param encryptedDataStream
     *            the InputStream to decrypt
     * @param x509CertificateFile
     *            the X.509 public certificate that was used for encryption
     * @param privateKeyFile
     *            the matching private key file needed for decryption
     * @return the decrypted InputStream
     * @throws IOException
     *             <ul>
     *             <li>if any of the given files does not exist</li>
     *             <li>if any of the given files cannot be read</li>
     *             </ul>
     * @throws CertificateNotYetValidException
     *             if the certificate is not yet valid
     * @throws CertificateExpiredException
     *             if the certificate is not valid anymore
     * @throws CertificateException
     *             <ul>
     *             <li>if the given certificate file does not contain a
     *             certificate</li>
     *             <li>if the certificate contained in the given file is not a
     *             X.509 certificate</li>
     *             </ul>
     * @throws EncryptionException
     *             if a problem occured during the encryption process
     */
    public static InputStream decrypt(InputStream encryptedDataStream, File x509CertificateFile, File privateKeyFile) throws IOException, CertificateNotYetValidException, CertificateExpiredException, CertificateException, EncryptionException {

        return new SMIMEDecryptedInputStream(encryptedDataStream, x509CertificateFile, privateKeyFile);
    }

    /**
     * Decrypts the given InputStream using the given X.509 certificate file
     * that was used for encryption and the matching private key file.
     * 
     * @param encryptedDataStream
     *            the InputStream to decrypt
     * @param x509CertificateFile
     *            the X.509 public certificate that was used for encryption
     * @param privateKeyFile
     *            the matching private key file needed for decryption
     * @param privateKeyPassword
     *            password to encrypt private key
     * @return the decrypted InputStream
     * @throws IOException
     *             <ul>
     *             <li>if any of the given files does not exist</li>
     *             <li>if any of the given files cannot be read</li>
     *             </ul>
     * @throws CertificateNotYetValidException
     *             if the certificate is not yet valid
     * @throws CertificateExpiredException
     *             if the certificate is not valid anymore
     * @throws CertificateException
     *             <ul>
     *             <li>if the given certificate file does not contain a
     *             certificate</li>
     *             <li>if the certificate contained in the given file is not a
     *             X.509 certificate</li>
     *             </ul>
     * @throws EncryptionException
     *             if a problem occured during the encryption process
     */
    public static InputStream decrypt(InputStream encryptedDataStream, File x509CertificateFile, File privateKeyFile, final String privateKeyPassword) throws IOException, CertificateNotYetValidException, CertificateExpiredException, CertificateException, EncryptionException {

        return new SMIMEDecryptedInputStream(encryptedDataStream, x509CertificateFile, privateKeyFile, privateKeyPassword);
    }
}
