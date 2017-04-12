/*******************************************************************************
 * Copyright (c) 2016 Sebastian Hagedorn.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Sebastian Hagedorn sh[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.security;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateException;
import java.util.Base64;

import sernet.gs.service.VeriniceCharset;
import sernet.gs.ui.rcp.main.bsi.dialogs.XMLImportDialog;
import sernet.verinice.interfaces.encryption.EncryptionException;
import sernet.verinice.interfaces.encryption.IEncryptionService;
import sernet.verinice.service.crypto.PasswordBasedEncryption;
import sernet.verinice.service.crypto.SMIMEBasedEncryption;

/**
 * this is a implementation of {@link IEncryptionService} which should be 
 * used by {@link XMLImportDialog} because there is no need to transfer the
 * data that is going to be im-/exported to the server before de-/encrypting 
 * it.
 * 
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class ClientCryptoService implements IEncryptionService {

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.encryption.IEncryptionService#encrypt(byte[], char[])
     */
    @Override
    public byte[] encrypt(byte[] unencryptedByteData, char[] password) throws EncryptionException {
        // not used in this implementation
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.encryption.IEncryptionService#encrypt(java.lang.String, char[], java.lang.String)
     */
    @Override
    public String encrypt(String plainText, char[] password, String salt) throws EncryptionException {
        // not used in this implementation
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.encryption.IEncryptionService#decrypt(java.lang.String, char[], java.lang.String)
     */
    @Override
    public String decrypt(String cypherText, char[] password, String salt) throws EncryptionException {
        byte[] cypherTextBytes = new byte[0];
        try{
            cypherTextBytes = Base64.getDecoder().decode(cypherText.getBytes(IEncryptionService.CRYPTO_DEFAULT_ENCODING));
        } catch (UnsupportedEncodingException e){
            throw new EncryptionException("Unsupported encoding", e);
        }
        byte[] saltBytes = salt.getBytes();
        byte[] plainTextBytes = PasswordBasedEncryption.decrypt(cypherTextBytes, password, saltBytes, false);
        return new String(plainTextBytes, VeriniceCharset.CHARSET_UTF_8);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.encryption.IEncryptionService#encrypt(byte[], char[], byte[])
     */
    @Override
    public byte[] encrypt(byte[] unencryptedByteData, char[] password, byte[] salt) throws EncryptionException {
        return PasswordBasedEncryption.encrypt(unencryptedByteData, password, salt, true);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.encryption.IEncryptionService#decrypt(byte[], char[])
     */
    @Override
    public byte[] decrypt(byte[] encryptedByteData, char[] password) throws EncryptionException {
        return PasswordBasedEncryption.decrypt(encryptedByteData, password);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.encryption.IEncryptionService#decrypt(byte[], char[], byte[])
     */
    @Override
    public byte[] decrypt(byte[] encryptedByteData, char[] password, byte[] salt) throws EncryptionException {
        return PasswordBasedEncryption.decrypt(encryptedByteData, password, salt, true);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.encryption.IEncryptionService#encrypt(java.io.OutputStream, char[])
     */
    @Override
    public OutputStream encrypt(OutputStream unencryptedDataStream, char[] password) throws EncryptionException, IOException {
        return PasswordBasedEncryption.encrypt(unencryptedDataStream, password);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.encryption.IEncryptionService#encrypt(java.io.OutputStream, java.lang.String)
     */
    @Override
    public OutputStream encrypt(OutputStream unencryptedDataStream, String keyAlias) throws EncryptionException, IOException, CertificateException {
        // not used in this implementation
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.encryption.IEncryptionService#encrypt(byte[], java.lang.String)
     */
    @Override
    public byte[] encrypt(byte[] unencryptedByteData, String keyAlias) throws CertificateException, EncryptionException, IOException {
        return SMIMEBasedEncryption.encrypt(unencryptedByteData, keyAlias);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.encryption.IEncryptionService#decrypt(java.io.InputStream, char[])
     */
    @Override
    public InputStream decrypt(InputStream encryptedInputStream, char[] password) throws EncryptionException, IOException {
        // not used in this implementation
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.encryption.IEncryptionService#encrypt(byte[], java.io.File)
     */
    @Override
    public byte[] encrypt(byte[] unencryptedByteData, File x509CertificateFile) throws CertificateException, EncryptionException, IOException {
        return SMIMEBasedEncryption.encrypt(unencryptedByteData, x509CertificateFile);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.encryption.IEncryptionService#decrypt(byte[], java.io.File, java.io.File)
     */
    @Override
    public byte[] decrypt(byte[] encryptedByteData, File x509CertificateFile, File privateKeyPemFile) throws IOException, CertificateException, EncryptionException {
        // not used in this implementation
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.encryption.IEncryptionService#decrypt(byte[], java.io.File, java.io.File, java.lang.String)
     */
    @Override
    public byte[] decrypt(byte[] encryptedByteData, File x509CertificateFile, File privateKeyPemFile, String privateKeyPassword) throws IOException, CertificateException, EncryptionException {
        return SMIMEBasedEncryption.decrypt(encryptedByteData, x509CertificateFile,
                privateKeyPemFile, privateKeyPassword);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.encryption.IEncryptionService#encrypt(java.io.OutputStream, java.io.File)
     */
    @Override
    public OutputStream encrypt(OutputStream unencryptedDataStream, File x509CertificateFile) throws IOException, CertificateException, EncryptionException {
        return SMIMEBasedEncryption.encrypt(unencryptedDataStream, x509CertificateFile);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.encryption.IEncryptionService#decrypt(java.io.InputStream, java.io.File, java.io.File)
     */
    @Override
    public InputStream decrypt(InputStream encryptedDataStream, File x509CertificateFile, File privateKeyFile) throws IOException, CertificateException, EncryptionException {
        // not used in this implementation
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.encryption.IEncryptionService#decrypt(java.io.InputStream, java.io.File, java.io.File, java.lang.String)
     */
    @Override
    public InputStream decrypt(InputStream encryptedDataStream, File x509CertificateFile, File privateKeyFile, String privateKeyPassword) throws IOException, CertificateException, EncryptionException {
        // not used in this implementation
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.encryption.IEncryptionService#decrypt(byte[], java.lang.String)
     */
    @Override
    public byte[] decrypt(byte[] encryptedByteData, String keyAlias) throws IOException, CertificateException, EncryptionException {
        // not used in this implementation
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.encryption.IEncryptionService#decrypt(java.io.InputStream, java.lang.String)
     */
    @Override
    public InputStream decrypt(InputStream encryptedDataStream, String keyAlias) throws IOException, CertificateException, EncryptionException {
        // not used in this implementation
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.encryption.IEncryptionService#decryptLicenseRestrictedProperty(java.lang.String, java.lang.String)
     */
    @Override
    public String decryptLicenseRestrictedProperty(String password, String value) throws EncryptionException {
        return PasswordBasedEncryption.decryptLicenserestrictedProperty(password, value);
    }

}
