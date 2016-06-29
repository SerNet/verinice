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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Calendar;

import javax.annotation.Resource;
import javax.security.auth.x500.X500Principal;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.junit.Test;

import sernet.gs.service.FileUtil;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.encryption.IEncryptionService;
import sernet.verinice.service.commands.SyncParameterException;

/**
 *
 */
public class CryptoTest extends ContextConfiguration {
    
    private static final Logger LOG = Logger.getLogger(CryptoTest.class);
    
    private static final int MAX_PASSWORD_LENGTH = 100;
    
    @Resource (name="encryptionService")
    private IEncryptionService encryptionService;
    
    private static final String VNA_FILE = "CryptoTest.vna";
    
    private static final String SECRET = "Lorem ipsum dolor sit amet, consectetur adipiscing elit." +
            " Donec at ligula et nibh pretium vulputate vitae quis tortor. " +
            "Integer ultrices facilisis ligula a pulvinar. Etiam commodo blandit eleifend. " +
            "Suspendisse malesuada ligula ut lectus fermentum, sit amet sodales elit malesuada. " +
            "Etiam nec vestibulum erat. Sed eget varius risus, vel ornare nisl. Duis sem augue, " +
            "volutpat at nisl ac, condimentum tincidunt erat. " +
            "Integer dapibus hendrerit lacus, quis semper augue feugiat sed. ";
    
    @Test
    public void passwordStreamBasedCryptoTest(){
        try{
            File f = File.createTempFile("veriniceCryptoTest", "pcr");
            f.deleteOnExit();
            char[] password = getPassword(20); 
            FileOutputStream fileOutputStream = new FileOutputStream(f);
            OutputStream encryptedOutputStream = getEncryptionService().encrypt(fileOutputStream, 
                    password);
            encryptedOutputStream.write(SECRET.getBytes());
            encryptedOutputStream.flush();
            encryptedOutputStream.close();

            FileInputStream fileInputStream = new FileInputStream(f.getAbsolutePath());
            InputStream decryptedInputStream = getEncryptionService().decrypt(fileInputStream, password);
            StringBuilder sb = new StringBuilder();            
            byte data = -1;
            while ((data = (byte) decryptedInputStream.read()) != -1) {
                sb.append((char)data);
            }
            assertEquals(SECRET, sb.toString());



        } catch (IOException e){
            LOG.error("IO-Error", e);
        }
        
    }

    @Test
    public void passwordByteBasedCryptoTest(){

        for(int i = 1; i <= MAX_PASSWORD_LENGTH; i++ ){
            char[] password = getPassword(i);
            byte[] encryptedMessage = 
                    getEncryptionService().encrypt(SECRET.getBytes(), password);


            byte[] decryptedMessage = 
                    getEncryptionService().decrypt(encryptedMessage, password);

            assertEquals("test fails on password(" + password.length + "):\n" + String.valueOf(password), new String(decryptedMessage), SECRET);
            
        }
    }
    
    @Test
    public void certificateByteBasedCryptoTest() throws GeneralSecurityException, IOException{
        KeyPair keyPair = generateKeyPair();
        assertNotNull("Keypair is null", keyPair);
        String distinguishedName = "CN=Test, L=Berlin, C=DE";
        int days = 365;
        X509Certificate cert = generateCertificate(distinguishedName, keyPair, days);
        String certPEM = convertToPem(cert.getEncoded(), false, true);
        assertNotNull(certPEM);
        File certFile = File.createTempFile("veriniceCert", "PEM");
        assertNotNull(certFile);
        FileUtil.writeStringToFile(certPEM, certFile.getAbsolutePath());
        certFile.deleteOnExit();
        byte[] encryptedData = getEncryptionService().encrypt(SECRET.getBytes(), certFile);
        byte[] privateKey = keyPair.getPrivate().getEncoded();
        String privateKeyString = convertToPem(privateKey, true, false);
        File keyFile = File.createTempFile("veriniceKey", "PEM");
        assertNotNull(keyFile);
        FileUtil.writeStringToFile(privateKeyString, keyFile.getAbsolutePath());
        certFile.deleteOnExit();
        byte[] decryptedData = getEncryptionService().decrypt(encryptedData, certFile, keyFile);
        assertEquals(SECRET, new String(decryptedData));
    }
    
    @Test
    public void VNAPBCryptoTest() throws SyncParameterException, IOException, CommandException{
        byte[] plainContent = FileUtil.getFileData(new File(getAbsoluteFilePath(VNA_FILE)));
        char[] password = getPassword(10);
        byte[] encryptedContent = getEncryptionService().encrypt(plainContent, password);
        byte[] decryptedContent = getEncryptionService().decrypt(encryptedContent, password);
        assertTrue(Arrays.areEqual(plainContent, decryptedContent));
    }

    private char[] getPassword(int length){
        return RandomStringUtils.randomAscii(length).toCharArray();
    }

    public IEncryptionService getEncryptionService() {
        return encryptionService;
    }
    
    public void setEncryptionService(IEncryptionService service){
        this.encryptionService = service;
    }
    
    X509Certificate generateCertificate(String dn, KeyPair pair, int days)
            throws GeneralSecurityException, IOException
            {
        PublicKey publicKey = pair.getPublic();
        PrivateKey privateKey = pair.getPrivate();
        if(publicKey instanceof RSAPublicKey){
            RSAPublicKey rsaPk = (RSAPublicKey)publicKey;
            RSAPublicKeySpec rsaPkSpec = new RSAPublicKeySpec(rsaPk.getModulus(), rsaPk.getPublicExponent());
            try{
                publicKey = KeyFactory.getInstance("RSA").generatePublic(rsaPkSpec);
            } catch (InvalidKeySpecException e){
                publicKey = pair.getPublic();
            }
        }
        if(privateKey instanceof RSAPrivateKey){
            RSAPrivateKey rsaPk = (RSAPrivateKey)privateKey;
            RSAPrivateKeySpec rsaPkSpec = new RSAPrivateKeySpec(rsaPk.getModulus(), rsaPk.getPrivateExponent());
            try{
                privateKey = KeyFactory.getInstance("RSA").generatePrivate(rsaPkSpec);
            } catch ( InvalidKeySpecException e){
                privateKey = pair.getPrivate();
            }
        }

        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
        String commonName = "CN=" + dn
                + ", OU=None, O=None L=None, C=None";
        X500Principal dnName = new X500Principal(commonName);
        certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
        certGen.setIssuerDN(dnName);
        certGen.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(true));
        Calendar cal = Calendar.getInstance();
        certGen.setNotBefore(cal.getTime());
        cal.add(Calendar.YEAR, 5);
        certGen.setNotAfter(cal.getTime());
        certGen.setSubjectDN(dnName);
        certGen.setPublicKey(publicKey);
        certGen.setSignatureAlgorithm("MD5WithRSA");
        return certGen.generate(privateKey, BouncyCastleProvider.PROVIDER_NAME);
            }  
    
    KeyPair generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException{
        KeyPairGenerator keyGen;
        keyGen = org.bouncycastle.jce.provider.asymmetric.ec.KeyPairGenerator.getInstance("RSA", BouncyCastleProvider.PROVIDER_NAME);
        keyGen.initialize(1024, new SecureRandom());
        return keyGen.generateKeyPair();
    }
    
    
    private String convertToPem(byte[] data, boolean isKey, boolean isCert) {
        String prefix = "";
        String suffix = "";
        if(isCert && !isKey){
            prefix = "-----BEGIN CERTIFICATE-----\n";
            suffix = "\n-----END CERTIFICATE-----";
        } 
        if(!isCert && isKey){
            prefix = "-----BEGIN PRIVATE KEY-----\n";
            suffix = "\n-----END PRIVATE KEY-----"    ;      
        }
        String certData;
        try {
            return certData = prefix + DatatypeConverter.printBase64Binary(data) + suffix;
        } catch (Exception e) {
            LOG.error("Error converting cert",e);
        }
        return null;
    }
    
    
    private String getAbsoluteFilePath(String path) {
        return getClass().getResource(path).getPath();
    }
    
}
