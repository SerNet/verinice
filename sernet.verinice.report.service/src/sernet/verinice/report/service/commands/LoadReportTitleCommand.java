/*******************************************************************************
 * Copyright (c) 2010 Robert Schuster <r.schuster@tarent.de>.
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
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.report.service.commands;

import java.util.List;

import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadReportElements;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.Organization;

/**
 * Loads and returns the report's title.
 * 
 * TODO: Somehow allow setting and storing the report's title in the application and then
 * provide access to that value via this command.
 * 
 * TODO: The command could probably be abstracted in a way that it can retrieve some arbitrary
 * report values from verinice (e.g. the confidentally value). If those values are stored as a
 * CnATreeElement using a specialized command could be avoided (the LoadEntityValues can do that). 
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 */
@SuppressWarnings("serial")
public class LoadReportTitleCommand extends GenericCommand {
    
    private Integer root;
    private List<CnATreeElement> elements;
    private String orgName="";

    public LoadReportTitleCommand(Integer root) {
        this.root = root;
    }

	public String getResult() {
//		return "<h1>Information Technologie (IT)</h1><h1>Security Assessment at VW TEST - Company 1</h1><h1>Final Report</h1>";
		return "<h1>Information Technology (IT)</h1><h1>" + elements.get(0).getTitle() + " at " + orgName+ "</h1><h1>Final Report</h1>";
	}

	@Override
	public void execute() {
	    try {
	        LoadReportElements command = new LoadReportElements(Audit.TYPE_ID, root);
            command = getCommandService().executeCommand(command);
            elements = command.getElements();
            if (elements == null)
                return;
            
            LoadReportParentOrgForObject command2 = new LoadReportParentOrgForObject(elements.get(0));
            command2 = getCommandService().executeCommand(command2);
            if (command2.getOrg() != null)
                orgName = command2.getOrg().getTitle();
            
        } catch (CommandException e) {
            throw new RuntimeCommandException(e);
        }
	}

    


}
