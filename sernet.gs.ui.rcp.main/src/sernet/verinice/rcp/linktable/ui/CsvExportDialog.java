/*******************************************************************************
 * Copyright (c) 2016 Ruth Motza.
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
 *     Ruth Motza <rm[at]sernet[dot]de> - Adaption from copied class
 *     Daniel Murygin <dm[at]sernet[dot]de> - Implementation of copied class
 *                                          - Refactoring                      
 ******************************************************************************/
package sernet.verinice.rcp.linktable.ui;

import java.io.File;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sernet.gs.service.FileUtil;
import sernet.gs.service.StringUtil;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.iso27k.rcp.ScopeMultiselectWidget;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.rcp.linktable.LinkTableUtil;
import sernet.verinice.service.csv.ICsvExport;
import sernet.verinice.service.linktable.vlt.VeriniceLinkTable;

/**
 * Dialog which opens when "selected scopes" is chosen in
 * {@link LinkTableComposite}. The dialog displays a list of all 
 * organizations and IT networks (German:IT-Verbuende). The user
 * can select one or more organizations and IT networks which
 * are used for executing the link table query.
 * 
 * @author Ruth Motza <rm[at]sernet[dot]de>
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("restriction")
public class CsvExportDialog extends TitleAreaDialog {
    private static final Logger LOG = Logger.getLogger(CsvExportDialog.class);

    private static final String DEFAULT_ORGANIZATION_TITLE = "organization"; //$NON-NLS-1$

    private ITreeSelection selection;
    private CnATreeElement selectedElement;
    private String filePath;

    private ScopeMultiselectWidget organizationWidget = null;

    private Text txtLocation;
    private String defaultFolder;
    private String organizationTitle = DEFAULT_ORGANIZATION_TITLE;

    private VeriniceLinkTable veriniceLinkTable;

    private String label;

    public CsvExportDialog(Shell activeShell, String label, VeriniceLinkTable veriniceLinkTable) {
        this(activeShell, (CnATreeElement) null, label, veriniceLinkTable);
    }

    public CsvExportDialog(Shell activeShell, CnATreeElement selectedOrganization, String label,
            VeriniceLinkTable veriniceLinkTable) {
        super(activeShell);
        this.label = label;
        this.veriniceLinkTable = veriniceLinkTable;
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
        selectedElement = selectedOrganization;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        final int layoutMargin = 10;
        final int sourceIdCompositeNumColumns = 3;
        final int sourceIdCompositeMarginTop = 15;
        final int txtLocationMinimumWidth = 302;

        initDefaultFolder();

        setTitle(Messages.CsvExportDialog_0);
        setMessage(Messages.CsvExportDialog_1, IMessageProvider.INFORMATION);

        final Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout layout = (GridLayout) composite.getLayout();
        layout.marginWidth = layoutMargin;
        layout.marginHeight = layoutMargin;
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true,true);
        composite.setLayoutData(gd);

        try {
            organizationWidget = new ScopeMultiselectWidget(composite, selection, selectedElement);
            setOrgTitle();

        } catch (CommandException ex) {
            LOG.error("Error while loading organizations", ex); //$NON-NLS-1$
            setMessage(Messages.CsvExportDialog_4, IMessageProvider.ERROR);
            return null;
        }

        addOrganizationelectionListener();

        final Composite sourceIdComposite = new Composite(composite, SWT.NONE);
        sourceIdComposite.setLayout(new GridLayout(sourceIdCompositeNumColumns,false));
        ((GridLayout) sourceIdComposite.getLayout()).marginTop = sourceIdCompositeMarginTop;
        gd = new GridData(SWT.FILL, SWT.BOTTOM, true,false);
        gd.grabExcessHorizontalSpace=true;
        sourceIdComposite.setLayoutData(gd);

        /*
         * Widgets to browse for storage location:
         */

        final Label labelLocation = new Label(sourceIdComposite, SWT.NONE);
        labelLocation.setText(Messages.CsvExportDialog_6);
        txtLocation = new Text(sourceIdComposite, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
        gd = new GridData(SWT.FILL, SWT.TOP, true,false);
        gd.grabExcessHorizontalSpace=true;
        gd.minimumWidth = txtLocationMinimumWidth;
        txtLocation.setLayoutData(gd);
        txtLocation.addKeyListener(new KeyListener() {
            @Override
            public void keyReleased(KeyEvent e) {
                filePath = txtLocation.getText();
            }
            @Override
            public void keyPressed(KeyEvent e) {
                // nothing to do
            }
        });

        final Button buttonBrowseLocations = new Button(sourceIdComposite, SWT.NONE);
        buttonBrowseLocations.setText(Messages.CsvExportDialog_7);
        buttonBrowseLocations.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                filePath = LinkTableUtil.createCsvFilePath(getShell(), label, organizationTitle);
                txtLocation.setText(filePath);
            }

        });
     
        if(organizationWidget.getSelectedElement()!=null) {
            filePath = defaultFolder + organizationTitle + getDefaultExtension();
            txtLocation.setText(filePath);
        }

        composite.pack();
        return composite;
    }

    private void addOrganizationelectionListener() {
        SelectionListener organizationListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button checkbox = (Button) e.getSource();
                if(checkbox.getSelection()) {
                    changeFilePathToCurrentSelection();
                }
                super.widgetSelected(e);
            }   
        };

        organizationWidget.addSelectionListener(organizationListener);
    }
    
    private void changeFilePathToCurrentSelection() {
        setOrgTitle();
        if(txtLocation!=null) {
            if(isFilepath()) {
                filePath = FileUtil.getFolderFromPath(txtLocation.getText()) + organizationTitle + getDefaultExtension();
            } else {
                filePath = defaultFolder + organizationTitle + getDefaultExtension();
            }
            txtLocation.setText(filePath);
        }
    }

    private void setOrgTitle() {
        String title = null;
        if(organizationWidget.getSelectedElement()!=null) {
            title = organizationWidget.getSelectedElement().getTitle();
        }
        if(title!=null) {
            organizationTitle = StringUtil.convertToFileName(title);
        } else {
            organizationTitle = DEFAULT_ORGANIZATION_TITLE;
        }
    }

    private boolean isFilepath() {
        return txtLocation!=null && txtLocation.getText()!=null && !txtLocation.getText().isEmpty();
    }

    private String initDefaultFolder() {
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        defaultFolder = prefs.getString(PreferenceConstants.DEFAULT_FOLDER_CSV_EXPORT);
        if(defaultFolder==null || defaultFolder.isEmpty()) {
            defaultFolder = System.getProperty("user.home"); //$NON-NLS-1$
        }
        if (!defaultFolder.endsWith(System.getProperty("file.separator"))) { //$NON-NLS-1$
            defaultFolder = defaultFolder + System.getProperty("file.separator"); //$NON-NLS-1$
        }
        return defaultFolder;
    }

    private String getDefaultExtension() {
        return ICsvExport.CSV_FILE_SUFFIX;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        String errorMessage = createErrorMessage();
        if (errorMessage!=null && !errorMessage.isEmpty()) {
            showErrorMessage(errorMessage);
        } else {
            checkIfFileExistAndSave();
        }
    }

    private void checkIfFileExistAndSave() {
        File file = new File(filePath);
        if(file.exists() && !file.isDirectory()) { 
            MessageDialog mDialog = new MessageDialog(
                    getShell(), NLS.bind(Messages.CsvExportDialog_9, file.getName()),
                    null,
                    NLS.bind(Messages.CsvExportDialog_14, file.getParentFile().getName()),
                    MessageDialog.WARNING,
                    new String[] { Messages.CsvExportDialog_16, Messages.CsvExportDialog_17 }, 0);
            int result = mDialog.open();
            if (result == 0) {
                saveScopeIdsAndCloseDialog();
            }
        } else {
            saveScopeIdsAndCloseDialog();
        }
    }

    private void saveScopeIdsAndCloseDialog() {
        veriniceLinkTable.clearScopeIds();
        for (CnATreeElement element : getSelectedElementSet()) {
            veriniceLinkTable.addScopeId(element.getDbId());
        }
        super.okPressed();
    }
    
    private String createErrorMessage() {
        StringBuilder sb = new StringBuilder();
        if (filePath == null || filePath.isEmpty()) {
            sb.append(Messages.CsvExportDialog_10);
        }
        if (organizationWidget.getSelectedElement() == null) {
            sb.append(Messages.CsvExportDialog_12);
        }
        return sb.toString();
    }
    
    private void showErrorMessage(String message) {
        StringBuilder sb = new StringBuilder(message);
        sb.append("\n"); //$NON-NLS-1$
        sb.append(Messages.CsvExportDialog_13);
        setMessage(sb.toString(), IMessageProvider.ERROR); 
    }

	public String getFilePath() {
        return filePath;
    }

    private Set<CnATreeElement> getSelectedElementSet() {
        return organizationWidget.getSelectedElementSet();
    }
}
