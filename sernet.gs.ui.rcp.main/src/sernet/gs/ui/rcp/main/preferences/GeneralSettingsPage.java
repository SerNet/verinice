/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
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
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import sernet.gs.service.VeriniceCharset;
import sernet.gs.ui.rcp.main.Activator;
import sernet.verinice.iso27k.rcp.CatalogView;
import sernet.verinice.iso27k.rcp.ISMView;

/**
 * Main preference page for CnA Tool Settings.
 * 
 * @author akoderman[at]sernet[dot]de
 * 
 */
public class GeneralSettingsPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private BooleanFieldEditor errorPopups;
	private BooleanFieldEditor derbyWarning;
	private BooleanFieldEditor inputHelperHints;
	private BooleanFieldEditor infoDialogTransformCatalogItems;
	private BooleanFieldEditor infoDialogCopy;
	private BooleanFieldEditor infoDialogCut;
    private BooleanFieldEditor infoDialogProcess;
	private BooleanFieldEditor infoDialogLdapImport;
	private BooleanFieldEditor switchPerspectiveIsmView;
	private BooleanFieldEditor switchPerspectiveCatalogView;
	
	private ComboFieldEditor encodingFieldEditor;
	private static final String[][] encodingComboValues = new String[][]{
        new String[]{VeriniceCharset.CHARSET_UTF_8.displayName(),VeriniceCharset.CHARSET_UTF_8.name()},
        new String[]{VeriniceCharset.CHARSET_ISO_8859_15.displayName(),VeriniceCharset.CHARSET_ISO_8859_15.name()},
        new String[]{VeriniceCharset.CHARSET_WINDOWS_1252.displayName(),VeriniceCharset.CHARSET_WINDOWS_1252.name()}
        
	};

	public GeneralSettingsPage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(Messages.getString("GeneralSettingsPage.0")); //$NON-NLS-1$
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	@Override
	public void createFieldEditors() {
		derbyWarning = new BooleanFieldEditor(PreferenceConstants.FIRSTSTART, Messages.getString("GeneralSettingsPage.1"), //$NON-NLS-1$
				getFieldEditorParent());
		addField(derbyWarning);

		errorPopups = new BooleanFieldEditor(PreferenceConstants.ERRORPOPUPS, Messages.getString("GeneralSettingsPage.2"), //$NON-NLS-1$
				getFieldEditorParent());
		addField(errorPopups);

		inputHelperHints = new BooleanFieldEditor(PreferenceConstants.INPUTHINTS, Messages.getString("GeneralSettingsPage.3"), getFieldEditorParent());
		addField(inputHelperHints);

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

		switchPerspectiveIsmView = new BooleanFieldEditor(PreferenceConstants.getDontAskBeforeSwitch(ISMView.class), Messages.getString("GeneralSettingsPage.SwitchPerspectiveIsm"), //$NON-NLS-1$
				getFieldEditorParent());
		addField(switchPerspectiveIsmView);

		switchPerspectiveCatalogView = new BooleanFieldEditor(PreferenceConstants.getDontAskBeforeSwitch(CatalogView.class), Messages.getString("GeneralSettingsPage.SwitchPerspectiveCatalog"), //$NON-NLS-1$
				getFieldEditorParent());
		addField(switchPerspectiveCatalogView);

		encodingFieldEditor = new ComboFieldEditor(PreferenceConstants.CHARSET_CATALOG, 
				"Zeichencodierung (Encoding) neuer Control Kataloge", 
				encodingComboValues, 
				getFieldEditorParent());
        addField(encodingFieldEditor);
		
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}
