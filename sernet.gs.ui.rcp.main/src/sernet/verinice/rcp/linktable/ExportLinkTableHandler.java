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
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.iso27k.rcp.*;
import sernet.verinice.rcp.RightsEnabledHandler;
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
    private static ISchedulingRule iSchedulingRule = new Mutex();

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
            setBaseEnabled(false);
            MessageDialog.openError(HandlerUtil.getActiveShell(event), "Error",
                    "You don't have the permission to perform this action.");
        }

        return null;
    }

    protected VeriniceLinkTable createLinkTable() {

        setShell();
        final String filePath = VeriniceLinkTableUtil.createFilePath(shell,
                "Load verinice link table (.vlt) file",
                PreferenceConstants.DEFAULT_FOLDER_VLT,
                VeriniceLinkTableUtil.getVltExtensions());
        VeriniceLinkTable veriniceLinkTable = null;
        if (filePath != null) {
            veriniceLinkTable = VeriniceLinkTableIO.read(filePath);
            LinkTableFileRegistry.add(veriniceLinkTable.getId(), filePath);
        }
        return veriniceLinkTable;
    }


    public void setShell() {
        if (Display.getCurrent().getActiveShell() != null) {
            shell = Display.getCurrent().getActiveShell();
        }
    }

    private void exportToCsv() {

        final VeriniceLinkTable veriniceLinkTable = createLinkTable();
        if (veriniceLinkTable != null) {
            final String filePath = VeriniceLinkTableUtil.createFilePath(shell,
                    "Export link table to CSV (.csv) table",
                    PreferenceConstants.DEFAULT_FOLDER_CSV_EXPORT,
                    VeriniceLinkTableUtil.getCsvExtensions());
            if (filePath != null) {
                VeriniceWorkspaceJob job = new VeriniceWorkspaceJob("Export CSV-File",
                        "Error while exporting link table") {

                    @Override
                    protected void doRunInWorkspace() {

                        if (filePath != null) {
                            csvExportHandler.setFilePath(filePath);
                            List<List<String>> table = linkTableService
                                    .createTable(VeriniceLinkTableIO
                                            .createLinkTableConfiguration(veriniceLinkTable));
                            csvExportHandler.exportToFile(csvExportHandler.convert(table));
                        }
                    }
                };

                JobScheduler.scheduleJob(job, iSchedulingRule);

                // TODO rmotza the information about sucessful export does not
                // work yet
                // job.addJobChangeListener(new JobChangeAdapter() {
                // /*
                // * (non-Javadoc)
                // *
                // * @see
                // * org.eclipse.core.runtime.jobs.JobChangeAdapter#done(org.
                // * eclipse.core.runtime.jobs.IJobChangeEvent)
                // */
                // @Override
                // public void done(IJobChangeEvent event) {
                // if (Status.OK_STATUS.equals(event.getResult())) {
                // Shell parent = Display.getCurrent().getActiveShell();
                // MessageDialog.openInformation(parent, "Csv Export", "Export
                // finished");
                // }
                // }
                // });
            }
        }
    }


}
