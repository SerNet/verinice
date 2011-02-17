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

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.CnAWorkspace;

/**
 * Main preference page for CnA Tool Settings.
 * 
 * @author akoderman[at]sernet[dot]de
 * 
 */
public class DatenbankPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private static final Logger log = Logger.getLogger(DatenbankPreferencePage.class);

	private RadioGroupFieldEditor dbDriver;
	private StringFieldEditor dialect;
	private StringFieldEditor url;
	private StringFieldEditor user;
	private StringFieldEditor pass;

	private boolean modified = false;

	public DatenbankPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(Messages.getString("DatenbankPreferencePage.0"));//$NON-NLS-1$
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.FieldEditorPreferencePage#createContents
	 * (org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		final Link link = new Link(parent, SWT.NONE);
		link.setText(Messages.getString("DatenbankPreferencePage.1")); //$NON-NLS-1$
		link.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				Program.launch(event.text);
			}

		});
		return super.createContents(parent);
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	@Override
	public void createFieldEditors() {

		createRadioGroup();
		dialect = new StringFieldEditor(PreferenceConstants.DB_DIALECT, Messages.getString("DatenbankPreferencePage.7"), //$NON-NLS-1$
				getFieldEditorParent());
		addField(dialect);

		url = new StringFieldEditor(PreferenceConstants.DB_URL, Messages.getString("DatenbankPreferencePage.8"), //$NON-NLS-1$
				getFieldEditorParent());
		addField(url);

		user = new StringFieldEditor(PreferenceConstants.DB_USER, Messages.getString("DatenbankPreferencePage.9"), //$NON-NLS-1$
				getFieldEditorParent());
		addField(user);

		pass = new StringFieldEditor(PreferenceConstants.DB_PASS, Messages.getString("DatenbankPreferencePage.10"), //$NON-NLS-1$
				getFieldEditorParent());
		addField(pass);

	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);

		if (visible) {
			boolean standalone = getPreferenceStore().getString(PreferenceConstants.OPERATION_MODE).equals(PreferenceConstants.OPERATION_MODE_INTERNAL_SERVER);

			// Do not show the fields when the remote server is in use
			setEnabledFields(standalone);
		}
	}

	private void setEnabledFields(boolean enable) {
		Composite parent = getFieldEditorParent();
		dbDriver.setEnabled(enable, parent);
		dialect.setEnabled(enable, parent);
		url.setEnabled(enable, parent);
		user.setEnabled(enable, parent);
		pass.setEnabled(enable, parent);
		
		// always disable the Oracle-Button, it's just there to point the user to verinice.PRO:
		Control[] radioButtons = dbDriver.getRadioBoxControl(parent).getChildren();
		radioButtons[radioButtons.length-1].setEnabled(false);

		if (enable) {
			setMessage(null);
		} else {
			setMessage(Messages.getString("DatenbankPreferencePage.ConfigurationDisabled"));
		}
	}

	private void createRadioGroup() {
		dbDriver = new RadioGroupFieldEditor(PreferenceConstants.DB_DRIVER, Messages.getString("DatenbankPreferencePage.11"), //$NON-NLS-1$
				1, new String[][] { { Messages.getString("DatenbankPreferencePage.12"), PreferenceConstants.DB_DRIVER_DERBY }, //$NON-NLS-1$
						{ Messages.getString("DatenbankPreferencePage.13"), PreferenceConstants.DB_DRIVER_POSTGRES }, //$NON-NLS-1$
						{ Messages.getString("DatenbankPreferencePage.15"), PreferenceConstants.DB_DRIVER_ORACLE } //$NON-NLS-1$
				}, getFieldEditorParent());
		addField(dbDriver);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		if (event.getProperty().equals(FieldEditor.VALUE)) {
			if (event.getSource() == dbDriver) {
				setDefaults((String) event.getNewValue());
			}
			checkState();
		}

		modified = true;
	}

	private void setDefaults(String newValue) {
		if (newValue.equals(PreferenceConstants.DB_DRIVER_DERBY)) {
			dialect.setStringValue(PreferenceConstants.DB_DIALECT_derby);

			// replace backslashes
			// derby db url looks like this on windows: c:/Programme/Verinice...
			String derbyUrl = PreferenceConstants.DB_URL_DERBY.replace("%s", CnAWorkspace //$NON-NLS-1$
					.getInstance().getWorkdir().replaceAll("\\\\", "/"));
			Logger.getLogger(this.getClass()).debug("Derby url is " + derbyUrl);
			url.setStringValue(derbyUrl);
			user.setStringValue(""); //$NON-NLS-1$
			pass.setStringValue(""); //$NON-NLS-1$
		} else if (newValue.equals(PreferenceConstants.DB_DRIVER_POSTGRES)) {
			dialect.setStringValue(PreferenceConstants.DB_DIALECT_postgres);
			url.setStringValue(PreferenceConstants.DB_URL_POSTGRES);
			user.setStringValue(""); //$NON-NLS-1$
			pass.setStringValue(""); //$NON-NLS-1$
		} else if (newValue.equals(PreferenceConstants.DB_DRIVER_MYSQL)) {
			dialect.setStringValue(PreferenceConstants.DB_DIALECT_mysql);
			url.setStringValue(PreferenceConstants.DB_URL_MYSQL);
			user.setStringValue(""); //$NON-NLS-1$
			pass.setStringValue(""); //$NON-NLS-1$
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

	@Override
	public boolean performOk() {
		if (modified) {
			MessageDialog.openInformation(this.getShell(), Messages.getString("DatenbankPreferencePage.RestartRequired"), Messages.getString("DatenbankPreferencePage.RestartRequiredText"));
		}

		return super.performOk();
	}
}
