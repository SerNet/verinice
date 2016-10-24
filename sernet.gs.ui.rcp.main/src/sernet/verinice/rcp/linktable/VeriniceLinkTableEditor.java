
/*******************************************************************************
 * Copyright (c) 2016 Daniel Murygin <dm{a}sernet{dot}de>.
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
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.linktable;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.verinice.interfaces.IVeriniceConstants;
import sernet.verinice.service.csv.CsvExport;
import sernet.verinice.service.csv.ICsvExport;
import sernet.verinice.service.linktable.LinkTableService;
import sernet.verinice.service.linktable.vlt.VeriniceLinkTable;
import sernet.verinice.service.linktable.vlt.VeriniceLinkTableIO;

/**
 *
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class VeriniceLinkTableEditor extends EditorPart {

    private static final Logger LOG = Logger.getLogger(VeriniceLinkTableEditor.class);

    public static final String EDITOR_ID = VeriniceLinkTableEditor.class.getName();

    private VeriniceLinkTable veriniceLinkTable;
    private LinkTableService linkTableService = new LinkTableService();
    private ICsvExport csvExportHandler = new CsvExport();

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void doSave(IProgressMonitor monitor) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#doSaveAs()
     */
    @Override
    public void doSaveAs() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
     */
    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        if (! (input instanceof VeriniceLinkTableEditorInput)) {
            throw new PartInitException("Input is not an instance of " + VeriniceLinkTableEditorInput.class.getSimpleName());
        }
        VeriniceLinkTableEditorInput vltEditorInput = (VeriniceLinkTableEditorInput) input;
        veriniceLinkTable=vltEditorInput.getInput();
        setSite(site);
        setInput(vltEditorInput);
        setPartName(veriniceLinkTable.getName());
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#isDirty()
     */
    @Override
    public boolean isDirty() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
     */
    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        Button exportButton = new Button(parent, SWT.PUSH);
        exportButton.setText("Export CSV");
        exportButton.setToolTipText("Export this link table to a CSV file");
        exportButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                exportToCsv();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
    }

    private void exportToCsv() {
        List<List<String>> table = linkTableService.createTable(VeriniceLinkTableIO.createLinkTableConfiguration(veriniceLinkTable));
        String filePath = createFilePath();
        csvExportHandler.setFilePath(filePath);
        csvExportHandler.exportToFile(csvExportHandler.convert(table));
    }

    private static String createFilePath() {
        FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
        dialog.setText("Export link table to CSV (.csv) table");
        try {
            dialog.setFilterPath(getDirectory());
        } catch (Exception e1) {
            LOG.debug("Error with file path: " + getDirectory(), e1);
            dialog.setFileName(""); //$NON-NLS-1$
        }
        dialog.setFilterExtensions(new String[] {"*" + ICsvExport.CSV_FILE_SUFFIX}); //$NON-NLS-1$
        dialog.setFilterNames(new String[] {"CSV table (.csv)"});
        dialog.setFilterIndex(0);
        return dialog.open();
    }

    private static String getDirectory() {
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        String dir = prefs.getString(PreferenceConstants.DEFAULT_FOLDER_CSV_EXPORT);
        if(dir==null || dir.isEmpty()) {
            dir = System.getProperty(IVeriniceConstants.USER_HOME);
        }
        if (!dir.endsWith(System.getProperty(IVeriniceConstants.FILE_SEPARATOR))) {
            dir = dir + System.getProperty(IVeriniceConstants.FILE_SEPARATOR);
        }
        return dir;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        // TODO Auto-generated method stub

    }

}
