/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.linktable.composite;

import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import sernet.gs.service.FileUtil;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.iso27k.rcp.ElementMultiselectWidget;
import sernet.verinice.iso27k.rcp.Messages;
import sernet.verinice.iso27k.rcp.action.ExportAction;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.rcp.linktable.VeriniceLinkTableUtil;
import sernet.verinice.service.commands.SyncParameter;
import sernet.verinice.service.csv.ICsvExport;
import sernet.verinice.service.linktable.vlt.VeriniceLinkTable;
import sernet.verinice.service.sync.VeriniceArchive;

/**
 * @author Daniel Murygin <dm@sernet.de>
 */
public class CsvExportDialog extends TitleAreaDialog {
    private static final Logger LOG = Logger.getLogger(CsvExportDialog.class);

    private static final String[] EXTENSION_ARRAY = new String[] {VeriniceArchive.EXTENSION_VERINICE_ARCHIVE,ExportAction.EXTENSION_XML};

    private static final String DEFAULT_ORGANIZATION_TITLE = "organization";

    /**
     * Indicates if the output should be encrypted.
     */
    private boolean encryptOutput = false;
    private boolean reImport = false;
    private ITreeSelection selection;
    private CnATreeElement selectedElement;
    private String filePath;
    private String sourceId;

    private ElementMultiselectWidget organizationWidget = null;

    private Text sourceIdText;
    private Text txtLocation;
    private String defaultFolder;
    private boolean useDefaultFolder = true;
    private String organizationTitle = DEFAULT_ORGANIZATION_TITLE;

    // ExportCommand.EXPORT_FORMAT_VERINICE_ARCHIV or ExportCommand.EXPORT_FORMAT_XML_PURE
    private int format = SyncParameter.EXPORT_FORMAT_DEFAULT;

    private boolean serverConnectionMode = false;

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

        setTitle(Messages.SamtExportDialog_0);
        setMessage(Messages.SamtExportDialog_1, IMessageProvider.INFORMATION);

        final Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout layout = (GridLayout) composite.getLayout();
        layout.marginWidth = layoutMargin;
        layout.marginHeight = layoutMargin;
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true,true);
        composite.setLayoutData(gd);

        try {
            organizationWidget = new ElementMultiselectWidget(composite, selection, selectedElement);
            setOrgTitle();

        } catch (CommandException ex) {
            LOG.error("Error while loading organizations", ex); //$NON-NLS-1$
            setMessage(Messages.SamtExportDialog_4, IMessageProvider.ERROR);
            return null;
        }

        addOrganizationelectionListener();


        if(!serverConnectionMode) {
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
            labelLocation.setText(Messages.SamtExportDialog_6);
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
            buttonBrowseLocations.setText(Messages.SamtExportDialog_7);
            buttonBrowseLocations.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {

                    filePath = VeriniceLinkTableUtil.createCsvFilePath(getShell(), label);
                    txtLocation.setText(filePath);
                }

                });
        }

        if(organizationWidget.getSelectedElement()!=null) {
            filePath = defaultFolder + organizationTitle + getDefaultExtension();
            txtLocation.setText(filePath);
        }

        composite.pack();
        return composite;
    }

    public void addOrganizationelectionListener() {
        SelectionListener organizationListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button checkbox = (Button) e.getSource();
                if(checkbox.getSelection()) {
                    changeFilePathToCurrentSelection();
                }
                super.widgetSelected(e);
            }

            public void changeFilePathToCurrentSelection() {
                setOrgTitle();
                if(txtLocation!=null) {
                    if(isFilepath()) {
                        filePath = FileUtil.getFolderFromPath(txtLocation.getText()) + organizationTitle + getDefaultExtension();
                    } else {
                        filePath = defaultFolder + organizationTitle + getDefaultExtension();
                    }
                    txtLocation.setText(filePath);
                }
                setSourceId(organizationWidget.getSelectedElement());
            }
        };

        organizationWidget.addSelectionLiustener(organizationListener);
    }

    private void setOrgTitle() {
        String title = null;
        if(organizationWidget.getSelectedElement()!=null) {
            title = organizationWidget.getSelectedElement().getTitle();
        }
        if(title!=null) {
            organizationTitle = convertToFileName(title);
            //organizationTitle = title.replaceAll("[^a-zA-Z]", ""); //hier ist es das  Umlaute-Problem, die werden ersetzt und nicht ordentlich ausgeschrieben!!!
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
            defaultFolder = System.getProperty("user.home");
        }
        if (!defaultFolder.endsWith(System.getProperty("file.separator"))) {
            defaultFolder = defaultFolder + System.getProperty("file.separator");
        }
        return defaultFolder;
    }

    private String getDefaultExtension() {
        return ICsvExport.CSV_FILE_SUFFIX;
    }

    private void setSourceId(CnATreeElement element) {
		if(element!=null && element.getSourceId()!=null) {
			this.sourceId = element.getSourceId();
			if(sourceIdText!=null) {
				sourceIdText.setText(element.getSourceId());
			}
		} else {
			this.sourceId = null;
			if(sourceIdText!=null) {
				sourceIdText.setText(""); //$NON-NLS-1$
			}
		}
	}



	private static String convertToFileName(String label) {
        String filename = ""; //$NON-NLS-1$
        if(label!=null) {
            filename = label.replace(' ', '_');
            filename = filename.replace("ä", "\u00E4"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("ü", "\u00FC"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("ö", "\u00F6"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("Ä", "\u00C4"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("Ü", "\u00DC"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("Ö", "\u00D6"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("ß", "\u00DF"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace(":", ""); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("\\", ""); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace(";", ""); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("<", ""); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace(">", ""); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("|", ""); //$NON-NLS-1$ //$NON-NLS-2$
           }
        return filename;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {

        StringBuilder sb = new StringBuilder();
        if (filePath == null || filePath.isEmpty()) {
            sb.append(Messages.SamtExportDialog_10);
        }
        if (organizationWidget.getSelectedElement() == null) {
            sb.append(Messages.SamtExportDialog_12);
        }
        if (sb.length() > 0) {
            sb.append("\n");
            sb.append(Messages.SamtExportDialog_13);
            setMessage(sb.toString(), IMessageProvider.ERROR);
        } else {

            for (CnATreeElement element : getSelectedElementSet()) {
                veriniceLinkTable.addScopeId(element.getDbId());
            }
            super.okPressed();
        }
    }

	public String getFilePath() {
        return filePath;
    }

    public boolean getEncryptOutput() {
        return encryptOutput;
    }

    public boolean getReImport() {
        return reImport;
    }
    public String getSourceId() {
        return sourceId;
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int exportFormat) {
        this.format = exportFormat;
    }

    /**
     * @return
     */
    public Set<CnATreeElement> getSelectedElementSet() {
        return organizationWidget.getSelectedElementSet();
    }

    /**
     * @return
     */
    public CnATreeElement getSelectedElement() {
        return organizationWidget.getSelectedElement();
    }
    public boolean getUseDefaultFolder(){
        return useDefaultFolder;
    }

    public static String[] getExtensionArray() {
        return EXTENSION_ARRAY;
    }

}
