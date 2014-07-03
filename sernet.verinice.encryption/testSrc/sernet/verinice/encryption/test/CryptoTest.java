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
package sernet.verinice.encryption.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;

import sernet.gs.service.FileUtil;
import sernet.verinice.encryption.impl.EncryptionService;
import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateIssuerName;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateSubjectName;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

/**
 *
 */
public class CryptoTest  {
    
    private static final Logger LOG = Logger.getLogger(CryptoTest.class);
    
    private static final int MAX_PASSWORD_LENGTH = 100;
    
    private EncryptionService encryptionService;
    
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
    public void certificateByteBasedCryptoTest(){
        KeyPair keyPair = generateKeyPair();
        assertNotNull("Keypair is null", keyPair);
        String distinguishedName = "CN=Test, L=Berlin, C=DE";
        int days = 365;
        String algorithm = "SHA1withRSA";
        try {
            X509Certificate cert = generateCertificate(distinguishedName, keyPair, days, algorithm);
            String certPEM = convertToPem(cert.getEncoded());
            assertNotNull(certPEM);
            File certFile = File.createTempFile("veriniceCert", "PEM");
            assertNotNull(certFile);
            FileUtil.writeStringToFile(certPEM, certFile.getAbsolutePath());
            certFile.deleteOnExit();
            byte[] encryptedData = getEncryptionService().encrypt(SECRET.getBytes(), certFile);
            byte[] privateKey = keyPair.getPrivate().getEncoded();
            String privateKeyString = convertToPem(privateKey);
            File keyFile = File.createTempFile("veriniceKey", "PEM");
            assertNotNull(keyFile);
            FileUtil.writeStringToFile(privateKeyString, keyFile.getAbsolutePath());
            certFile.deleteOnExit();
            byte[] decryptedData = getEncryptionService().decrypt(encryptedData, certFile, keyFile);
            assertEquals(SECRET, new String(decryptedData));
        } catch (GeneralSecurityException e) {
            LOG.error("Error creating certificate", e);
        } catch (IOException e) {
            LOG.error("Error creating certificate", e);
        } 
    }

    private char[] getPassword(int length){
        return RandomStringUtils.randomAscii(length).toCharArray();
    }

    public EncryptionService getEncryptionService() {
        if(encryptionService == null){
            encryptionService = new EncryptionService();
        }
        return encryptionService;
    }
    
    X509Certificate generateCertificate(String dn, KeyPair pair, int days, String algorithm)
            throws GeneralSecurityException, IOException
          {
            PrivateKey privkey = pair.getPrivate();
            X509CertInfo info = new X509CertInfo();
            Date from = new Date();
            Date to = new Date(from.getTime() + days * 86400000l);
            CertificateValidity interval = new CertificateValidity(from, to);
            BigInteger sn = new BigInteger(64, new SecureRandom());
            X500Name owner = new X500Name(dn);
           
            info.set(X509CertInfo.VALIDITY, interval);
            info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));
            info.set(X509CertInfo.SUBJECT, new CertificateSubjectName(owner));
            info.set(X509CertInfo.ISSUER, new CertificateIssuerName(owner));
            info.set(X509CertInfo.KEY, new CertificateX509Key(pair.getPublic()));
            info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
            AlgorithmId algo = new AlgorithmId(AlgorithmId.md5WithRSAEncryption_oid);
            info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));
           
            // Sign the cert to identify the algorithm that's used.
            X509CertImpl cert = new X509CertImpl(info);
            cert.sign(privkey, algorithm);
           
            // Update the algorith, and resign.
            algo = (AlgorithmId)cert.get(X509CertImpl.SIG_ALG);
            info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algo);
            cert = new X509CertImpl(info);
            cert.sign(privkey, algorithm);
            return cert;
          }  
    
    KeyPair generateKeyPair(){
        KeyPairGenerator keyGen;
        try {
            keyGen = org.bouncycastle.jce.provider.asymmetric.ec.KeyPairGenerator.getInstance("RSA", BouncyCastleProvider.PROVIDER_NAME);
            keyGen.initialize(1024, SecureRandom.getInstance("SHA1PRNG", "SUN"));
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Error creating a keyPair:\t", e);
        } catch (NoSuchProviderException e) {
            LOG.error("Error creating a keyPair:\t", e);
        }
        return null;
    }
    
    private String convertToPem(byte[] data) {
        String prefix = "-----BEGIN CERTIFICATE-----";
        String suffix = "-----END CERTIFICATE-----";
        String certData;
        try {
            certData = DatatypeConverter.printBase64Binary(data);
            return prefix + certData + suffix;
        } catch (Exception e) {
            LOG.error("Error converting cert",e);
        }
        return null;
    }
}
