/*******************************************************************************
 * Copyright (c) 2010 Robert Schuster <r.schuster@tarent.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.interfaces.encryption;

import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;

/**
 * Interface declaring the contract of the EncryptionService.
 * 
 * @author Sebastian Engel <s.engel@tarent.de>
 * 
 */
public interface IEncryptionService {

    public enum EncryptionMethod {
        PASSWORD, EXTERNAL_CERTIFICATE, PKCS11
    }

    public static final int CRYPTO_SALT_DEFAULT_LENGTH = 8;
    public static final String CRYPTO_DEFAULT_ENCODING = "UTF-8";

    String decrypt(String cypherText, char[] password, String salt) throws EncryptionException;

    byte[] encrypt(byte[] unencryptedByteData, char[] password, byte[] salt)
            throws EncryptionException;

    /**
     * Decrypts the given byte data with the given password using the AES
     * algorithm.
     * 
     * @param encryptedByteData
     *            the data to decrypt
     * @param password
     *            the password used for decryption
     * @return the decrypted data as array of bytes
     * @throws EncryptionException
     *             if a problem occured during the decryption process
     */
    byte[] decrypt(byte[] encryptedByteData, char[] password) throws EncryptionException;

    byte[] decrypt(byte[] encryptedByteData, char[] password, byte[] salt)
            throws EncryptionException;

    byte[] encrypt(byte[] unencryptedByteData, String keyAlias)
            throws CertificateException, EncryptionException, IOException;

    // ##### S/MIME Encryption #####

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
    byte[] encrypt(byte[] unencryptedByteData, File x509CertificateFile)
            throws CertificateException, EncryptionException, IOException;

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
    byte[] decrypt(byte[] encryptedByteData, File x509CertificateFile, File privateKeyPemFile,
            final String privateKeyPassword)
            throws IOException, CertificateException, EncryptionException;

    /**
     * decrypts a property that is stored in the db in a non-readable format,
     * due to license restrictions
     * 
     * @param password
     * @param value
     * @return
     * @throws EncryptionException
     */
    String decryptLicenseRestrictedProperty(String password, String value)
            throws EncryptionException;

    /**
     * decodes a Base64-Encoded byte[]
     * 
     * @param value
     * @return
     */
    byte[] decodeBase64(byte[] value);
}
