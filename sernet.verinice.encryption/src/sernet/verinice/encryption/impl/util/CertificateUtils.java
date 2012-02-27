package sernet.verinice.encryption.impl.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * <p>
 * Utility class for certificate handling.
 * </p>
 * <p>
 * <i>Certificates, or to be more specific, public key certificates, provide a mechanism 
 * that allows a third party, or issuer, to vouch for the fact that a particular public key 
 * is linked with a particular owner, or subject.</i> 
 * (from David Hook's book "Beginning Cryptography with Java")
 * </p>
 * <p>
 * Requires the BouncyCastle provider library on the classpath.
 * </p> 
 * 
 * @author Sebastian Engel <s.engel@tarent.de>
 * 
 */
public class CertificateUtils {

    private static final Logger LOG = Logger.getLogger(CertificateUtils.class);
    
	// Add the BouncyCastle security provider if not available yet
	static {
		if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
			Security.addProvider(new BouncyCastleProvider());
		}
	}

	/**
	 * Loads a X.509 certificate from the given file.
	 * 
	 * @param x509CertificateFile
	 *            the X.509 certificate file to load
	 * 
	 * @throws IOException
	 *             <ul>
	 *             <li>if the given file does not exist</li>
	 *             <li>if the given file is cannot be read</li>
	 *             </ul>
	 * @throws CertificateNotYetValidException
	 *             if the certificate is not yet valid
	 * @throws CertificateExpiredException
	 *             if the certificate is not valid anymore
	 * @throws CertificateException
	 *             <ul>
	 *             <li>if the given file is not a certificate file</li>
	 *             <li>if the certificate contained in the given file is not a X.509 certificate</li>
	 *             </ul> 
	 */
	public static X509Certificate loadX509CertificateFromFile(File x509CertificateFile) throws IOException,
			CertificateNotYetValidException, CertificateExpiredException, CertificateException {

		// Check availablity and readability of the given file first
		if (!x509CertificateFile.exists()) {
		    String message = "The given file \"" + x509CertificateFile + "\" does not exist.";
		    LOG.error(message);
			throw new IOException(message);
		} else if (!x509CertificateFile.canRead()) {
		    String message = "The given file \"" + x509CertificateFile + "\" cannot be read.";
		    LOG.error(message);
			throw new IOException(message);
		}
		
		// Since the file seems to be ok, try to make a X509 certificate from it.
		CertificateFactory certificateFactory = null;
		try {
			certificateFactory = CertificateFactory.getInstance("X.509",BouncyCastleProvider.PROVIDER_NAME);
		} catch (NoSuchProviderException e) {
			// Shouldn't happen, since the BouncyCastle provider was added on class loading
			// or even before
			LOG.error("Certificate provider not found.", e);
		    throw new RuntimeException("Certificate provider not found.", e);
		}

		Certificate certificate = certificateFactory.generateCertificate(new FileInputStream(x509CertificateFile));
		if (certificate == null) {
		    String message = "The given file \"" + x509CertificateFile + "\" does not contain a X.509 certificate.";
		    LOG.error(message);
            throw new CertificateException(message);
        }
		if (!certificate.getType().equalsIgnoreCase("x.509")) {
		    String message = "The certificate contained in the given file \"" + x509CertificateFile + "\" is not a X.509 certificate.";
		    LOG.error(message);
			throw new CertificateException(message);
		}
		
		X509Certificate x509Certificate = (X509Certificate) certificate;
		// Lastly checks if the certificate is (still) valid.
		// If not this throws a CertificateExpiredException or
		// CertificateNotYetValidException respectively.
		x509Certificate.checkValidity();

		return x509Certificate;
	}

}
