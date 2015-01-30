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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.Workbench;

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

    private StringFieldEditor aDServiceName;

    private String initialADServiceName;

    private boolean initialKerberosStatus;

    private BooleanFieldEditor activate;

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

        activate = new BooleanFieldEditor(PreferenceConstants.KERBEROS_STATUS, "Kerberos active", getFieldEditorParent());
        initialKerberosStatus = getPreferenceStore().getBoolean(PreferenceConstants.KERBEROS_STATUS);
        addField(activate);

        aDServiceName = new StringFieldEditor(PreferenceConstants.VERINICEPRO_SERVICE_NAME, "verinicepro AD service", getFieldEditorParent());
        initialADServiceName = getPreferenceStore().getString(PreferenceConstants.VERINICEPRO_SERVICE_NAME);
        aDServiceName.setEnabled(initialKerberosStatus, getFieldEditorParent());

        addField(aDServiceName);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getSource() == activate) {
            aDServiceName.setEnabled(activate.getBooleanValue(), getFieldEditorParent());
        }
    };

    @Override
    public boolean performOk() {
        boolean status = super.performOk();
        if (activate.getBooleanValue() != initialKerberosStatus || !initialADServiceName.equals(aDServiceName.getStringValue())) {
            MessageDialog mDialog = new MessageDialog(Display.getDefault().getActiveShell(), "blah", null, "blub", MessageDialog.QUESTION, new String[] { "1", "2" }, 1); //$NON-NLS-1$ //$NON-NLS-2$
            
            int result = mDialog.open();            
            if (result == 1) {
                Workbench.getInstance().restart();
            }
        }

        return status;
    }

    @Override
    public void init(IWorkbench arg0) {
    }

}