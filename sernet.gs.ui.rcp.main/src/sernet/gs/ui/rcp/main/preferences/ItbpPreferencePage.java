/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman
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
 *     Alexander Koderman - initial API and implementation
 *     Daniel Murygin 
 ******************************************************************************/
package sernet.gs.ui.rcp.main.preferences;

import java.io.File;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;
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
import sernet.verinice.rcp.Preferences;

/**
 * Preference page for IT baseline protection (ITBP).
 */
public class ItbpPreferencePage extends FieldEditorPreferencePage
        implements IWorkbenchPreferencePage {

    private static final Logger LOG = Logger.getLogger(ItbpPreferencePage.class);

    private FileFieldEditor catalogZipfilePath;

    private Composite fieldEditorParent;

    public ItbpPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    @Override
    protected Control createContents(Composite parent) {
        boolean isStandalone = sernet.verinice.rcp.Preferences.isStandalone();
        if (!isStandalone) {
            GridLayoutFactory.fillDefaults().applyTo(parent);
            Label servermode = new Label(parent, SWT.NONE);
            servermode.setText(Messages.getString("ItbpPreferencePage.22"));//$NON-NLS-1$
            fieldEditorParent = new Composite(parent, SWT.NULL);
        } else {
            Link generalTextLink = new Link(parent, SWT.NONE);
            fieldEditorParent = new Composite(parent, SWT.NULL);
            GridLayoutFactory.swtDefaults().numColumns(1).margins(5, 0)
                    .generateLayout(fieldEditorParent);
            fieldEditorParent.setFont(parent.getFont());
            generalTextLink.setText(Messages.getString("ItbpPreferencePage.0")); //$NON-NLS-1$
            generalTextLink.addListener(SWT.Selection, new KatalogPreferenceLinkListener());
            createFieldEditors();
        }
        initialize();
        checkState();
        return fieldEditorParent;

    }

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
        catalogComposite.setText(Messages.getString("ItbpPreferencePage.24")); //$NON-NLS-1$
        GridLayoutFactory.fillDefaults().margins(5, 10).numColumns(1)
                .generateLayout(catalogComposite);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(catalogComposite);
        Link calatogLink = new Link(catalogComposite, SWT.NONE);
        calatogLink.setText(Messages.getString("ItbpPreferencePage.11")); //$NON-NLS-1$
        calatogLink.addListener(SWT.Selection, new KatalogPreferenceLinkListener());
        Composite catalogEditorParent = new Composite(catalogComposite, SWT.FILL);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(catalogEditorParent);
        catalogZipfilePath = new FileFieldEditor(PreferenceConstants.BSIZIPFILE,
                Messages.getString("ItbpPreferencePage.8"), //$NON-NLS-1$
                catalogEditorParent);
        catalogZipfilePath.setFileExtensions(new String[] { "*.zip;*.ZIP", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
        addField(catalogZipfilePath);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        if (event.getProperty().equals(FieldEditor.VALUE)) {
            checkState();
            if (event.getSource() == catalogZipfilePath
                    && Preferences.isBpCatalogLoadedFromZipFile()) {
                try {
                    IInternalServer internalServer = Activator.getDefault().getInternalServer();
                    internalServer.setGSCatalogURL(
                            new File(catalogZipfilePath.getStringValue()).toURI().toURL());
                } catch (MalformedURLException e) {
                    LOG.warn("ITBP catalog zip file path is an invalid URL."); //$NON-NLS-1$
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

    @Override
    public void init(IWorkbench workbench) {
        // nothing to do
    }

    @Override
    public boolean isValid() {
        // always allow user to navigate away from page:
        return true;
    }

    private class KatalogPreferenceLinkListener implements Listener {
        @Override
        public void handleEvent(Event event) {
            Program.launch(event.text);

        }

    }

}
