/*******************************************************************************
 * Copyright (c) 2015 verinice.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     verinice <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.kerberos.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;

import sernet.verinice.kerberos.Activator;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 * 
 * @author Benjamin Weiﬂenfels <bw[at]sernet[dot]de>
 *
 */
public class KerberosPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public KerberosPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("Configure the kerberos authentification");
    }

    /**
     * Creates the field editors. Field editors are abstractions of the common
     * GUI blocks needed to manipulate various types of preferences. Each field
     * editor knows how to save and restore itself.
     */
    public void createFieldEditors() {
        addField(new BooleanFieldEditor(PreferenceConstants.VERINICEPRO_SERVICE, "Kerberos active", getFieldEditorParent()));
        addField(new StringFieldEditor(PreferenceConstants.VERINICEPRO_SERVICE, "verinicepro AD service", getFieldEditorParent()));
    }

    public void init(IWorkbench workbench) {
    }

}