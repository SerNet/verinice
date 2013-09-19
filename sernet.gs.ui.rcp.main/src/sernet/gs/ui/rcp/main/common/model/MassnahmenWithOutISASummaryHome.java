/*******************************************************************************
 * Copyright (c) 2011 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.common.model;

import java.util.Map;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.statscommands.MaturitySummary;
import sernet.gs.ui.rcp.main.service.statscommands.MaturitySummaryWithOutISA;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.iso27k.ControlGroup;

public class MassnahmenWithOutISASummaryHome extends MassnahmenSummaryHome {
   
	
	/**
     * @param elmt 
     * @param level 
     * @return
     * @throws CommandException 
     */
	@Override
    public Map<String, Double> getControlGroups(ControlGroup elmt) throws CommandException {
        MaturitySummaryWithOutISA command = new MaturitySummaryWithOutISA(elmt.getEntity().getEntityType(), elmt.getEntity().getDbId(), MaturitySummary.TYPE_IMPLEMENTATION);
        command = ServiceFactory.lookupCommandService().executeCommand(command);
        return command.getSummary();
    }

	@Override
    public Map<String, Double> getControlMaxGroups(ControlGroup elmt) throws CommandException {
        MaturitySummaryWithOutISA command = new MaturitySummaryWithOutISA(elmt.getEntity().getEntityType(), elmt.getEntity().getDbId(), MaturitySummary.TYPE_MAX);
        command = ServiceFactory.lookupCommandService().executeCommand(command);
        return command.getSummary();
    }

    /**
     * @param elmt
     * @return
     * @throws CommandException 
     */
	@Override
    public Map<String, Double> getControlGoal1Groups(ControlGroup elmt) throws CommandException {
        MaturitySummaryWithOutISA command = new MaturitySummaryWithOutISA(elmt.getEntity().getEntityType(), elmt.getEntity().getDbId(), MaturitySummary.TYPE_THRESHOLD1);
        command = ServiceFactory.lookupCommandService().executeCommand(command);
        return command.getSummary();

    }
    
   // FIXME ak creating links in linkmaker fails, after thagt no links can be created not even by dragndrop
    
    // FIXME ak remove "Person" field from Configuration, replace with uuid as STring, change query in "LoadConfiguration", allow accounts for iso-persons as well as bsimodel-persons, test if that works
    

    /**
     * @param elmt
     * @return
     * @throws CommandException 
     */
	@Override
    public Map<String, Double> getControlGoal2Groups(ControlGroup elmt) throws CommandException {
        MaturitySummaryWithOutISA command = new MaturitySummaryWithOutISA(elmt.getEntity().getEntityType(), elmt.getEntity().getDbId(), MaturitySummary.TYPE_THRESHOLD2);
        command = ServiceFactory.lookupCommandService().executeCommand(command);
        return command.getSummary();
        
    }
}
