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

import java.io.File;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
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
import sernet.verinice.interfaces.IInternalServer;

/**
 * Main preference page for CnA Tool Settings.
 * 
 * @author akoderman[at]sernet[dot]de
 * 
 */
public class KatalogePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private static final Logger LOG = Logger.getLogger(KatalogePreferencePage.class);

	private FileFieldEditor zipfilePath;
	private FileFieldEditor datenschutzZipPath;

	public KatalogePreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createContents (org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
	    final Link link = new Link(parent, SWT.NONE);
        link.setText(Messages.getString("KatalogePreferencePage.0")); //$NON-NLS-1$
        link.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                Program.launch(event.text);
            }
        });
	    final Link link2 = new Link(parent, SWT.NONE);
	    link2.setText(Messages.getString("KatalogePreferencePage.11") + //$NON-NLS-1$
				Messages.getString("KatalogePreferencePage.14")); //$NON-NLS-1$
	    link2.addListener(SWT.Selection, new Listener() {
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

		zipfilePath = new FileFieldEditor(PreferenceConstants.BSIZIPFILE, Messages.getString("KatalogePreferencePage.8"), //$NON-NLS-1$
				getFieldEditorParent());
		zipfilePath.setFileExtensions(new String[] { "*.zip;*.ZIP", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
		addField(zipfilePath);

		datenschutzZipPath = new FileFieldEditor(PreferenceConstants.DSZIPFILE, Messages.getString("KatalogePreferencePage.10"), //$NON-NLS-1$
				getFieldEditorParent());
		datenschutzZipPath.setFileExtensions(new String[] { "*.zip;*.ZIP", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
		addField(datenschutzZipPath);

	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		if (event.getProperty().equals(FieldEditor.VALUE)) {
			checkState();
			IInternalServer internalServer = Activator.getDefault().getInternalServer();
			Preferences prefs = Activator.getDefault().getPluginPreferences();
			String accessMethod = prefs.getString(PreferenceConstants.GSACCESS);

			if (event.getSource() == zipfilePath && accessMethod.equals(PreferenceConstants.GSACCESS_ZIP)) {
				try {
					internalServer.setGSCatalogURL(new File(zipfilePath.getStringValue()).toURI().toURL());
				} catch (MalformedURLException e) {
					LOG.warn("GS catalog zip file path is an invalid URL."); //$NON-NLS-1$
				}
			} else if (event.getSource() == datenschutzZipPath) {
				try {
					internalServer.setDSCatalogURL(new File(datenschutzZipPath.getStringValue()).toURI().toURL());
				} catch (MalformedURLException e) {
					LOG.warn("DS catalog zip file path is an invalid URL."); //$NON-NLS-1$
				}
			}
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
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		// only editable when server is not used, client has direct access to GS
		// catalogues
		// otherwise server is used to access gs catalogue data to ensure that
		// all clients are
		// working on the same data
		if (visible) {
			String opmode = getPreferenceStore().getString(PreferenceConstants.OPERATION_MODE);
			setEnabledFields(opmode.equals(PreferenceConstants.OPERATION_MODE_INTERNAL_SERVER));
		}
	}

	private void setEnabledFields(boolean enable) {
		datenschutzZipPath.setEnabled(enable, getFieldEditorParent());
		zipfilePath.setEnabled(enable, getFieldEditorParent());

		if (enable) {
			setMessage(null);
		} else {
			setMessage(Messages.getString("KatalogePreferencePage.22")); //$NON-NLS-1$
		}
	}

	@Override
	public boolean isValid() {
		// always allow user to navigate away from page:
		return true;
	}

}
