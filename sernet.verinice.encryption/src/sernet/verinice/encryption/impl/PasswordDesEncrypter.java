/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.encryption.impl;

import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.engines.BlowfishEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.modes.PaddedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 * 
 */
public class PasswordDesEncrypter {
    private BufferedBlockCipher cipher;
    private KeyParameter key;

    // Initialize the cryptographic engine.
    // The key array should be at least 8 bytes long.

    public PasswordDesEncrypter(byte[] key) {
        /*
         * cipher = new PaddedBlockCipher( new CBCBlockCipher( new DESEngine() )
         * );
         */

        cipher = new PaddedBlockCipher(new CBCBlockCipher(new BlowfishEngine()));
        this.key = new KeyParameter(key);
    }

    // Initialize the cryptographic engine.
    // The string should be at least 8 chars long.

    public PasswordDesEncrypter(String key) {
        this(key.getBytes());
    }

    // Private routine that does the gritty work.
    private byte[] callCipher(byte[] data) throws CryptoException {
        int size = cipher.getOutputSize(data.length);
        byte[] result = new byte[size];
        int olen = cipher.processBytes(data, 0, data.length, result, 0);
        olen += cipher.doFinal(result, olen);

        if (olen < size) {
            byte[] tmp = new byte[olen];
            System.arraycopy(result, 0, tmp, 0, olen);
            result = tmp;
        }
        return result;
    }

    // Encrypt arbitrary byte array, returning the
    // encrypted data in a different byte array.

    public synchronized byte[] encrypt(byte[] data) throws CryptoException {
        if (data == null || data.length == 0) {
            return new byte[0];
        }
        cipher.init(true, key);
        return callCipher(data);
    }

    // Encrypts a string.
    public byte[] encryptString(String data) throws CryptoException {
        if (data == null || data.length() == 0) {
            return new byte[0];
        }
        return encrypt(data.getBytes());
    }

    // Decrypts arbitrary data.
    public synchronized byte[] decrypt(byte[] data) throws CryptoException {
        if (data == null || data.length == 0) {
            return new byte[0];
        }
        cipher.init(false, key);
        return callCipher(data);
    }

    // Decrypts a string that was previously encoded
    // using encryptString.

    public String decryptString(byte[] data) throws CryptoException {
        if (data == null || data.length == 0) {
            return "";
        }
        return new String(decrypt(data));
    }
}
