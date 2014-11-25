**************************************************************************************************
** This file contains information about this bundle, including its usage and some background    ** 
** information to encryption in general.                                                        **
**************************************************************************************************



1) Introduction
#################

This bundle contains classes that offer password based and certificate based en- and decryption of
data. The data to process may be byte arrays or streams.


2) Background information to encryption
#########################################

There are two kinds of encryption methods used in this bundle. Password Based Encryption and 
Certificate Based Encryption. As the names suggest these methods are used to en- and decrypt data 
using a password or a public certificate. Password Based Encryption is a kind of symmetric 
encryption whereas Certificate Based Encryption is a kind of asymmetric encryption. Both of these
techniques and other encryption related topics are explained below. 


2.1) Symmetric encryption
==============================
Symmetric encryption means that the same key is used for en- and decryption. In case of Passsword 
Based Encryption this key is the password. The weak point of this method is the exchange of the 
key. If an unauthorized individium gets the key, he can decrypt the data.


2.2) Asymmetric encryption
==============================
Asymmetric encryption, which is also called public key encryption, uses a key pair, which is made 
of a public key and a private key. The public key can be given to other persons who may want to 
use that key to encrypt some data for the key's owner. The private key on the other hand must be 
kept secret by the owner. It is used to decrypt the data that was encrypted with the public key. 
Asymmetric encryption is more secure as symmetric encryption because the private key can be kept 
secret. On the other hand it is slower than symmetric encryption.


2.3) S/MIME Encryption
==============================
S/MIME stands for Secure Multipurpose Internet Mail Extension. It was made to sign and encrypt 
email messages and is described in the RFC 2898.

S/MIME uses certificates. Certificates prove that a public key belongs to a certain person. 
Therefore they are also called public key certificates. The most common format for public key 
certificates is X.509. X.509 is a standard that specifies the structure of a digital certificate. 
Certificates files can exist in different encodings:  

- DER (Distingushed Encoding Rules)
    DER is an encoding format for certificates, public and private keys. The file extension of DER 
    files is ".der". DER encoded files are not encrypted.
    
- PEM (Privacy Enhanced Mail)
    PEM is a Base64 encoded DER format. As such it may also contain certificates, public and 
    private keys. The DER encoded content is enclosed between an ASCII header and footer:
    
    -> example for certificate
    
    -----BEGIN CERTIFICATE-----
    Base64 encoded DER format of the certificate content
    -----END CERTIFICATE-----


    -> for a private key
    
    -----BEGIN RSA PRIVATE KEY-----
    Base64 encoded DER format of an RSA private pey 
    -----END RSA PRIVATE KEY-----
    
    
    PEM is the standard format for OpenSSL.



3) Usage
##########

The bundle's Activator class registers the EncryptionService to the bundle context.  
To use the encryption bundle in another bundle request an instance of the EncryptionService like 
this:

ServiceReference service = bundleContext.getServiceReference(IEncryptionService.class.getName());
IEncryptionService encryptionService = (IEncryptionService) bundleContext.getService(service);

Now for encrypting and decrypting just call the appropriate method on the EncryptionService 
instance.



3.1) Password Based Encryption
================================


---------------------------------------------------------------------------------
3.1.1) Encrypting a byte array with a password:
---------------------------------------------------------------------------------
byte[] unencryptedByteData = ...;
char[] password = ...;

byte[] encryptedByteData = encryptionService.encrypt(unencryptedByteData, password);


---------------------------------------------------------------------------------
3.1.2) Decrypting a byte array with a password
---------------------------------------------------------------------------------
byte[] encryptedByteData = ...;
char[] password = ...;

byte[] decryptedByteData = encryptionService.decrypt(encryptedByteData, password);


---------------------------------------------------------------------------------
3.1.3) Encrypting an OutputStream with a password (for example FileOutputStream)
---------------------------------------------------------------------------------
byte[] unencryptedByteData = ...;
char[] password = ...;
FileOutputStream unencryptedOutStream = new FileOutputStream("secretOutput.txt");

OutputStream encryptedOutputStream = encryptionService.encrypt(unencryptedOutStream, password); 
encryptedOutputStream.write(unencryptedByteData);
encryptedOutputStream.flush();
encryptedOutputStream.close();


---------------------------------------------------------------------------------
3.1.4) Decrypting an InputStream with a password (for example FileInputStream)
---------------------------------------------------------------------------------
byte[] unencryptedByteData = ...;
char[] password = ...;
FileInputStream encryptedInStream = new FileInputStream("secretOutput.txt");

InputStream decryptedInputStream = encryptionService.decrypt(encryptedInStream, password);
decryptedInputStream.read(...);
...



3.2) Certificate Based Encryption
==================================

The public certificate is required for both, encryption and decryption whereas the private key is 
only needed for decryption. Both files are expected to be PEM encoded.


---------------------------------------------------------------------------------
3.2.1) Encrypting a byte array with a public certificate:
---------------------------------------------------------------------------------
byte[] unencryptedByteData = ...;
File certificateFile = new File("certificate.pem");

byte[] encryptedByteData = encryptionService.encrypt(unencryptedByteData, certificateFile);


---------------------------------------------------------------------------------
3.2.2) Decrypting a byte array with a public certificate:
---------------------------------------------------------------------------------
byte[] encryptedByteData = ...;
File certificateFile = new File("certificate.pem");
File privateKeyfile = new File("privateKey.pem");

byte[] decryptedByteData = 
    encryptionService.decrypt(encryptedByteData, certificateFile, privateKeyFile);


------------------------------------------------------------------------------------
3.2.3) Encrypting an OutputStream with a certificate (for example FileOutputStream)
------------------------------------------------------------------------------------
File certificateFile = new File("certificate.pem");
File privateKeyfile = new File("privateKey.pem");

FileOutputStream unencryptedOutStream = new FileOutputStream("secretOutput.txt");
OutputStream encryptedOutputStream = 
    encryptionService.encrypt(unencryptedOutStream, certificateFile, privateKeyFile);
     
encryptedOutputStream.write(...);
...


---------------------------------------------------------------------------------
3.1.4) Decrypting an InputStream with a password (for example FileInputStream)
---------------------------------------------------------------------------------
File certificateFile = new File("certificate.pem");
File privateKeyfile = new File("privateKey.pem");

FileInputStream encryptedInStream = new FileInputStream("secretOutput.txt");
InputStream decryptedInputStream = 
    encryptionService.decrypt(encryptedInStream, certificateFile, privateKeyFile);

decryptedInputStream.read(...);
...