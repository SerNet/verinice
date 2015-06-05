package sernet.gs.ui.rcp.main.security;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.ui.internal.net.auth.NetAuthenticator;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;

/**
 * This class is a subclass of {@link NetAuthenticator} implementing an optional provision
 * of authentication data through verinice's PKI.
 * 
 * <p>The {@link NetAuthenticator} class is normally hard-coded to be used as Equinox'
 * {@link Authenticator} implementation. By removing the respective registration of that
 * class from the <code>plugin.xml</code> of the <code>org.eclipse.ui.net</code> bundle
 * and instead providing the same option for this class, it is possible to bring the
 * authentication process under application control.</p>
 * 
 * <p>The implementation checks whether a specific configuration property was set
 * and in this case uses the certificates found in the verinice keystores in order
 * to do an authentication.</p>
 * 
 * <p>Unfortunately it is not possible to know which specific certificate was used for
 * SSL client authentication. As such the implementation tries all available endpoint
 * certificates. Since the {@link Authenticator} API does not tell whether a previous
 * password authentication attempt failed or not, this implementation uses a map with
 * hostnames as keys and a list of available certificates as values. Each time an
 * authentication is attempted it takes one of the certificates in the list and removes
 * it. The result is that servers whose users have multiple client certificates on
 * their smartcards/keystores should be OK with multiple login attempts.</p>
 * 
 * <p>In case that all certificates have been tried the user is prompted with a
 * username/password dialog. The same happens when the application is configured
 * to not make use of PKI password authentication at all.</p>
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 *
 */
@SuppressWarnings("restriction")
public class VeriniceAuthenticator extends NetAuthenticator {
	
	private Map<String, LinkedList<X509Certificate>> certMap = new HashMap<String, LinkedList<X509Certificate>>(); 

	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		Preferences prefs = Activator.getDefault().getPluginPreferences();
		
		X509Certificate cert = null; 

		if (prefs.getBoolean(PreferenceConstants.CRYPTO_SERVER_AUTHENTICATION_VIA_CERTIFICATE_ENABLED))
		{
			
			try {
				cert = getNextCertificate(getRequestingHost());
			} catch (KeyStoreException e) {
				throw new IllegalStateException(e);
			} catch (NoSuchProviderException e) {
				throw new IllegalStateException(e);
			} catch (CertificateException e) {
				throw new IllegalStateException(e);
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalStateException(e);
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
		
		if (cert != null) {
			// IMPLEMENTATION NOTE: This part needs to be changed in a way to present something to the
			// verinice server based on the certificate.
			//
			// Possible idea: 
			// Use the DN as the username. Sign the phrase 'verinice', convert that signature to a char-array and use
			// it as the password. On the server side. Lookup a user's public key using using the DN and check whether
			// the signature is correct.

			String dn = cert.getSubjectX500Principal().toString();
			String userName = dn;
			char[] password = dn.toCharArray();

			// TODO: Do something clever with the certificate - this implementation is lame and only a demonstration.
			if (dn.equals("EMAILADDRESS=r.schuster@tarent.de, CN=rschus, OU=development, O=tarent, L=Strasbourg, ST=Bas-Rhin, C=fr")) {
				userName = "admin";
				password = "geheim".toCharArray();
			}
			
			return new PasswordAuthentication(userName, password);
		}

		return super.getPasswordAuthentication();
	}
	
	/**
	 * Retrieves the next possible {@link X509Certificate} for the given host.
	 * 
	 * <p>The method returns <code>null</code> if no (more) certificate is available.</p>
	 * 
	 * @param host
	 * @return
	 * @throws KeyStoreException
	 * @throws NoSuchProviderException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	private X509Certificate getNextCertificate(String host) throws KeyStoreException, NoSuchProviderException, CertificateException, NoSuchAlgorithmException, IOException {
		LinkedList<X509Certificate> certs = certMap.get(host);
		if (certs == null) {
			certs = (LinkedList<X509Certificate>)getCertificates();
			certMap.put(host, certs);
		}
		
		if (certs.isEmpty()){
			return null;
		}
		return certs.removeFirst();
	}

	/**
	 * Retrieves all available endpoint certificates from verinice's keystore.
	 * 
	 * <p>Note: The actual keystore implementation depends on what was set in the respective configuration dialog.</p>
	 * 
	 * @return
	 * @throws KeyStoreException
	 * @throws NoSuchProviderException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	private List<X509Certificate> getCertificates() throws KeyStoreException, NoSuchProviderException, CertificateException, NoSuchAlgorithmException, IOException {
		LinkedList<X509Certificate> certs = new LinkedList<X509Certificate>();

		KeyStore ks = KeyStore.getInstance("verinice-ks", VeriniceSecurityProvider.NAME);
		ks.load(null, null);
		Enumeration<String> e = ks.aliases();
		while (e.hasMoreElements()) {
			X509Certificate cert = (X509Certificate) ks.getCertificate(e.nextElement());
			
			// Only certificates that denote endpoints (as opposed to CAs) should be considered.
			// (Usually a smartcard contains just one such client certificate).
			if (cert.getBasicConstraints() == -1){
				certs.add(cert);
			}
		}
		
		return certs;
	}
}
