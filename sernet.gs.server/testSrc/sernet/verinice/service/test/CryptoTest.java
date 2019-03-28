/*******************************************************************************
 * Copyright (c) 2013 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;

import javax.annotation.Resource;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.encoders.Base64;
import org.junit.Assert;
import org.junit.Test;

import sernet.gs.service.VeriniceCharset;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.encryption.IEncryptionService;
import sernet.verinice.service.commands.SyncParameterException;
import sernet.verinice.service.crypto.PasswordBasedEncryption;

/**
 *
 */
public class CryptoTest extends ContextConfiguration {

    private static final Logger LOG = Logger.getLogger(CryptoTest.class);

    private static final int MAX_PASSWORD_LENGTH = 100;

    private static final String BC_PROVIDER_NAME = BouncyCastleProvider.PROVIDER_NAME;
    private static final int CRYPTO_SALT_DEFAULT_LENGTH = 8;
    private static final String CRYPTO_DEFAULT_ENCODING = "UTF-8";
    private static final int CRYPTO_KEY_ITERATION_COUNTS = 1200;
    private static final String ENCRYPTION_ALGORITHM = "PBEWITHSHA256AND256BITAES-CBC-BC";

    @Resource(name = "encryptionService")
    private IEncryptionService encryptionService;

    private static final String VNA_FILE = "CryptoTest.vna";

    private static final String SECRET = "Lorem ipsum dolor sit amet, consectetur adipiscing elit."
            + " Donec at ligula et nibh pretium vulputate vitae quis tortor. "
            + "Integer ultrices facilisis ligula a pulvinar. Etiam commodo blandit eleifend. "
            + "Suspendisse malesuada ligula ut lectus fermentum, sit amet sodales elit malesuada. "
            + "Etiam nec vestibulum erat. Sed eget varius risus, vel ornare nisl. Duis sem augue, "
            + "volutpat at nisl ac, condimentum tincidunt erat. "
            + "Integer dapibus hendrerit lacus, quis semper augue feugiat sed. ";

    @Test
    public void passwordByteBasedCryptoTest() {

        for (int i = 1; i <= MAX_PASSWORD_LENGTH; i++) {
            char[] password = getPassword(i);
            byte[] encryptedMessage = PasswordBasedEncryption.encrypt(SECRET.getBytes(), password);

            byte[] decryptedMessage = getEncryptionService().decrypt(encryptedMessage, password);

            assertEquals(
                    "test fails on password(" + password.length + "):\n" + String.valueOf(password),
                    new String(decryptedMessage), SECRET);

        }
    }

    @Test
    public void VNAPBCryptoTest() throws SyncParameterException, IOException, CommandException {
        byte[] plainContent = Files.readAllBytes(Paths.get(getAbsoluteFilePath(VNA_FILE)));
        char[] password = getPassword(10);
        byte[] encryptedContent = PasswordBasedEncryption.encrypt(plainContent, password);
        byte[] decryptedContent = getEncryptionService().decrypt(encryptedContent, password);
        assertTrue(Arrays.areEqual(plainContent, decryptedContent));
    }

    private char[] getPassword(int length) {
        return RandomStringUtils.randomAscii(length).toCharArray();
    }

    public IEncryptionService getEncryptionService() {
        return encryptionService;
    }

    public void setEncryptionService(IEncryptionService service) {
        this.encryptionService = service;
    }

    private String getAbsoluteFilePath(String path) {
        return getClass().getResource(path).getPath();
    }

    @Test
    public void testBouncyCastle() {

        final String PLAINTEXT = "ISO27K1";
        final String PASSWORD = "111";

        try {
            byte[] saltBytes = getSalt();
            String saltString = new String(saltBytes, VeriniceCharset.CHARSET_UTF_8);
            String cyphertext = encryptCLIWay(PLAINTEXT, PASSWORD, saltString);
            String decryptedText = decryptCLIWay(cyphertext, PASSWORD);
            Assert.assertEquals(PLAINTEXT, decryptedText);

        } catch (Exception e) {
            LOG.error("Something went wrong", e);
        }
    }

    private byte[] getSalt()
            throws NoSuchAlgorithmException, NoSuchProviderException, UnsupportedEncodingException {
        byte[] salt = new byte[CRYPTO_SALT_DEFAULT_LENGTH];
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        byte[] randomAlphanumericBytes = new BigInteger(130, secureRandom).toString(32)
                .getBytes(CRYPTO_DEFAULT_ENCODING);
        salt = java.util.Arrays.copyOfRange(randomAlphanumericBytes, 0, CRYPTO_SALT_DEFAULT_LENGTH);
        return salt;
    }

    private String decryptCLIWay(String cypherText, String password)
            throws NoSuchAlgorithmException, NoSuchProviderException, UnsupportedEncodingException,
            InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

        if (Security.getProvider(BC_PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        SecretKeyFactory secKeyFac = SecretKeyFactory.getInstance(ENCRYPTION_ALGORITHM,
                BC_PROVIDER_NAME);

        char[] keyChar = new char[password.length()];
        password.getChars(0, password.length(), keyChar, 0);
        PBEKeySpec pbeKeySpec = new PBEKeySpec(keyChar);

        final byte[] bytes = Base64
                .decode(cypherText.getBytes(IEncryptionService.CRYPTO_DEFAULT_ENCODING));
        final byte[] salt = java.util.Arrays.copyOf(bytes, CRYPTO_SALT_DEFAULT_LENGTH);
        final byte[] cipherText = java.util.Arrays.copyOfRange(bytes, CRYPTO_SALT_DEFAULT_LENGTH,
                bytes.length);

        PBEParameterSpec bEParameterSpec = new PBEParameterSpec(salt, CRYPTO_KEY_ITERATION_COUNTS);
        SecretKey secret = secKeyFac.generateSecret(pbeKeySpec);

        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM, BC_PROVIDER_NAME);
        cipher.init(Cipher.DECRYPT_MODE, secret, bEParameterSpec);
        byte[] decrypted = cipher.doFinal(cipherText);

        return new String(decrypted, IEncryptionService.CRYPTO_DEFAULT_ENCODING);

    }

    private String encryptCLIWay(String plainText, String password, String salt)
            throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException,
            NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        if (Security.getProvider(BC_PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        SecretKeyFactory secKeyFac = SecretKeyFactory.getInstance(ENCRYPTION_ALGORITHM,
                BC_PROVIDER_NAME);

        char[] keyChar = new char[password.length()];
        password.getChars(0, password.length(), keyChar, 0);
        PBEKeySpec pbeKeySpec = new PBEKeySpec(keyChar);
        PBEParameterSpec paramSpec = new PBEParameterSpec(
                salt.getBytes(VeriniceCharset.CHARSET_UTF_8), CRYPTO_KEY_ITERATION_COUNTS);
        SecretKey secret = secKeyFac.generateSecret(pbeKeySpec);

        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM, BC_PROVIDER_NAME);
        cipher.init(Cipher.ENCRYPT_MODE, secret, paramSpec);
        byte[] encrypted = cipher.doFinal(plainText.getBytes(CRYPTO_DEFAULT_ENCODING));
        byte[] saltBytes = paramSpec.getSalt();
        byte[] saltAndEncrypted = new byte[saltBytes.length + encrypted.length];

        System.arraycopy(saltBytes, 0, saltAndEncrypted, 0, saltBytes.length);
        System.arraycopy(encrypted, 0, saltAndEncrypted, saltBytes.length, encrypted.length);

        byte[] encoded = Base64.encode(saltAndEncrypted);
        final String encodedString = new String(encoded, CRYPTO_DEFAULT_ENCODING);
        return encodedString;
    }

}
