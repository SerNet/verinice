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

/**
 * Interface declaring the contract of the EncryptionService.
 * 
 * @author Sebastian Engel <s.engel@tarent.de>
 * 
 */
public interface IEncryptionService {

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
