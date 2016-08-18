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
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
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

    private Composite fieldEditorParent;

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

        String opmode = getPreferenceStore().getString(PreferenceConstants.OPERATION_MODE);
        boolean isStandalone = opmode
                .equals(PreferenceConstants.OPERATION_MODE_INTERNAL_SERVER);

        if (!isStandalone) {
            GridLayoutFactory.fillDefaults().applyTo(parent);
            Label servermode = new Label(parent, SWT.NONE);
            servermode.setText(Messages.getString("KatalogePreferencePage.22"));//$NON-NLS-1$
            fieldEditorParent = new Composite(parent, SWT.NULL);
        } else {
            Link generalTextLink = new Link(parent, SWT.NONE);
        fieldEditorParent = new Composite(parent, SWT.NULL);
        GridLayoutFactory.swtDefaults().numColumns(1).margins(5, 0)
                .generateLayout(fieldEditorParent);
        fieldEditorParent.setFont(parent.getFont());
            generalTextLink.setText(Messages.getString("KatalogePreferencePage.0")); //$NON-NLS-1$
            generalTextLink.addListener(SWT.Selection, new KatalogPreferenceLinkListener());
        createFieldEditors();
        }
        initialize();
        checkState();
        return fieldEditorParent;

	}

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#
     * getFieldEditorParent()
     */
    @Override
    protected Composite getFieldEditorParent() {
        return fieldEditorParent;
    }

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	@Override
	public void createFieldEditors() {

        Group catalogComposite = new Group(getFieldEditorParent(), SWT.FILL);
        catalogComposite.setText(Messages.getString("KatalogePreferencePage.24")); //$NON-NLS-1$
        GridLayoutFactory.fillDefaults().margins(5, 10).numColumns(1)
                .generateLayout(catalogComposite);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(catalogComposite);
        Link calatogLink = new Link(catalogComposite, SWT.NONE);
        calatogLink.setText(Messages.getString("KatalogePreferencePage.11")); //$NON-NLS-1$
        calatogLink.addListener(SWT.Selection, new KatalogPreferenceLinkListener());
        Composite catalogEditorParent = new Composite(catalogComposite, SWT.FILL);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(catalogEditorParent);
        zipfilePath = new FileFieldEditor(PreferenceConstants.BSIZIPFILE,
                Messages.getString("KatalogePreferencePage.8"), //$NON-NLS-1$
                catalogEditorParent);
        zipfilePath.setFileExtensions(new String[] { "*.zip;*.ZIP", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
        addField(zipfilePath);

        Group datenschutzZipComposite = new Group(getFieldEditorParent(), SWT.FILL);
        datenschutzZipComposite.setText(Messages.getString("KatalogePreferencePage.26")); //$NON-NLS-1$
        GridLayoutFactory.fillDefaults().margins(5, 10).numColumns(1)
                .generateLayout(datenschutzZipComposite);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(datenschutzZipComposite);
        Link datenschutzZipLink = new Link(datenschutzZipComposite, SWT.NONE);
        datenschutzZipLink.setText(Messages.getString("KatalogePreferencePage.14")); //$NON-NLS-1$
        datenschutzZipLink.addListener(SWT.Selection, new KatalogPreferenceLinkListener());
        Composite datenSchutzZipPathParent = new Composite(datenschutzZipComposite, SWT.FILL);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(datenSchutzZipPathParent);
        datenschutzZipPath = new FileFieldEditor(PreferenceConstants.DSZIPFILE,
                Messages.getString("KatalogePreferencePage.10"), //$NON-NLS-1$
                datenSchutzZipPathParent);
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
	public boolean isValid() {
		// always allow user to navigate away from page:
		return true;
	}

    private class KatalogPreferenceLinkListener implements Listener {

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.
         * Event)
         */
        @Override
        public void handleEvent(Event event) {
            Program.launch(event.text);

        }

    }

}
