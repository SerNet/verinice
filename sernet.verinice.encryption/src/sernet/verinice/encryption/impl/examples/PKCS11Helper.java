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
package sernet.verinice.encryption.impl.examples;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.Security;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import sun.security.pkcs11.SunPKCS11;

/**
 * Helps setting up a 'SunPKCS11-verinice' Security provider for PKCS#11 libraries.
 * 
 * @author Robert Schuster <r.schuster[at]tarent[dot]de>
 *
 */
@SuppressWarnings("restriction")
public final class PKCS11Helper {
    
    private PKCS11Helper(){}

	public static void setupSunPKCS11Provider(String pkcs11LibPath,
			final char[] password) {
		// Prevents installing the provider twice.
		if (Security.getProvider("SunPKCS11-verinice") != null){
			return;
		}
		// If the user enabled anything PKCS#11 related we need to lead the
		// PKCS#11 library and add its
		// provider.
		String configFile = createPKCS11ConfigFile(pkcs11LibPath);
		if (configFile != null) {
			// The availability of this class in an OSGi environment depends on
			// a system property. If
			// get errors of this class not being available check that you have
			// -Dosgi.parentClassloader=ext
			// in your VM arguments.
			SunPKCS11 p = new SunPKCS11(configFile);
			p.setCallbackHandler(new CallbackHandler() {

				@Override
				public void handle(Callback[] callbacks) throws IOException,
						UnsupportedCallbackException {
					((PasswordCallback) callbacks[0]).setPassword(password);
				}
			});
			Security.addProvider(p);
		}
	}

	private static String createPKCS11ConfigFile(String pkcs11LibPath) {
		File f = null;
		PrintWriter writer = null;
		try {
			f = File.createTempFile("pkcs11", ".cfg");
			f.deleteOnExit();
			writer = new PrintWriter(new FileOutputStream(f));
			writer.println("name = verinice");
			writer.println("description = verinice PKCS#11 configuration");
			writer.println("library = " + pkcs11LibPath);
			writer.close();
		} catch (IOException e) {
			return null;
		} finally {
			if (writer != null){
				writer.close();
			}
		}

		return f.getAbsolutePath();
	}

}
