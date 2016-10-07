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
 *     Ruth Motza <rm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.linktable.handlers;

import static sernet.verinice.rcp.linktable.LinkTableUtil.createCsvFilePath;
import static sernet.verinice.service.linktable.vlt.VeriniceLinkTableIO.createLinkTableConfiguration;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import sernet.gs.service.VeriniceCharset;
import sernet.gs.ui.rcp.main.Activator;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.iso27k.rcp.Mutex;
import sernet.verinice.rcp.RightsEnabledHandler;
import sernet.verinice.rcp.jobs.VeriniceWorkspaceJob;
import sernet.verinice.rcp.linktable.LinkTableUtil;
import sernet.verinice.rcp.linktable.Messages;
import sernet.verinice.service.csv.CsvExport;
import sernet.verinice.service.csv.ICsvExport;
import sernet.verinice.service.linktable.ILinkTableConfiguration;
import sernet.verinice.service.linktable.LinkTableService;
import sernet.verinice.service.linktable.LinkedTableCreator;
import sernet.verinice.service.linktable.generator.GraphLinkedTableCreator;
import sernet.verinice.service.linktable.vlt.VeriniceLinkTable;
import sernet.verinice.service.linktable.vlt.VeriniceLinkTableIO;

/**
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class ExportLinkTableHandler extends RightsEnabledHandler {

    private LinkTableService linkTableService;
    private ICsvExport csvExportHandler = new CsvExport();
    private Shell shell = null;
    private boolean fromEditor;
    private VeriniceLinkTable veriniceLinkTable;
    private String csvFilePath;
    private LinkedTableCreator linkedTableCreator;

    public ExportLinkTableHandler() {
        this(false, null);
    }

    public ExportLinkTableHandler(boolean fromEditor, VeriniceLinkTable veriniceLinkTable) {
        super(false);
        this.fromEditor = fromEditor;
        this.veriniceLinkTable = veriniceLinkTable;
        this.linkTableService = new LinkTableService();
        linkedTableCreator = new GraphLinkedTableCreator();
        this.linkTableService.setLinkTableCreator(linkedTableCreator);
        this.csvExportHandler.setCharset(VeriniceCharset.CHARSET_WINDOWS_1250);
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.EXPORT_LINK_TABLE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
     * ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        if (checkRights()) {
            exportToCsv();
        } else {
            MessageDialog.openError(HandlerUtil.getActiveShell(event), "Error", //$NON-NLS-1$
                    Messages.ExportLinkTableHandler_1);
        }

        return null;
    }

    protected VeriniceLinkTable createLinkTable() {

        setShell();
        final String filePath = LinkTableUtil.createVltFilePath(shell,
                Messages.ExportLinkTableHandler_2, SWT.OPEN, null);
        VeriniceLinkTable veriniceLinkTableTemp = null;
        if (filePath != null) {
            veriniceLinkTableTemp = VeriniceLinkTableIO.read(filePath);
        }
        return veriniceLinkTableTemp;
    }


    public void setShell() {
        if (Display.getCurrent().getActiveShell() != null) {
            shell = Display.getCurrent().getActiveShell();
        }
    }

    private void exportToCsv() {
        setShell();
        if (!fromEditor) {
            veriniceLinkTable = createLinkTable();
        }
        if (veriniceLinkTable == null) {
            return;
        }


        if(veriniceLinkTable.useAllScopes()){
            csvFilePath = createCsvFilePath(shell, Messages.ExportLinkTableHandler_3, null);
        } else{
            csvFilePath = LinkTableUtil.createCsvFilePathAndHandleScopes(shell,
                    Messages.ExportLinkTableHandler_3, veriniceLinkTable);
        }

        if (csvFilePath != null) {
            VeriniceWorkspaceJob job = new VeriniceWorkspaceJob(Messages.ExportLinkTableHandler_4,
                    Messages.ExportLinkTableHandler_5) {

                @Override
                protected void doRunInWorkspace() {

                    if (csvFilePath != null) {
                        Activator.inheritVeriniceContextState();

                        ILinkTableConfiguration conf = createLinkTableConfiguration(veriniceLinkTable);
                        List<List<String>> table = linkTableService.createTable(conf);

                        csvExportHandler.setFilePath(csvFilePath);
                        csvExportHandler.exportToFile(csvExportHandler.convert(table));
                    }
                }
            };

            JobScheduler.scheduleJob(job, new Mutex());
        }

    }


}
