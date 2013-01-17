/*******************************************************************************
 * Copyright (c) 2012 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import sernet.gs.ui.rcp.main.Activator;
import sernet.verinice.iso27k.rcp.CatalogView;
import sernet.verinice.iso27k.rcp.ISMView;

/**
 *
 */
public class MessageDialogWithTogglePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private BooleanFieldEditor infoDialogTransformCatalogItems;
    private BooleanFieldEditor infoDialogCopy;
    private BooleanFieldEditor infoDialogCut;
    private BooleanFieldEditor infoDialogProcess;
    private BooleanFieldEditor infoDialogLdapImport;
    private BooleanFieldEditor infoDialogStatusDerivation;
    private BooleanFieldEditor switchPerspectiveIsmView;
    private BooleanFieldEditor switchPerspectiveCatalogView;
    private BooleanFieldEditor showValidationReportWarning;

    
    public MessageDialogWithTogglePreferencePage(){
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription(Messages.getString("MessageDialogWithTogglePreferencePage.0")); //$NON-NLS-1$
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    @Override
    public void init(IWorkbench arg0) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    @Override
    protected void createFieldEditors() {
        infoDialogTransformCatalogItems = new BooleanFieldEditor(PreferenceConstants.INFO_CONTROLS_ADDED, Messages.getString("GeneralSettingsPage.InfoControlsAdded"), //$NON-NLS-1$
                getFieldEditorParent());
        addField(infoDialogTransformCatalogItems);

        infoDialogCopy = new BooleanFieldEditor(PreferenceConstants.INFO_ELEMENTS_COPIED, Messages.getString("GeneralSettingsPage.InfoCopy"), //$NON-NLS-1$
                getFieldEditorParent());
        addField(infoDialogCopy);

        infoDialogCut = new BooleanFieldEditor(PreferenceConstants.INFO_ELEMENTS_CUT, Messages.getString("GeneralSettingsPage.InfoCut"), //$NON-NLS-1$
                getFieldEditorParent());
        addField(infoDialogCut);
        
        infoDialogProcess = new BooleanFieldEditor(PreferenceConstants.INFO_PROCESSES_STARTED, Messages.getString("GeneralSettingsPage.InfoProcess"), //$NON-NLS-1$
                getFieldEditorParent());
        addField(infoDialogProcess);
        
        infoDialogLdapImport = new BooleanFieldEditor(PreferenceConstants.INFO_IMPORT_LDAP, Messages.getString("GeneralSettingsPage.InfoLdapImport"), //$NON-NLS-1$
                getFieldEditorParent());
        addField(infoDialogLdapImport);
        
        infoDialogStatusDerivation = new BooleanFieldEditor(PreferenceConstants.INFO_STATUS_DERIVED, Messages.getString("GeneralSettingsPage.InfoDerivationStatus"), getFieldEditorParent()); //$NON-NLS-1$
        addField(infoDialogStatusDerivation);
        
        switchPerspectiveIsmView = new BooleanFieldEditor(PreferenceConstants.getDontAskBeforeSwitch(ISMView.class), Messages.getString("GeneralSettingsPage.SwitchPerspectiveIsm"), //$NON-NLS-1$
                getFieldEditorParent());
        addField(switchPerspectiveIsmView);

        switchPerspectiveCatalogView = new BooleanFieldEditor(PreferenceConstants.getDontAskBeforeSwitch(CatalogView.class), Messages.getString("GeneralSettingsPage.SwitchPerspectiveCatalog"), //$NON-NLS-1$
                getFieldEditorParent());
        addField(switchPerspectiveCatalogView);
        
        showValidationReportWarning = new BooleanFieldEditor(PreferenceConstants.SHOW_REPORT_VALIDATION_WARNING, Messages.getString("GeneralSettingsPage.ShowValidationReportWarning"), getFieldEditorParent());
        addField(showValidationReportWarning);
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        if (event.getProperty().equals(FieldEditor.VALUE)) {
            checkState();
        }
    }

    @Override
    protected void checkState() {
        super.checkState();
        if (!isValid()) {
            return;
        }

    }

}
