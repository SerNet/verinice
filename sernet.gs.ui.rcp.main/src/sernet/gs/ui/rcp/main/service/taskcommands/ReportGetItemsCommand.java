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
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;
import java.util.ArrayList;

import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.reports.IBSIReport;
import sernet.gs.ui.rcp.main.reports.BsiReport;
import sernet.gs.ui.rcp.main.reports.ISMReport;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementById;
import sernet.gs.ui.rcp.main.service.crudcommands.RefreshElement;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.HydratorUtil;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.service.commands.LoadBSIModel;

public class ReportGetItemsCommand extends GenericCommand {

    private BsiReport report;
    private ArrayList<CnATreeElement> items;
    private Integer itverbundDbId;
    private Integer scopeDbId;

    public ReportGetItemsCommand(BsiReport report) {
        this.report = report;
        if (report instanceof ISMReport) {
            ISMReport ismReport = (ISMReport) report;
            scopeDbId = ismReport.getOrganization().getDbId();
        } else
            itverbundDbId = report.getItverbund().getDbId();
    }

    public void execute() {
        try {

            if (itverbundDbId != null) {
                getBsiItems();
            } else {
                getISMItems();
            }
            
            

        } catch (CommandException e) {
            throw new RuntimeCommandException(e);
        }

        report = null;
    }

    /**
     * @throws CommandException 
     * 
     */
    private void getISMItems() throws CommandException {
        LoadCnAElementById command = new LoadCnAElementById(Organization.TYPE_ID, scopeDbId);
        command = getCommandService().executeCommand(command);
        Organization org = (Organization) command.getFound();

        // replace report's itverbund with the DB-connected instance:
        ((ISMReport) report).setOrganization(org);

        IBaseDao<? extends CnATreeElement, Serializable> dao = getDaoFactory().getDAO(Organization.class);

        items = ((IBSIReport) report).getItems();
        HydratorUtil.hydrateElements(dao, items, false);
    
    }

    /**
     * @throws CommandException
     */
    private void getBsiItems() throws CommandException {
        LoadCnAElementById command = new LoadCnAElementById(ITVerbund.TYPE_ID, itverbundDbId);
        command = getCommandService().executeCommand(command);
        ITVerbund itverbund = (ITVerbund) command.getFound();

        // replace report's itverbund with the DB-connected instance:
        ((BsiReport) report).setItverbund(itverbund);

        IBaseDao<? extends CnATreeElement, Serializable> dao = getDaoFactory().getDAO(ITVerbund.class);

        items = ((IBSIReport) report).getItems();
        HydratorUtil.hydrateElements(dao, items, false);
    }

    public ArrayList<CnATreeElement> getItems() {
        return items;
    }

}
