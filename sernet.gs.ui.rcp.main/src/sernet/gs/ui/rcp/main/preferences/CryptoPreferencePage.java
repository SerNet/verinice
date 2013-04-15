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
package sernet.gs.ui.rcp.main.preferences;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import sernet.gs.ui.rcp.main.Activator;

/**
 * Preference page for the cryptographic options.
 * 
 * @author Robert Schuster <r.schuster[at]tarent[dot]de>
 * 
 */
public class CryptoPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
    
    private static final Logger LOG = Logger.getLogger(CryptoPreferencePage.class);

	public CryptoPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	@Override
	public void createFieldEditors() {
		Composite fep = getFieldEditorParent();
		
		
		// If this is checked the verinice security provider will take over the certificate/key management
		// for SSL connections.
		// Requires a restart of the application when changed.
		addField(new BooleanFieldEditor(PreferenceConstants.CRYPTO_VERINICE_SSL_SECURITY_ENABLED, Messages.getString("CryptoPreferencePage.0"), fep)); //$NON-NLS-1$
		
		addField(new BooleanFieldEditor(PreferenceConstants.CRYPTO_SERVER_AUTHENTICATION_VIA_CERTIFICATE_ENABLED, Messages.getString("CryptoPreferencePage.1"), fep)); //$NON-NLS-1$
		
		
        
		if(isPKCS11Supported()){
		    RadioGroupFieldEditor trustStoreSourceRadioField = new RadioGroupFieldEditor(PreferenceConstants.CRYPTO_TRUSTSTORE_SOURCE, Messages.getString("CryptoPreferencePage.2"), //$NON-NLS-1$
		            1, new String[][] { { Messages.getString("CryptoPreferencePage.3"), PreferenceConstants.CRYPTO_TRUSTSTORE_SOURCE_FROM_FILE }, //$NON-NLS-1$
		        { Messages.getString("CryptoPreferencePage.4"), PreferenceConstants.CRYPTO_TRUSTSTORE_SOURCE_FROM_PKCS11_LIBRARY } //$NON-NLS-1$
		    }, fep);
		    addField(trustStoreSourceRadioField);
		} else {
		    RadioGroupFieldEditor trustStoreSourceRadioFieldWin = new RadioGroupFieldEditor(PreferenceConstants.CRYPTO_TRUSTSTORE_SOURCE, Messages.getString("CryptoPreferencePage.2"), //$NON-NLS-1$
		            1, new String[][] { { Messages.getString("CryptoPreferencePage.3"), PreferenceConstants.CRYPTO_TRUSTSTORE_SOURCE_FROM_FILE }, //$NON-NLS-1$
		    }, fep);		
		    addField(trustStoreSourceRadioFieldWin); // pkcs#11 is not an option here
		}
		
		if(isPKCS11Supported()){
		    RadioGroupFieldEditor keyStoreSourceRadioField = new RadioGroupFieldEditor(PreferenceConstants.CRYPTO_KEYSTORE_SOURCE, Messages.getString("CryptoPreferencePage.5"), //$NON-NLS-1$
		            1, new String[][] { { Messages.getString("CryptoPreferencePage.6"), PreferenceConstants.CRYPTO_KEYSTORE_SOURCE_NONE }, //$NON-NLS-1$
		        { Messages.getString("CryptoPreferencePage.7"), PreferenceConstants.CRYPTO_KEYSTORE_SOURCE_FROM_FILE }, //$NON-NLS-1$
		        { Messages.getString("CryptoPreferencePage.8"), PreferenceConstants.CRYPTO_KEYSTORE_SOURCE_FROM_PKCS11_LIBRARY } //$NON-NLS-1$
		    }, fep);
		    addField(keyStoreSourceRadioField);
		} else {
		    RadioGroupFieldEditor keyStoreSourceRadioFieldWin = new RadioGroupFieldEditor(PreferenceConstants.CRYPTO_KEYSTORE_SOURCE, Messages.getString("CryptoPreferencePage.5"), //$NON-NLS-1$
		            1, new String[][] { { Messages.getString("CryptoPreferencePage.6"), PreferenceConstants.CRYPTO_KEYSTORE_SOURCE_NONE }, //$NON-NLS-1$
		        { Messages.getString("CryptoPreferencePage.7"), PreferenceConstants.CRYPTO_KEYSTORE_SOURCE_FROM_FILE }, //$NON-NLS-1$
		    }, fep);
		    addField(keyStoreSourceRadioFieldWin);
		}
		
		addField(new FileFieldEditor(PreferenceConstants.CRYPTO_KEYSTORE_FILE, Messages.getString("CryptoPreferencePage.9"), fep)); //$NON-NLS-1$
		addField(new FileFieldEditor(PreferenceConstants.CRYPTO_TRUSTSTORE_FILE, Messages.getString("CryptoPreferencePage.10"), fep)); //$NON-NLS-1$
		if(isPKCS11Supported()){
		    addField(new FileFieldEditor(PreferenceConstants.CRYPTO_PKCS11_LIBRARY_PATH, Messages.getString("CryptoPreferencePage.11"), fep)); //$NON-NLS-1$
		    addField(new StringFieldEditor(PreferenceConstants.CRYPTO_PKCS11_CERTIFICATE_ALIAS, Messages.getString("CryptoPreferencePage.15"), fep)); //$NON-NLS-1$
		}
        
		// There is no option to decrypt by PKCS11 
		//addField(new BooleanFieldEditor(PreferenceConstants.CRYPTO_PKCS11_LIBRARY_ENABLED, Messages.getString("CryptoPreferencePage.12"), fep)); //$NON-NLS-1$
	}

	@Override
	public void init(IWorkbench workbench) {
		// Nothing to do.
	}
	
	private boolean isPKCS11Supported(){
	    
        try {
            String osName = System.getProperty("os.name");
            String osArch = System.getProperty("os.arch");
            if((osName.toLowerCase().contains("win") && osArch.contains("64"))
                    || osName.toLowerCase().contains("macos")){
                LOG.debug("Currently no PKCS#11 implementation for " + osName + "/" + osArch + " bit available");
                return false; // on a win7/64 / osx host-system pkcs#11 is not supported by native jvm
            }
        } catch (Exception e) {
            LOG.error("Error while registering verinice security provider.", e);
        }
        return true;
	}

}
