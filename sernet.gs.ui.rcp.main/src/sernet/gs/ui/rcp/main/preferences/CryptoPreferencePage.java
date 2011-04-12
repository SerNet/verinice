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

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import sernet.gs.ui.rcp.main.Activator;

/**
 * Preference page for the cryptographic options.
 * 
 * TODO: Make this a nicer dialog.
 * 
 * @author Robert Schuster <r.schuster[at]tarent[dot]de>
 * 
 */
public class CryptoPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

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
		addField(new BooleanFieldEditor(PreferenceConstants.CRYPTO_VERINICE_SSL_SECURITY_ENABLED, "verwende verinice-Sicherheitsinfrastruktur für SSL-Verbindungen", fep));

		addField(new RadioGroupFieldEditor(PreferenceConstants.CRYPTO_TRUSTSTORE_SOURCE, "Quelle für den Zertifikatspeicher (Server- und CA-Zertifikate)",
				1, new String[][] { { "Datei", PreferenceConstants.CRYPTO_TRUSTSTORE_SOURCE_FROM_FILE },
						{ "PKCS#11-Bibliothek", PreferenceConstants.CRYPTO_TRUSTSTORE_SOURCE_FROM_PKCS11_LIBRARY }
				}, fep));
		
		addField(new RadioGroupFieldEditor(PreferenceConstants.CRYPTO_KEYSTORE_SOURCE, "Quelle für Schlüsselspeicher (private Schlüssel und Client-Zertifikate)",
				1, new String[][] { { "keine", PreferenceConstants.CRYPTO_KEYSTORE_SOURCE_NONE },
						{ "Datei", PreferenceConstants.CRYPTO_KEYSTORE_SOURCE_FROM_FILE },
						{ "PKCS#11-Bibliothek", PreferenceConstants.CRYPTO_KEYSTORE_SOURCE_FROM_PKCS11_LIBRARY }
				}, fep));
		
		addField(new StringFieldEditor(PreferenceConstants.CRYPTO_TRUSTSTORE_FILE, "Pfad zum Zertifikatspeicher", fep));
		addField(new StringFieldEditor(PreferenceConstants.CRYPTO_KEYSTORE_FILE, "Pfad zum Schlüsselspeicher", fep));

		addField(new BooleanFieldEditor(PreferenceConstants.CRYPTO_PKCS11_LIBRARY_ENABLED, "PKCS#11-Bibliothek für Verschlüsselung/Entschlüsselung verwenden", fep));
		
		addField(new StringFieldEditor(PreferenceConstants.CRYPTO_PKCS11_LIBRARY_PATH, "Pfad zur PKCS#11-Bibliothek", fep));
	}

	@Override
	public void init(IWorkbench workbench) {
		// Nothing to do.
	}

}
