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
import org.eclipse.jface.preference.StringFieldEditor;
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

		addField(new BooleanFieldEditor(PreferenceConstants.CRYPTO_KEYSTORE_ENABLED, "verwende Keystore", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.CRYPTO_KEYSTORE_FILE, "Pfad zum Keystore", getFieldEditorParent()));

		addField(new BooleanFieldEditor(PreferenceConstants.CRYPTO_TRUSTSTORE_ENABLED, "verwende Truststore", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.CRYPTO_TRUSTSTORE_FILE, "Pfad zum Truststore", getFieldEditorParent()));

		addField(new BooleanFieldEditor(PreferenceConstants.CRYPTO_PKCS11_LIBRARY_ENABLED, "verwende PKCS#11-Bibliothek", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.CRYPTO_PKCS11_LIBRARY_PATH, "Pfad zur PKCS#11-Bibliothek", getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
		// Nothing to do.
	}

}
