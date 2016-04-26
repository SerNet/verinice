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
package sernet.verinice.rcp.linktable;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import sernet.gs.ui.rcp.main.Activator;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.iso27k.rcp.*;
import sernet.verinice.rcp.RightsEnabledHandler;
import sernet.verinice.rcp.jobs.VeriniceWorkspaceJob;
import sernet.verinice.service.csv.CsvExport;
import sernet.verinice.service.csv.ICsvExport;
import sernet.verinice.service.linktable.LinkTableService;
import sernet.verinice.service.linktable.vlt.VeriniceLinkTable;
import sernet.verinice.service.linktable.vlt.VeriniceLinkTableIO;

/**
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class ExportLinkTableHandler extends RightsEnabledHandler {

    private LinkTableService linkTableService = new LinkTableService();
    private ICsvExport csvExportHandler = new CsvExport();
    private Shell shell = null;
    private boolean fromEditor;
    private VeriniceLinkTable veriniceLinkTable;
    private String csvFilePath;

    public ExportLinkTableHandler() {
        this(false, null);
    }

    public ExportLinkTableHandler(boolean fromEditor, VeriniceLinkTable veriniceLinkTable) {
        super(false);
        this.fromEditor = fromEditor;
        this.veriniceLinkTable = veriniceLinkTable;
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
        final String filePath = VeriniceLinkTableUtil.createVltFilePath(shell,
                Messages.ExportLinkTableHandler_2);
        VeriniceLinkTable veriniceLinkTableTemp = null;
        if (filePath != null) {
            veriniceLinkTableTemp = VeriniceLinkTableIO.read(filePath);
            LinkTableFileRegistry.add(veriniceLinkTableTemp.getId(), filePath);
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
            csvFilePath = VeriniceLinkTableUtil.createCsvFilePath(shell,
                    Messages.ExportLinkTableHandler_3);
        } else{
            csvFilePath = VeriniceLinkTableUtil.createCsvFilePathAndHandleScopes(shell,
                    Messages.ExportLinkTableHandler_3, veriniceLinkTable);
        }

        if (csvFilePath != null) {
            VeriniceWorkspaceJob job = new VeriniceWorkspaceJob(Messages.ExportLinkTableHandler_4,
                    Messages.ExportLinkTableHandler_5) {

                @Override
                protected void doRunInWorkspace() {

                    if (csvFilePath != null) {
                        Activator.inheritVeriniceContextState();
                        csvExportHandler.setFilePath(csvFilePath);
                        List<List<String>> table = linkTableService
                                .createTable(VeriniceLinkTableIO
                                        .createLinkTableConfiguration(veriniceLinkTable));
                        table.add(0, VeriniceLinkTableUtil.getTableHeaders(veriniceLinkTable));
                        csvExportHandler.exportToFile(csvExportHandler.convert(table));
                    }
                }
            };

            JobScheduler.scheduleJob(job, new Mutex());
        }

    }


}
