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

import static sernet.verinice.kerberos.preferences.Messages.KerberosPreferencePage_0;
import static sernet.verinice.kerberos.preferences.Messages.KerberosPreferencePage_2;
import static sernet.verinice.kerberos.preferences.Messages.KerberosPreferencePage_3;
import static sernet.verinice.kerberos.preferences.Messages.KerberosPreferencePage_4;
import static sernet.verinice.kerberos.preferences.Messages.KerberosPreferencePage_5;
import static sernet.verinice.kerberos.preferences.Messages.KerberosPreferencePage_6;
import static sernet.verinice.kerberos.preferences.Messages.KerberosPreferencePage_7;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
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

    private static Logger LOG = Logger.getLogger(KerberosPreferencePage.class);

    private StringFieldEditor aDServiceName;

    private String initialADServiceName;

    private boolean initialKerberosStatus;

    private BooleanFieldEditor activate;

    public KerberosPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setTitle(KerberosPreferencePage_0);
    }

    /**
     * Creates the field editors. Field editors are abstractions of the common
     * GUI blocks needed to manipulate various types of preferences. Each field
     * editor knows how to save and restore itself.
     */
    public void createFieldEditors() {

        activate = new BooleanFieldEditor(PreferenceConstants.KERBEROS_STATUS, KerberosPreferencePage_2, getFieldEditorParent());
        aDServiceName = new StringFieldEditor(PreferenceConstants.VERINICEPRO_SERVICE_NAME, KerberosPreferencePage_3, getFieldEditorParent());

        aDServiceName.setEnabled(initialKerberosStatus, getFieldEditorParent());

        addField(activate);
        addField(aDServiceName);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {

        super.propertyChange(event);

        if (event.getSource() == activate) {
            aDServiceName.setEnabled(activate.getBooleanValue(), getFieldEditorParent());
        }
    };

    @Override
    public boolean performOk() {
        boolean status = super.performOk();

        if (activate.getBooleanValue() != initialKerberosStatus || !initialADServiceName.equals(aDServiceName.getStringValue())) {
            restartMessageDialog();
        }

        return status;
    }

    @Override
    public void init(IWorkbench workbench) {
        initialKerberosStatus = getPreferenceStore().getBoolean(PreferenceConstants.KERBEROS_STATUS);
        initialADServiceName = getPreferenceStore().getString(PreferenceConstants.VERINICEPRO_SERVICE_NAME);
        noDefaultAndApplyButton();
    }

    public void restartMessageDialog() {
        Thread restart = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(50L);
                } catch (InterruptedException e) {
                    LOG.error("error while storing ad sso configuration occurred.", e);
                }
                getDisplay().syncExec(new Runnable() {
                    @Override
                    public void run() {
                        MessageDialog mDialog = new MessageDialog(Display.getDefault().getActiveShell(), KerberosPreferencePage_4, null, KerberosPreferencePage_5, MessageDialog.QUESTION, new String[] { KerberosPreferencePage_6, KerberosPreferencePage_7 }, 1); //$NON-NLS-1$ //$NON-NLS-2$

                        int result = mDialog.open();
                        if (result == 1) {
                            Workbench.getInstance().restart();
                        }
                    }
                });

            }
        });

        restart.start();

    }

    static Display getDisplay() {
        Display display = Display.getCurrent();
        // may be null if outside the UI thread
        if (display == null) {
            display = Display.getDefault();
        }
        return display;
    }
}
