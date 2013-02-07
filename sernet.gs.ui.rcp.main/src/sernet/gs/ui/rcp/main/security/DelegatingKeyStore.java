/*******************************************************************************
 * Copyright (c) 2011 Robert Schuster <r.schuster[at]tarent[dot]de>.
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
 *     Robert Schuster <r.schuster[at]tarent[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.Enumeration;

import javax.security.auth.callback.PasswordCallback;

import org.apache.log4j.Logger;

/**
 * The delegating keystore allows bringing keystore handling under application control.
 * 
 * <p>Normally what is done in regard to key- and truststores is pretty much hardcoded in the
 * JDKs providers. Unfortunately some aspects of this handling is quite user-unfriendly. This
 * includes:
 * <ul>
 * <li>passwords for key stores are required even if they are not needed (only *private* keys
 *  are password encrypted)</li>
 *  <li>if the user mistyped a password there is no way to let him/her try another time when this
 *  key is used for the SSL context initialization</li>
 *  </ul>
 *  </p>
 *  
 *  <p>The implementation fixes all of the above by hooking into the machinery.</p>
 *  
 *  <p>Implementation note: The user-friendly password retrieval method has so far only been
 *  implemented for the key retrieval process (see {@link #engineGetKey(String, char[])}), as it
 *  is not important for other processes yet.</p> 
 * 
 * @author Robert Schuster <r.schuster[at]tarent[dot]de>
 * 
 */
abstract class DelegatingKeyStore extends KeyStoreSpi {
	
    private static final Logger LOG = Logger.getLogger(DelegatingKeyStore.class);
    
	/** The {@link KeyStore} instance to which all methods delegate. */
	private final KeyStore delegate;
	
	/** An optional @{link PasswordHandler} instance which comes into use when passwords are required
	 * for accessing resources.
	 */
	private final PasswordHandler passwordHandler;
	
	/** The number of times this instance should ask the user for a correct password before giving up.
	 * */
	private final int maxAttempts;
	
	/*
	 * An alias of a certificate
	 * If set only this alias will be read from the keystore.
	 * All other aliases with be ignored.
	 */
	private String certificateAlias;
	
	/** A helper class of which instances are used by subclasses to do a proper initialization
	 * of the {@link DelegatingKeyStore}.
	 * 
	 * It is only mandatory to set the {@link #keyStore} field.
	 * 
	 */
	static class Configuration {
		protected KeyStore keyStore;
		protected PasswordHandler passwordHandler;
		protected int maxAttempts;
		private String certificateAlias;
	}
	
	/** Helper interface for retrieving the actual password.
	 * 
	 * <p>The callee is supposed to inspect the {@link PasswordSession} instance
	 * to find out about the process.</p>
	 * 
	 */
	interface PasswordHandler {
		void handle(PasswordSession session); 
	}
	
	/**
	 * A helper class for the password retrieval process.
	 * 
	 * <p>It has means to store the password the user typed and to find out whether
	 * a previously entered password was wrong. For this one should call the {@link #wasWrong()}
	 * method.</p>
	 *
	 */
	static final class PasswordSession {
		private PasswordCallback cb = new PasswordCallback("X", true);
		
		private boolean wasWrong = false;
		
		private boolean userGaveUp = false;
		
		char[] getPassword() {
			return cb.getPassword();
		}
		
		void setPassword(char[] password) {
			cb.setPassword(password);
		}
		
		void giveUp() {
			userGaveUp = true;
		}
		
		void clearPassword() {
			cb.clearPassword();
		}
		
		boolean wasWrong() {
			return wasWrong;
		}
		
		boolean userGaveUp() {
			return userGaveUp;
		}
	}
	
	/**
	 * Default constructor for this class. Subclases are forced to call it.
	 * 
	 * The constructor will call the {@link #init()} method which subclasses have to provide.
	 * 
	 */
	protected DelegatingKeyStore() {
		Configuration config = init();
		delegate = config.keyStore;
		passwordHandler = config.passwordHandler;
		maxAttempts = config.maxAttempts;
		certificateAlias = config.certificateAlias;
	}
	
	/**
	 * A helper method which subclasses have to implement in order to correctly initialize
	 * the @{link {@link DelegatingKeyStore}.
	 * 
	 * The implementor is supposed to return a correctly set up @{link {@link Configuration}}
	 * object.
	 * 
	 * @return
	 */
	protected abstract Configuration init();

	@Override
	public Enumeration<String> engineAliases() {
		try {
		    if (LOG.isDebugEnabled()) {
                LOG.debug("engineAliases()...");
            }
			return delegate.aliases();
		} catch (KeyStoreException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean engineContainsAlias(String alias) {
		try {
		    if (LOG.isDebugEnabled()) {
                LOG.debug("engineContainsAlias, alias: " + alias);
            }
			return delegate.containsAlias(alias);
		} catch (KeyStoreException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void engineDeleteEntry(String alias) throws KeyStoreException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("engineDeleteEntry, alias: " + alias);
        }
		delegate.deleteEntry(alias);
	}

	@Override
	public Certificate engineGetCertificate(String alias) {
		try {
		    if (LOG.isDebugEnabled()) {
	            LOG.debug("engineGetCertificate, alias: " + alias);
	        }
			return delegate.getCertificate(alias);
		} catch (KeyStoreException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String engineGetCertificateAlias(Certificate cert) {
		try {
		    if (LOG.isDebugEnabled()) {
                LOG.debug("engineGetCertificateAlias...");
            }
			return delegate.getCertificateAlias(cert);
		} catch (KeyStoreException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Certificate[] engineGetCertificateChain(String alias) {
		try {
		    if (LOG.isDebugEnabled()) {
                LOG.debug("engineGetCertificateChain, alias: " + alias);
            }
			return delegate.getCertificateChain(alias);
		} catch (KeyStoreException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Date engineGetCreationDate(String alias) {
		try {
		    if (LOG.isDebugEnabled()) {
                LOG.debug("engineGetCreationDate, alias: " + alias);
            }
			return delegate.getCreationDate(alias);
		} catch (KeyStoreException e) {
			throw new RuntimeException(e);
		}
	}

	private Key engineGetKeyImpl(String alias, char[] password)
			throws NoSuchAlgorithmException, UnrecoverableKeyException {
		try {
		    if (LOG.isDebugEnabled()) {
                LOG.debug("engineGetKeyImpl, alias: " + alias);
            }
			return delegate.getKey(alias, password);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * This method realizes the key retrieval with a user-friendly password retrieval process.
	 * 
	 * <p>At first the method tries to get the key with the password that is given as the argument
	 * and <em>only</em> if that fails the @{link {@link PasswordHandler} instance is being used
	 * to ask the user. Every time a password is provided another attempt is made at retrieving
	 * the key. If the password is correct the process is finished. If it is not another attempt
	 * is being made.</p>
	 * 
	 * <p>If the argument-provided password is <code>null</code> the attempt to get the
	 * key with that password is not counted.</p>
	 * 
	 * <p>When the key to be retrieved is a public key, a password is not neccessary. By unconditionally
	 * trying to retrieve the key once, a gratuitous password request can be avoided.</p> 
	 * 
	 * @param alias
	 * @param password
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws UnrecoverableKeyException
	 */
	@Override
	public Key engineGetKey(String alias, char[] password)
			throws NoSuchAlgorithmException, UnrecoverableKeyException {
	    if (LOG.isDebugEnabled()) {
            LOG.debug("engineGetKey, alias: " + alias);
        }
	    if(certificateAlias!=null && !certificateAlias.equals(alias)) {
	        return null;
	    }
	    
		// If there is no password handler there is nothing we can do in case
		// the password is wrong/missing/whatever.
		// Handling this case here, simplifies later code.
		if (passwordHandler == null){
			return engineGetKeyImpl(alias, password);
		}
		PasswordSession session = new PasswordSession();
		int attempt = 0;
		char[] password0 = (password != null) ? password.clone() : null ;
		while (attempt < maxAttempts) {
			// First attempt might happen with a default password.
			try {
				return engineGetKeyImpl(alias, password0);
			} catch (UnrecoverableKeyException e) {
				session.wasWrong = (password0 != null);
				// If a password was provided, count this as an attempt.
				// Otherwise not.
				if (password0 != null){
					attempt++;
				}
				passwordHandler.handle(session);
				if (session.userGaveUp()) {
					throw new UnrecoverableKeyException("User voluntarily gave up supplying a password.");
				}
				password0 = session.getPassword();
			} finally {
				session.clearPassword();
			}
		}
		
		throw new UnrecoverableKeyException("Unable to retrieve key after " + maxAttempts + " of getting it from the user.");
		
	}

	@Override
	public boolean engineIsCertificateEntry(String alias) {
		try {
		    if (LOG.isDebugEnabled()) {
                LOG.debug("engineIsCertificateEntry, alias: " + alias);
            }
			return delegate.isCertificateEntry(alias);
		} catch (KeyStoreException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean engineIsKeyEntry(String alias) {
		try {
		    if (LOG.isDebugEnabled()) {
                LOG.debug("engineIsKeyEntry, alias: " + alias);
            }
			return delegate.isKeyEntry(alias);
		} catch (KeyStoreException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void engineLoad(InputStream stream, char[] password)
			throws IOException, NoSuchAlgorithmException,
			CertificateException {
	    if (LOG.isDebugEnabled()) {
            LOG.debug("engineLoad...");
        }
		delegate.load(stream, password);
	}

	@Override
	public void engineSetCertificateEntry(String alias, Certificate cert)
			throws KeyStoreException {
	    if (LOG.isDebugEnabled()) {
            LOG.debug("engineSetCertificateEntry, alias: " + alias);
        }
		delegate.setCertificateEntry(alias, cert);
	}

	@Override
	public void engineSetKeyEntry(String alias, byte[] key,
			Certificate[] chain) throws KeyStoreException {
		delegate.setKeyEntry(alias, key, chain);
	}

	@Override
	public void engineSetKeyEntry(String alias, Key key, char[] password,
			Certificate[] chain) throws KeyStoreException {
	    if (LOG.isDebugEnabled()) {
            LOG.debug("engineSetKeyEntry, alias: " + alias);
        }
		delegate.setKeyEntry(alias, key, password, chain);
	}

	@Override
	public int engineSize() {
		try {
			return delegate.size();
		} catch (KeyStoreException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void engineStore(OutputStream stream, char[] password)
			throws IOException, NoSuchAlgorithmException,
			CertificateException {
		try {
			delegate.store(stream, password);
		} catch (KeyStoreException e) {
			throw new RuntimeException(e);
		}
	}
	
}